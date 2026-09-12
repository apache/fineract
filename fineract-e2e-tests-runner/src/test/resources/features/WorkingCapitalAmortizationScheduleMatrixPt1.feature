@WorkingCapital
@WorkingCapitalAmortizationScheduleMatrixPt1Feature
Feature: WorkingCapitalAmortizationScheduleMatrixPt1

  # =============================================================================
  # Scope 1: Amortization schedule & EIR math (S1-S4)
  # Matrix variations: accounting NONE/ACC_DEF_REV_AM, NPV 360/365,
  #                    period payment frequency DAYS(1/7/30)/DAYS(7)/MONTHS,
  #                    discount modes, business date enabled/disabled
  # =============================================================================

  @TestRailId:C102486
  Scenario: Verify amortization schedule & EIR math - UC1: baseline, DAYS(1) frequency, NPV 360
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with the following matrix data:
      | accountingRule | npvDayCount | repaymentFrequencyType | repaymentEvery |
      | NONE           | 360         | DAYS                   | 1              |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         | 1000.00                    |
      | 1         | 2026-01-02 | 50.00                 | 8959.61         | 990.39                     |
      | 2         | 2026-01-03 | 50.00                 | 8919.18         | 980.82                     |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 0.00                       |
    And The retrieved amortization schedule has exactly 201 payment rows
    And The retrieved amortization schedule has no negative monetary amounts

  @TestRailId:C102482
  Scenario: Verify amortization schedule & EIR math - UC2: daily, NPV 365
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with the following matrix data:
      | accountingRule | npvDayCount | repaymentFrequencyType | repaymentEvery |
      | NONE           | 365         | DAYS                   | 1              |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 365         | 49.32                 | 203                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         | 1000.00                    |
      | 1         | 2026-01-02 | 49.32                 | 8960.16         | 990.52                     |
      | 2         | 2026-01-03 | 49.32                 | 8920.28         | 981.08                     |
      | 203       | 2026-07-23 | 37.36                 | 0.00            | 0.00                       |
    And The retrieved amortization schedule has exactly 204 payment rows

  @TestRailId:C102487
  Scenario: Verify amortization schedule & EIR math - UC3: ACC_DEF_REV_AM accounting, daily NPV 360
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with the following matrix data:
      | accountingRule | npvDayCount | repaymentFrequencyType | repaymentEvery |
      | ACC_DEF_REV_AM | 360         | DAYS                   | 1              |
    Then Admin verifies Working Capital Loan Product has accounting rule "ACC_DEF_REV_AM"
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    # --- Repayment ---
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Journal entries verification ---
    And Working Capital Loan Transactions tab has a "DISCOUNT_FEE" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
    And Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 9.61  |        |
      | INCOME    | 404000       | Interest Income           |       | 9.61   |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 50.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 50.0  |        |
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8959.61         | 9.61                       | 990.39                     | 50.00               | 8959.61       | 9.61                     | 990.39                   |
      | 2         | 2026-01-03 | 50.00                 | 8919.18         | 9.57                       | 980.82                     |                     |               |                          |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 0.05                       | 0.00                       |                     |               |                          |                          |
    And The retrieved amortization schedule has exactly 201 payment rows
    And The retrieved amortization schedule actual amortization is consistent with the loan realized and unrealized income after close of business

  @TestRailId:C102488
  Scenario: Verify amortization schedule & EIR math - UC4: no discount, daily NPV 360
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with the following matrix data:
      | accountingRule | npvDayCount | repaymentFrequencyType | repaymentEvery |
      | NONE           | 360         | DAYS                   | 1              |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 10000           | 100000             | 18                | 0        |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and "0" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount and "0" discount amount
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 0.00              | 10000.00              | 100000.00          | 18                | 360         |                       |                       |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 0         | 2026-01-01 | -10000.00             | 10000.00        |                            | 0.00                       |
      | 1         | 2026-01-02 | 50.00                 | 9950.00         | 0.00                       | 0.00                       |
      | 2         | 2026-01-03 | 50.00                 | 9900.00         | 0.00                       | 0.00                       |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 0.00                       | 0.00                       |
    And The retrieved amortization schedule has exactly 201 payment rows

  @TestRailId:C102490
  Scenario: Verify amortization schedule & EIR math - UC5: discount added after disbursement, daily NPV 360
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with the following matrix data:
      | accountingRule | npvDayCount | repaymentFrequencyType | repaymentEvery |
      | NONE           | 360         | DAYS                   | 1              |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "0" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "0" discount amount
    # --- Discount fee added after disbursement ---
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         | 1000.00                    |
      | 1         | 2026-01-02 | 50.00                 | 8959.61         | 990.39                     |
      | 2         | 2026-01-03 | 50.00                 | 8919.18         | 980.82                     |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 0.00                       |
    And The retrieved amortization schedule has exactly 201 payment rows

  @TestRailId:C102491
  Scenario: Verify amortization schedule & EIR math - UC6: discount fee adjustment, daily NPV 360
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with the following matrix data:
      | accountingRule | npvDayCount | repaymentFrequencyType | repaymentEvery |
      | NONE           | 360         | DAYS                   | 1              |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "0" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "0" discount amount
    # --- Discount fee ---
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    # --- Discount fee adjustment on 02 January 2026 ---
    When Admin sets the business date to "02 January 2026"
    And Admin adds Discount fee adjustment with "500" amount on transaction date "02 January 2026" on Working Capital loan account for last discount
    # --- Amortization schedule verification after adjustment ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 500.00            | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 190                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         | 500.00                     |
      | 1         | 2026-01-02 | 50.00                 | 8955.14         | 494.86                     |
      | 2         | 2026-01-03 | 50.00                 | 8910.26         | 489.74                     |
      | 190       | 2026-07-10 | 50.00                 | 0.00            | 0.00                       |
    And The retrieved amortization schedule has exactly 191 payment rows

  @TestRailId:C102548
  Scenario: Verify amortization schedule & EIR math - UC7: advanced accounting with payment-channel fund-source mapping
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    # --- Repayment ---
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan with the following payment details:
      | paymentType    | accountNumber | checkNumber | routingCode | receiptNumber | bankNumber |
      | MONEY_TRANSFER |               |             |             |               |            |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Journal entries verification ---
    And Working Capital Loan Transactions tab has a "DISCOUNT_FEE" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
    And Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 9.61  |        |
      | INCOME    | 404000       | Interest Income           |       | 9.61   |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "02 January 2026" which has the following Journal entries:
      | Type  | Account code | Account name     | Debit | Credit |
      | ASSET | 987654       | Fund Receivables | 50.0  |        |
      | ASSET | 112601       | Loans Receivable |       | 50.0   |
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8959.61         | 9.61                       | 990.39                     | 50.00               | 8959.61       | 9.61                     | 990.39                   |
      | 2         | 2026-01-03 | 50.00                 | 8919.18         | 9.57                       | 980.82                     |                     |               |                          |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 0.05                       | 0.00                       |                     |               |                          |                          |
    And The retrieved amortization schedule has exactly 201 payment rows
    And The retrieved amortization schedule actual amortization is consistent with the loan realized and unrealized income after close of business


  # =============================================================================
  # Scope 2: Non-multiple payments
  # =============================================================================

  @TestRailId:C102492
  Scenario: Verify non-multiple payments - UC1: daily loan, payment not equal to scheduled daily amount
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

  # =============================================================================
  # Scope 3: Overpayment to a few cents
  # =============================================================================

  @TestRailId:C102494
  Scenario: Verify overpayment to a few cents - UC1: daily loan, pay a few cents more than total due
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    # --- Repayment ---
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 10000.05 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance payload contains the following fields:
      | field                | value |
      | principalOutstanding | 0.0   |
      | overpaymentAmount    | 0.05  |
    # --- Amortization schedule verification ---
    When Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 1000.00                    | 9000.00       | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8959.61         | 1000.00                    | 10000.00            | 1000.00                  | 0.00                       | 0.00          | 0.00                     |
    And The retrieved amortization schedule has exactly 2 payment rows
    And The retrieved amortization schedule actual amortization total is "1000.00"
    And The retrieved amortization schedule actual payments plus future expected payments total "10000.00"
    And The retrieved amortization schedule has no negative monetary amounts

  # =============================================================================
  # Scope 4: Last-period tail
  # =============================================================================

  @TestRailId:C102495
  Scenario: Verify last-period tail - UC1: daily loan, final payment is the smaller remainder
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 17                | 1000     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then The last projected payment amount is the smaller tail remainder
    And The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 17                | 360         | 47.22               | 212                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         | 1000.00                    |
      | 1         | 2026-01-02 | 47.22                 | 8961.86         | 990.92                     |
      | 2         | 2026-01-03 | 47.22                 | 8923.68         | 981.88                     |
      | 211       | 2026-07-31 | 47.22                 | 36.54           | 0.04                       |
      | 212       | 2026-08-01 | 36.58                 | 0.00            | 0.00                       |
    And The retrieved amortization schedule has exactly 213 payment rows

  # =============================================================================
  # Scope 5: 1-day term
  # =============================================================================

  @TestRailId:C102497
  Scenario: Verify 1-day term - UC1: daily loan with a single projected instalment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with the following matrix data:
      | accountingRule | npvDayCount | repaymentFrequencyType | repaymentEvery |
      | NONE           | 360         | DAYS                   | 1              |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 100             | 36000              | 100               | 0        |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "0" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "0" discount amount
    # --- Amortization schedule verification ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 0.00              | 100.00                | 36000.00           | 100               | 360         | 100.00                | 1                     |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedDiscountFeeBalance |
      | 0         | 2026-01-01 | -100.00               | 100.00          | 0.00                       |
      | 1         | 2026-01-02 | 100.00                | 0.00            | 0.00                       |
    And The retrieved amortization schedule has no negative monetary amounts
    And The retrieved amortization schedule has exactly 2 payment rows

  @TestRailId:C102493
  Scenario: Verify 1-day term - UC2: daily loan repaid in full on the due date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with the following matrix data:
      | accountingRule | npvDayCount | repaymentFrequencyType | repaymentEvery |
      | NONE           | 360         | DAYS                   | 1              |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 100             | 36000              | 100               | 0        |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "0" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "0" discount amount
    # --- Projected schedule before repayment ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 0.00              | 100.00                | 36000.00           | 100               | 360         | 100.00                | 1                     |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedDiscountFeeBalance |
      | 0         | 2026-01-01 | -100.00               | 100.00          | 0.00                       |
      | 1         | 2026-01-02 | 100.00                | 0.00            | 0.00                       |
    And The retrieved amortization schedule has exactly 2 payment rows
    # --- Repayment on the due date ---
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 100 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    # --- Schedule after full repayment ---
    When Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 0.00              | 100.00                | 36000.00           | 100               | 360         | 100.00                | 1                     |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -100.00               | 100.00          |                            |                     |                          | 0.00                       | 100.00        | 0.00                     |
      | 1         | 2026-01-02 | 100.00                | 0.00            | 0.00                       | 100.00              | 0.00                     | 0.00                       | 0.00          | 0.00                     |
    And The retrieved amortization schedule has no negative monetary amounts
    And The retrieved amortization schedule has exactly 2 payment rows

  @TestRailId:C102496
  Scenario: Verify 1-day term - UC3: daily loan partially repaid on the due date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with the following matrix data:
      | accountingRule | npvDayCount | repaymentFrequencyType | repaymentEvery |
      | NONE           | 360         | DAYS                   | 1              |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 100             | 36000              | 100               | 0        |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "0" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "0" discount amount
    # --- Projected schedule before repayment ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 0.00              | 100.00                | 36000.00           | 100               | 360         | 100.00                | 1                     |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedDiscountFeeBalance |
      | 0         | 2026-01-01 | -100.00               | 100.00          | 0.00                       |
      | 1         | 2026-01-02 | 100.00                | 0.00            | 0.00                       |
    And The retrieved amortization schedule has exactly 2 payment rows
    # --- Partial repayment on the due date ---
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    # --- Schedule after partial repayment ---
    When Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 0.00              | 100.00                | 36000.00           | 100               | 360         | 100.00                | 1                     |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -100.00               | 100.00          |                            |                     |                          | 0.00                       | 100.00        | 0.00                     |
      | 1         | 2026-01-02 | 100.00                | 0.00            | 0.00                       | 50.00               | 0.00                     | 0.00                       | 50.00         | 0.00                     |
      | 2         | 2026-01-03 | 50.00                 | 0.00            | 0.00                       |                     |                          | 0.00                       |               |                          |
    And The retrieved amortization schedule has no negative monetary amounts
    And The retrieved amortization schedule has exactly 3 payment rows