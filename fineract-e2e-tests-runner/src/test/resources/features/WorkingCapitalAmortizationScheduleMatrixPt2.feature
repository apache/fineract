@WorkingCapital
@WorkingCapitalAmortizationScheduleMatrixPt2Feature
Feature: WorkingCapitalAmortizationScheduleMatrixPt2

  # =============================================================================
  # Scope 6: Tiny rate (many rows)
  # =============================================================================

  @TestRailId:C102498
  Scenario: Verify tiny rate schedule - UC1: low periodPaymentRate produces many rows and sampled values still sum correctly
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with the following matrix data:
      | accountingRule | npvDayCount | repaymentFrequencyType | repaymentEvery |
      | NONE           | 360         | DAYS                   | 1              |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 90              | 10000              | 0.36              | 10       |
    When Admin successfully approves the working capital loan on "01 January 2026" with "90" amount and "10" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "90" EUR transaction amount and "10" discount amount
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 10.00             | 90.00                 | 10000.00           | 0.36              | 360         | 0.10                  | 1000                  |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedDiscountFeeBalance |
      | 0         | 2026-01-01 | -90.00                | 90.00           | 10.00                      |
      | 1         | 2026-01-02 | 0.10                  | 89.92           | 9.98                       |
      | 2         | 2026-01-03 | 0.10                  | 89.84           | 9.96                       |
      | 1000      | 2028-09-27 | 0.10                  | 0.00            | 0.00                       |
    And The retrieved amortization schedule has exactly 1001 payment rows
    And The retrieved amortization schedule expected amortization sums to the discount fee and both expected balances close to zero

  # =============================================================================
  # Scope 7: 0.005 rounding boundaries
  # =============================================================================

  @TestRailId:C102499
  Scenario: Verify 0.005 rounding boundary - UC1: 2-decimal currency rounds 50.015 daily payment to 50.02 (HALF_EVEN rounding)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with the following matrix data:
      | accountingRule | npvDayCount | repaymentFrequencyType | repaymentEvery | digitsAfterDecimal |
      | NONE           | 360         | DAYS                   | 1              | 2                  |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 10000           | 100000             | 18.0054           | 0        |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and "0" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount and "0" discount amount
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 0.00              | 10000.00              | 100000.00          | 18.0054           | 360         | 50.02                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedDiscountFeeBalance |
      | 0         | 2026-01-01 | -10000.00             | 10000.00        | 0.00                       |
      | 1         | 2026-01-02 | 50.02                 | 9949.98         | 0.00                       |
      | 2         | 2026-01-03 | 50.02                 | 9899.96         | 0.00                       |
      | 199       | 2026-07-19 | 50.02                 | 46.02           | 0.00                       |
      | 200       | 2026-07-20 | 46.02                 | 0.00            | 0.00                       |
    And The retrieved amortization schedule has exactly 201 payment rows
    And The last projected payment amount is the smaller tail remainder
    And The retrieved amortization schedule has no negative monetary amounts

  @TestRailId:C102500
  Scenario: Verify 0.005 rounding boundary - UC2: 3-decimal currency leaves 0.006 outstanding after repayments
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with the following matrix data:
      | accountingRule | npvDayCount | repaymentFrequencyType | repaymentEvery | digitsAfterDecimal |
      | NONE           | 365         | DAYS                   | 1              | 3                  |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 17                | 1000     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    # --- Repayment ---
    When Admin sets the business date to "03 January 2026"
    And Customer makes repayment on "03 January 2026" with 46 transaction amount on Working Capital loan
    # --- Repayment ---
    When Admin sets the business date to "04 January 2026"
    And Customer makes repayment on "04 January 2026" with 46 transaction amount on Working Capital loan
    # --- Repayment ---
    And Customer makes repayment on "02 January 2026" with 9907.994 transaction amount on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance payload contains the following fields:
      | field                | value |
      | principalOutstanding | 0.006 |
    # --- Amortization schedule verification ---
    When Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 17                | 365         | 46.575                | 215                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 1000.00                    | 9000.00       | 1000.00                  |
      | 1         | 2026-01-02 | 46.575                | 8962.380        | 8.955                      | 9907.994            | 999.855                  | 991.045                    | 91.861        | 0.145                    |
      | 2         | 2026-01-03 | 46.575                | 45.377          | 0.091                      | 46.000              | 0.090                    | 0.054                      | 45.951        | 0.055                    |
      | 3         | 2026-01-04 | 45.997                | 0.000           | 0.046                      | 46.000              | 0.055                    | 0.009                      | 0.006         | 0.000                    |
      | 4         | 2026-01-05 | 0.006                 | 0.000           | 0.000                      |                     |                          | 0.000                      |               |                          |
    And The retrieved amortization schedule actual amortization total is "1000"
    And The retrieved amortization schedule has no negative monetary amounts
    And The retrieved amortization schedule has exactly 5 payment rows

  # =============================================================================
  # Scope 8: Expected vs actual columns
  # =============================================================================

  @TestRailId:C102501
  Scenario: Verify expected vs actual columns - UC1: exact on-time payments keep expected and actual columns aligned
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    # --- Repayment ---
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    # --- Repayment ---
    When Admin sets the business date to "03 January 2026"
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                     | 9000.00       |                          |
      | 1         | 2026-01-02 | 50.00                 | 8959.61         | 50.00               | 8959.61       | 9.61                     |
      | 2         | 2026-01-03 | 50.00                 | 8919.18         | 50.00               | 8919.18       | 9.57                     |
      | 200       | 2026-07-20 | 50.00                 | 0.00            |                     |               |                          |
    And The retrieved amortization schedule has exactly 201 payment rows
    And Every fully paid amortization schedule period has actual amortization equal to expected amortization
    And The retrieved amortization schedule has no negative monetary amounts

  @TestRailId:C102502
  Scenario: Verify expected vs actual columns - UC2: partial payment records reduced actual payment while expected instalment stays unchanged
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    # --- Repayment ---
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 25 transaction amount on Working Capital loan
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                     | 9000.00       |                          |
      | 1         | 2026-01-02 | 50.00                 | 8959.61         | 25.00               | 8979.81       | 4.81                     |
      | 2         | 2026-01-03 | 50.00                 | 8939.39         |                     |               |                          |
      | 201       | 2026-07-21 | 25.00                 | 0.00            |                     |               |                          |
    And The retrieved amortization schedule has exactly 202 payment rows
    And The retrieved amortization schedule has no negative monetary amounts

  @TestRailId:C102503
  Scenario: Verify expected vs actual columns - UC3: missed payment day records zero actual payment and re-projects remaining expected balances
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    # --- Repayment ---
    When Admin sets the business date to "03 January 2026"
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 1000.00                    | 9000.00       | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8959.61         | 9.61                       | 0.00                | 0.00                     | 990.39                     | 9000.00       | 1000.00                  |
      | 2         | 2026-01-03 | 50.00                 | 8959.61         | 9.61                       | 50.00               | 9.61                     | 990.39                     | 8959.61       | 990.39                   |
      | 3         | 2026-01-04 | 50.00                 | 8919.18         | 9.57                       |                     |                          | 980.82                     |               |                          |
      | 201       | 2026-07-21 | 50.00                 | 0.00            | 0.05                       |                     |                          | 0.00                       |               |                          |
    And The retrieved amortization schedule has exactly 202 payment rows
    And The retrieved amortization schedule has no negative monetary amounts

  # =============================================================================
  # Scope 9: Annual EIR
  # =============================================================================

  @TestRailId:C102504
  Scenario: Verify annual EIR - UC1: daily NPV 360
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    Then Working capital loan details has annual EIR "0.468451024804980076"

  # =============================================================================
  # Scope 10: Rate changes
  # =============================================================================

  @TestRailId:C102506
  Scenario: Verify rate change - UC1: single rate decrease and schedule reprojects
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    When Admin sets the business date to "02 January 2026"
    Then Working Capital Loan period payment rate in effect is "18"
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 1000.00                    | 9000.00       | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8959.61         | 9.61                       |                     |                          | 990.39                     |               |                          |
      | 2         | 2026-01-03 | 50.00                 | 8919.18         | 9.57                       |                     |                          | 980.82                     |               |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 0.05                       |                     |                          | 0.00                       |               |                          |
    And The retrieved amortization schedule has exactly 201 payment rows
    # --- Rate change ---
    And Admin update Working Capital period payment rate with "9" value
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then Working Capital Loan period payment rate in effect is "9"
    And The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 1000.00                    | 9000.00       | 1000.00                  |
      | 1         | 2026-01-02 | 25.00                 | 8979.82         | 4.82                       |                     |                          | 995.18                     |               |                          |
      | 2         | 2026-01-03 | 25.00                 | 8959.62         | 4.80                       |                     |                          | 990.38                     |               |                          |
      | 400       | 2027-02-05 | 25.00                 | 0.00            | 0.01                       |                     |                          | 0.00                       |               |                          |
    And The retrieved amortization schedule has exactly 401 payment rows
    And The retrieved amortization schedule has no negative monetary amounts

  @TestRailId:C102507
  Scenario: Verify rate change - UC2: single rate increase effective from a future date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 9                 | 1000     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then Working Capital Loan period payment rate in effect on "01 January 2026" is "9"
    And The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 9                 | 360         | 25.00                 | 400                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 1000.00                    | 9000.00       | 1000.00                  |
      | 1         | 2026-01-02 | 25.00                 | 8979.82         | 4.82                       |                     |                          | 995.18                     |               |                          |
      | 2         | 2026-01-03 | 25.00                 | 8959.62         | 4.80                       |                     |                          | 990.38                     |               |                          |
      | 400       | 2027-02-05 | 25.00                 | 0.00            | 0.01                       |                     |                          | 0.00                       |               |                          |
    And The retrieved amortization schedule has exactly 401 payment rows
    # --- Rate change ---
    And Admin update Working Capital period payment rate with "18" value effective from "15 January 2026"
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    And Working Capital Loan period payment rate in effect on "15 January 2026" is "18"
    And The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 9                 | 360         | 25.00                 | 400                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 1000.00                    | 9000.00       | 1000.00                  |
      | 1         | 2026-01-02 | 25.00                 | 8979.82         | 4.82                       |                     |                          | 995.18                     |               |                          |
      | 13        | 2026-01-14 | 25.00                 | 8736.77         | 4.69                       |                     |                          | 938.23                     |               |                          |
      | 14        | 2026-01-15 | 50.00                 | 8696.10         | 9.33                       |                     |                          | 928.90                     |               |                          |
      | 15        | 2026-01-16 | 50.00                 | 8655.38         | 9.28                       |                     |                          | 919.62                     |               |                          |
      | 207       | 2026-07-27 | 25.00                 | 0.00            | 0.03                       |                     |                          | 0.00                       |               |                          |
    And The retrieved amortization schedule has exactly 208 payment rows
    And The retrieved amortization schedule has no negative monetary amounts

  @TestRailId:C102508
  Scenario: Verify rate change - UC3: multiple sequential rate changes
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then Working Capital Loan period payment rate in effect on "01 January 2026" is "18"
    And The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 1000.00                    | 9000.00       | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8959.61         | 9.61                       |                     |                          | 990.39                     |               |                          |
      | 2         | 2026-01-03 | 50.00                 | 8919.18         | 9.57                       |                     |                          | 980.82                     |               |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 0.05                       |                     |                          | 0.00                       |               |                          |
    And The retrieved amortization schedule has exactly 201 payment rows
    # --- Rate change ---
    And Admin update Working Capital period payment rate with "9" value effective from "05 January 2026"
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then Working Capital Loan period payment rate in effect on "05 January 2026" is "9"
    And The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 1000.00                    | 9000.00       | 1000.00                  |
      | 3         | 2026-01-04 | 50.00                 | 8878.70         | 9.52                       |                     |                          | 971.30                     |               |                          |
      | 4         | 2026-01-05 | 25.00                 | 8858.45         | 4.75                       |                     |                          | 966.55                     |               |                          |
      | 5         | 2026-01-06 | 25.00                 | 8838.19         | 4.74                       |                     |                          | 961.81                     |               |                          |
      | 397       | 2027-02-02 | 25.00                 | 0.00            | 0.01                       |                     |                          | 0.00                       |               |                          |
    And The retrieved amortization schedule has exactly 398 payment rows
    # --- Rate change ---
    And Admin update Working Capital period payment rate with "18" value effective from "10 January 2026"
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then Working Capital Loan period payment rate in effect on "10 January 2026" is "18"
    And The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 1000.00                    | 9000.00       | 1000.00                  |
      | 3         | 2026-01-04 | 50.00                 | 8878.70         | 9.52                       |                     |                          | 971.30                     |               |                          |
      | 4         | 2026-01-05 | 25.00                 | 8858.45         | 4.75                       |                     |                          | 966.55                     |               |                          |
      | 5         | 2026-01-06 | 25.00                 | 8838.19         | 4.74                       |                     |                          | 961.81                     |               |                          |
      | 8         | 2026-01-09 | 25.00                 | 8777.35         | 4.71                       |                     |                          | 947.65                     |               |                          |
      | 9         | 2026-01-10 | 50.00                 | 8736.72         | 9.37                       |                     |                          | 938.28                     |               |                          |
      | 10        | 2026-01-11 | 50.00                 | 8696.05         | 9.33                       |                     |                          | 928.95                     |               |                          |
      | 202       | 2026-07-22 | 50.00                 | 24.97           | 0.08                       |                     |                          | 0.03                       |               |                          |
      | 203       | 2026-07-23 | 25.00                 | 0.00            | 0.03                       |                     |                          | 0.00                       |               |                          |
    And The retrieved amortization schedule has exactly 204 payment rows
    And The retrieved amortization schedule has no negative monetary amounts

  @TestRailId:C102512
  Scenario: Verify rate change - UC4: backdated payment placed after and backdated to before applied rate change reprojects the schedule
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    # --- Baseline: original rate 18, no payments ---
    And Admin retrieves the projected amortization schedule
    Then Working Capital Loan period payment rate in effect on "02 January 2026" is "18"
    And The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 1000.00                    | 9000.00       | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8959.61         | 9.61                       |                     |                          | 990.39                     |               |                          |
      | 2         | 2026-01-03 | 50.00                 | 8919.18         | 9.57                       |                     |                          | 980.82                     |               |                          |
      | 199       | 2026-07-19 | 50.00                 | 49.95           | 0.11                       |                     |                          | 0.05                       |               |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 0.05                       |                     |                          | 0.00                       |               |                          |
    And The retrieved amortization schedule has exactly 201 payment rows
    # --- Rate change on 05 Jan ---
    When Admin sets the business date to "05 January 2026"
    And Admin update Working Capital period payment rate with "9" value effective from "05 January 2026"
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then Working Capital Loan period payment rate in effect on "05 January 2026" is "9"
    And The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 1000.00                    | 9000.00       | 1000.00                  |
      | 3         | 2026-01-04 | 50.00                 | 8959.61         | 9.61                       | 0.00                | 0.00                     | 990.39                     | 9000.00       | 1000.00                  |
      | 4         | 2026-01-05 | 25.00                 | 8979.82         | 4.82                       |                     |                          | 995.18                     |               |                          |
      | 5         | 2026-01-06 | 25.00                 | 8959.62         | 4.80                       |                     |                          | 990.38                     |               |                          |
      | 402       | 2027-02-07 | 25.00                 | 24.99           | 0.03                       |                     |                          | 0.01                       |               |                          |
      | 403       | 2027-02-08 | 25.00                 | 0.00            | 0.01                       |                     |                          | 0.00                       |               |                          |
    And The retrieved amortization schedule has exactly 404 payment rows
    # --- Backdated repayment on 03 Jan (before the rate change) while current date is 10 Jan ---
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 1000.00                    | 9000.00       | 1000.00                  |
      | 2         | 2026-01-03 | 50.00                 | 8959.61         | 9.61                       | 50.00               | 9.61                     | 990.39                     | 8959.61       | 990.39                   |
      | 3         | 2026-01-04 | 50.00                 | 8919.18         | 9.57                       | 0.00                | 0.00                     | 980.82                     | 8959.61       | 990.39                   |
      | 4         | 2026-01-05 | 25.00                 | 8939.41         | 4.80                       |                     |                          | 985.59                     |               |                          |
      | 5         | 2026-01-06 | 25.00                 | 8919.19         | 4.78                       |                     |                          | 980.81                     |               |                          |
      | 400       | 2027-02-05 | 25.00                 | 24.99           | 0.03                       |                     |                          | 0.01                       |               |                          |
      | 401       | 2027-02-06 | 25.00                 | 0.00            | 0.01                       |                     |                          | 0.00                       |               |                          |
    And The retrieved amortization schedule has exactly 402 payment rows
    And The retrieved amortization schedule has no negative monetary amounts

  @TestRailId:C102513
  Scenario: Verify rate change - UC5: undo of a pre-rate-change payment placed after an applied rate change reprojects the schedule
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    # --- Baseline: original rate 18, no payments ---
    And Admin retrieves the projected amortization schedule
    Then Working Capital Loan period payment rate in effect on "02 January 2026" is "18"
    And The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 1000.00                    | 9000.00       | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8959.61         | 9.61                       |                     |                          | 990.39                     |               |                          |
      | 2         | 2026-01-03 | 50.00                 | 8919.18         | 9.57                       |                     |                          | 980.82                     |               |                          |
      | 199       | 2026-07-19 | 50.00                 | 49.95           | 0.11                       |                     |                          | 0.05                       |               |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 0.05                       |                     |                          | 0.00                       |               |                          |
    And The retrieved amortization schedule has exactly 201 payment rows
    # --- Make a payment on 03 Jan at the original rate 18 ---
    When Admin sets the business date to "03 January 2026"
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 1000.00                    | 9000.00       | 1000.00                  |
      | 2         | 2026-01-03 | 50.00                 | 8959.61         | 9.61                       | 50.00               | 9.61                     | 990.39                     | 8959.61       | 990.39                   |
      | 3         | 2026-01-04 | 50.00                 | 8919.18         | 9.57                       |                     |                          | 980.82                     |               |                          |
      | 199       | 2026-07-19 | 50.00                 | 99.84           | 0.16                       |                     |                          | 0.16                       |               |                          |
      | 201       | 2026-07-21 | 50.00                 | 0.00            | 0.05                       |                     |                          | 0.00                       |               |                          |
    And The retrieved amortization schedule has exactly 202 payment rows
    # --- Rate change on 05 Jan ---
    When Admin sets the business date to "05 January 2026"
    And Admin update Working Capital period payment rate with "9" value effective from "05 January 2026"
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then Working Capital Loan period payment rate in effect on "05 January 2026" is "9"
    And The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 1000.00                    | 9000.00       | 1000.00                  |
      | 2         | 2026-01-03 | 50.00                 | 8959.61         | 9.61                       | 50.00               | 9.61                     | 990.39                     | 8959.61       | 990.39                   |
      | 3         | 2026-01-04 | 50.00                 | 8919.18         | 9.57                       | 0.00                | 0.00                     | 980.82                     | 8959.61       | 990.39                   |
      | 4         | 2026-01-05 | 25.00                 | 8939.41         | 4.80                       |                     |                          | 985.59                     |               |                          |
      | 5         | 2026-01-06 | 25.00                 | 8919.19         | 4.78                       |                     |                          | 980.81                     |               |                          |
      | 400       | 2027-02-05 | 25.00                 | 24.99           | 0.03                       |                     |                          | 0.01                       |               |                          |
      | 401       | 2027-02-06 | 25.00                 | 0.00            | 0.01                       |                     |                          | 0.00                       |               |                          |
    And The retrieved amortization schedule has exactly 402 payment rows
    # --- Undo the pre-rate-change payment after the rate change, while current date is 10 Jan ---
    When Admin sets the business date to "10 January 2026"
    And Customer undo "1"th "REPAYMENT" transaction made on "03 January 2026" on Working Capital loan
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then Working Capital Loan period payment rate in effect on "05 January 2026" is "9"
    And The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 1000.00                    | 9000.00       | 1000.00                  |
      | 2         | 2026-01-03 | 50.00                 | 8959.61         | 9.61                       | 0.00                | 0.00                     | 990.39                     | 9000.00       | 1000.00                  |
      | 3         | 2026-01-04 | 50.00                 | 8959.61         | 9.61                       | 0.00                | 0.00                     | 990.39                     | 9000.00       | 1000.00                  |
      | 4         | 2026-01-05 | 25.00                 | 8979.82         | 4.82                       |                     |                          | 995.18                     |               |                          |
      | 5         | 2026-01-06 | 25.00                 | 8959.62         | 4.80                       |                     |                          | 990.38                     |               |                          |
      | 402       | 2027-02-07 | 25.00                 | 24.99           | 0.03                       |                     |                          | 0.01                       |               |                          |
      | 403       | 2027-02-08 | 25.00                 | 0.00            | 0.01                       |                     |                          | 0.00                       |               |                          |
    And The retrieved amortization schedule has exactly 404 payment rows
    And The retrieved amortization schedule has no negative monetary amounts

  # =============================================================================
  # Scope 11: Persisted model consistency after every transaction (API-only)
  # =============================================================================

  @TestRailId:C102509
  Scenario: Verify persisted model consistency - UC1: after repayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    # --- Repayment ---
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 1234 transaction amount on Working Capital loan
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8959.61         | 9.61                       | 990.39                     | 1234.00             | 7990.47       | 775.53                   |
      | 2         | 2026-01-03 | 50.00                 | 7949.01         | 8.54                       | 766.99                     |                     |               |                          |
      | 177       | 2026-06-27 | 16.00                 | 0.00            | 0.02                       | 0.00                       |                     |               |                          |
    And The retrieved amortization schedule has exactly 178 payment rows
    And The retrieved amortization schedule actual amortization is consistent with the loan realized and unrealized income after close of business

  @TestRailId:C102510
  Scenario: Verify persisted model consistency - UC2: after charge and repayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 35.0 transaction amount
    # --- Repayment ---
    And Customer makes repayment on "10 January 2026" with 85 transaction amount on Working Capital loan
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 0.00              | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 180                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 0.00                       |                     | 9000.00       |                          | 0.00                     |
      | 1         | 2026-01-02 | 50.00                 | 8950.00         | 0.00                       | 0.00                       | 0.00                | 9000.00       | 0.00                     | 0.00                     |
      | 2         | 2026-01-03 | 50.00                 | 8950.00         | 0.00                       | 0.00                       | 0.00                | 9000.00       | 0.00                     | 0.00                     |
      | 10        | 2026-01-11 | 50.00                 | 8900.00         | 0.00                       | 0.00                       |                     |               |                          |                          |
      | 180       | 2026-06-30 | 50.00                 | 400.00          | 0.00                       | 0.00                       |                     |               |                          |                          |
    And The retrieved amortization schedule has exactly 189 payment rows
    And The retrieved amortization schedule actual amortization is consistent with the loan realized and unrealized income after close of business
    And The retrieved amortization schedule has no negative monetary amounts

  @TestRailId:C102511
  Scenario: Verify persisted model consistency - UC3: after rate change
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    # --- Repayment ---
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    # --- Rate change ---
    And Admin update Working Capital period payment rate with "9" value
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 1000.00                    | 9000.00       | 1000.00                  |
      | 1         | 2026-01-02 | 25.00                 | 8979.82         | 4.82                       | 50.00               | 9.62                     | 995.18                     | 8959.62       | 990.38                   |
      | 2         | 2026-01-03 | 25.00                 | 8939.42         | 4.80                       |                     |                          | 985.58                     |               |                          |
      | 399       | 2027-02-04 | 25.00                 | 0.00            | 0.01                       |                     |                          | 0.00                       |               |                          |
    And The retrieved amortization schedule has exactly 400 payment rows
    And The retrieved amortization schedule actual amortization is consistent with the loan realized and unrealized income after close of business

  # =============================================================================
  # Matrix dimension: Charges
  # =============================================================================

  @TestRailId:C102519
  Scenario: Verify charges - UC1: charge after maturity is payable on a closed discounted loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    # --- Payoff ---
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Admin closes the Working Capital loan with a full repayment on "02 January 2026"
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    # --- Post-maturity charge ---
    When Admin sets the business date to "20 July 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 July 2026" due date and 25.0 transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 20 July 2026    | 25.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 25.0       | 25.0            | 0.0      | 0.0            | 0.0                 | 0.0          |
    And Working capital loan details has the following field values:
      | balance.totalOutstanding     | 25.0       |
      | balance.principalOutstanding | 0.0        |
      | timeline.actualMaturityDate  | 2026-07-20 |
    # --- Fee repayment ---
    And Customer makes repayment on "20 July 2026" with 25.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 25.0       | 0.0             | 25.0     | 0.0            | 0.0                 | 0.0          |
    And Working capital loan details has the following field values:
      | balance.totalOutstanding | 0.0 |
    # --- Discount amortization verification ---
    When Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 1000.00                    | 9000.00       | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8959.61         | 1000.00                    | 10000.00            | 1000.00                  | 0.00                       | 0.00          | 0.00                     |
    And The retrieved amortization schedule has exactly 201 payment rows
    And The retrieved amortization schedule actual amortization total is "1000"

