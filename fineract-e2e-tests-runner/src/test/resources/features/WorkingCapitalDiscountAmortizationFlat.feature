@WorkingCapital
@WorkingCapitalDiscountFeeAmortizationFlatFeature
Feature: WorkingCapitalDiscountFeeAmortizationFlat

# FLAT amortization: ratio = (discountFee - adjustment) / (netDisbursement + discountFee - adjustment).
# Reference loan: 9000 disbursed, 1000 discount fee -> ratio 10%; 100000 payment volume at 18% over 360 days bills
# 50 a day, so a 10000 gross runs 200 periods and every period earns 10% x 50 = 5.00.

  @TestRailId:C106662
  Scenario: Verify Working Capital Flat amortization - UC1: loan inherits FLAT type and projected schedule follows the flat ratio
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_FLAT_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    # --- Discount fee ---
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name                  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_FLAT_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0           | 18.0              | 1000.0   |
    And Working capital loan details has the following field values:
      | amortizationType.id | FLAT |
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 100000.00          | 18.00             | 50.00                 | 200                   |
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4500.00         | 5.00                       | 500.00                     |                     |               |                          |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 5.00                       | 0.00                       |                     |               |                          |                          |
    Then The retrieved amortization schedule expected amortization sums to the discount fee and both expected balances close to zero
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C106663
  Scenario: Verify Working Capital Flat amortization - UC2: a repayment amortizes exactly the flat ratio share and posts matching journal entries
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_FLAT_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    # --- Discount fee ---
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4500.00         | 5.00                       | 500.00                     |                     |               |                          |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 5.00                       | 0.00                       |                     |               |                          |                          |
    # --- Repayment ---
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 5.0               |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           |       | 5.0    |
      | LIABILITY | 240005       | Deferred Interest Revenue | 5.0   |        |
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     | 50.00               | 8955.00       | 5.00                     | 995.00                   |
      | 2         | 2026-01-03 | 50.00                 | 8910.00         | 5.00                       | 990.00                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4500.00         | 5.00                       | 500.00                     |                     |               |                          |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 5.00                       | 0.00                       |                     |               |                          |                          |
    # --- 2nd run WC COB shouldn't generate any more/new amortization
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 5.0               |                  |                   |                       | false    |
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     | 50.00               | 8955.00       | 5.00                     | 995.00                   |
      | 2         | 2026-01-03 | 50.00                 | 8910.00         | 5.00                       | 990.00                     | 0.00                | 8955.00       | 0.00                     | 995.00                   |
      | 3         | 2026-01-04 | 50.00                 | 8910.00         | 5.00                       | 990.00                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4545.00         | 5.00                       | 505.00                     |                     |               |                          |                          |
      | 200       | 2026-07-20 | 50.00                 | 45.00           | 5.00                       | 5.00                       |                     |               |                          |                          |

    Then Admin closes the Working Capital loan with a full repayment on "04 January 2026"

  @TestRailId:C106664
  Scenario: Verify Working Capital Flat amortization - UC3: no Discount Fee Amortization transaction triggers on COB without a repayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_FLAT_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    # --- Discount fee ---
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4500.00         | 5.00                       | 500.00                     |                     |               |                          |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 5.00                       | 0.00                       |                     |               |                          |                          |
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 0.0                | 100000.0           | 0.0            | 1000.0           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
    # --- The single COB run to 08 Jan marks 02-07 Jan (6 days) as elapsed and unpaid, which restates
    # the expected trajectory from that unpaid position instead of the original static plan: every
    # later expected row runs 6 periods behind (6 x 45.00 / 6 x 5.00).
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
      | 6         | 2026-01-07 | 50.00                 | 8955.00         | 5.00                       | 995.00                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
      | 7         | 2026-01-08 | 50.00                 | 8955.00         | 5.00                       | 995.00                     |                     |               |                          |                          |
      | 8         | 2026-01-09 | 50.00                 | 8910.00         | 5.00                       | 990.00                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4770.00         | 5.00                       | 530.00                     |                     |               |                          |                          |
      | 200       | 2026-07-20 | 50.00                 | 270.00          | 5.00                       | 30.00                      |                     |               |                          |                          |
    Then Admin closes the Working Capital loan with a full repayment on "08 January 2026"

  @TestRailId:C106665
  Scenario: Verify Working Capital Flat amortization - UC4: non-round repayments round the cumulative flat amortization once, with no rounding adjustment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_FLAT_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    # --- Discount fee ---
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4500.00         | 5.00                       | 500.00                     |                     |               |                          |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 5.00                       | 0.00                       |                     |               |                          |                          |
    # --- Repayment ---
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    # 10% x 33.33 = 3.333 -> 3.33
    And Customer makes repayment on "02 January 2026" with 33.33 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     | 33.33               | 8970.00       | 3.33                     | 996.67                   |
      # The real 33.33 restates the trajectory from 33.33's own earned ratio (3.33), so every later
      # expected row runs behind the naive plan by the 5.00-3.33 = 1.67 gap that repayment left unearned.
      | 2         | 2026-01-03 | 50.00                 | 8925.00         | 5.00                       | 991.67                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4515.00         | 5.00                       | 501.67                     |                     |               |                          |                          |
      | 200       | 2026-07-20 | 50.00                 | 15.00           | 5.00                       | 1.67                       |                     |               |                          |                          |
    # cumulative 10% x 99.99 = 9.999 -> 10.00 ; second amortization = 10.00 - 3.33
    And Customer makes repayment on "03 January 2026" with 66.66 transaction amount on Working Capital loan
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 33.33             | 33.33            | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 3.33              |                  |                   |                       | false    |
      | 03 January 2026 | Repayment                 | 66.66             | 66.66            | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization | 6.67              |                  |                   |                       | false    |
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     | 33.33               | 8970.00       | 3.33                     | 996.67                   |
      | 2         | 2026-01-03 | 50.00                 | 8925.00         | 5.00                       | 991.67                     | 66.66               | 8910.01       | 6.67                     | 990.00                   |
      | 3         | 2026-01-04 | 50.00                 | 8865.01         | 5.00                       | 985.00                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4500.01         | 5.00                       | 500.00                     |                     |               |                          |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.01            | 5.00                       | 0.00                       |                     |               |                          |                          |
    Then Admin closes the Working Capital loan with a full repayment on "04 January 2026"

  @TestRailId:C106666
  Scenario: Verify Working Capital Flat amortization - UC5: full payoff in uneven instalments amortizes exactly the discount fee
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_FLAT_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    # --- Discount fee ---
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4500.00         | 5.00                       | 500.00                     |                     |               |                          |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 5.00                       | 0.00                       |                     |               |                          |                          |
    # --- Repayment ---
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    # 10% x 3333.33 = 333.333 -> 333.33
    And Customer makes repayment on "02 January 2026" with 3333.33 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    # cumulative 10% x 6666.66 = 666.666 -> 666.67 ; 666.67 - 333.33 = 333.34
    And Customer makes repayment on "03 January 2026" with 3333.33 transaction amount on Working Capital loan
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    # cumulative 10% x 10000 = 1000.00 ; 1000.00 - 666.67 = 333.33 ; the loan is paid in full
    And Customer makes repayment on "04 January 2026" with 3333.34 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 3333.33           | 3333.33          | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 333.33            |                  |                   |                       | false    |
      | 03 January 2026 | Repayment                 | 3333.33           | 3333.33          | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization | 333.34            |                  |                   |                       | false    |
      | 04 January 2026 | Repayment                 | 3333.34           | 3333.34          | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization | 333.33            |                  |                   |                       | false    |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 10000.0            | 1000.0         | 0.0              | 0.0               |
    # --- The schedule ends the day the loan closes (04 Jan, day 3) - there is no payment 100/200 to read
    # anymore. Each real, oversized repayment restates the trajectory from its own earned ratio, so rows
    # 2 and 3 run off the naive per-day plan; row 3 is forced to the full discount fee on close.
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    | 9000.00       | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     | 6000.00       | 666.67                   |
      | 2         | 2026-01-03 | 50.00                 | 5955.00         | 5.00                       | 661.67                     | 3000.01       | 333.33                   |
      | 3         | 2026-01-04 | 50.00                 | 2955.01         | 333.33                     | 0.00                       | 0.00          | 0.00                     |

  @TestRailId:C106667
  Scenario: Verify Working Capital Flat amortization - UC6: a discount fee adjustment decreases the ratio and restates already-earned amortization
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_FLAT_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    # --- Discount fee ---
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4500.00         | 5.00                       | 500.00                     |                     |               |                          |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 5.00                       | 0.00                       |                     |               |                          |                          |
    # --- Repayment ---
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 5.0               |                  |                   |                       | false    |
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     | 50.00               | 8955.00       | 5.00                     | 995.00                   |
      | 2         | 2026-01-03 | 50.00                 | 8910.00         | 5.00                       | 990.00                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4500.00         | 5.00                       | 500.00                     |                     |               |                          |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 5.00                       | 0.00                       |                     |               |                          |                          |
    # --- Discount fee adjustment --- payable fee 1000-500=500 ; initial balance 9000+500=9500 ; ratio 500/9500 = 5.2632%
    # restated day-2 amortization = 5.2632% x 50 = 2.63 ; adjustment posted = 5.00 - 2.63 = 2.37
    Then Admin adds Discount fee adjustment with "500" amount on transaction date "03 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9500.0    | 50.0               | 5.0            | 495.0            | 0.0               |
    # --- The restated schedule now runs to term 190 (9500/50) on the new 500/9500 ratio ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 500.00                     |                     | 9000.00       |                          | 500.00                   |
      | 1         | 2026-01-02 | 50.00                 | 8952.63         | 2.63                       | 497.37                     | 50.00               | 8952.63       | 2.63                     | 497.37                   |
      | 2         | 2026-01-03 | 50.00                 | 8905.26         | 2.63                       | 494.74                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4263.16         | 2.63                       | 236.84                     |                     |               |                          |                          |
      | 190       | 2026-07-10 | 50.00                 | 0.00            | 2.63                       | 0.00                       |                     |               |                          |                          |
    Then The retrieved amortization schedule expected amortization sums to the discount fee and both expected balances close to zero
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           | 2.37  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 2.37   |
    Then Admin closes the Working Capital loan with a full repayment on "04 January 2026"

  @TestRailId:C106668
  Scenario: Verify Working Capital Flat amortization - UC7: a discount fee adjustment of the whole fee takes the ratio to zero and reverses what was earned
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_FLAT_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    # --- Discount fee ---
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4500.00         | 5.00                       | 500.00                     |                     |               |                          |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 5.00                       | 0.00                       |                     |               |                          |                          |
    # --- Repayment ---
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 5.0            | 995.0            | 0.0               |
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     | 50.00               | 8955.00       | 5.00                     | 995.00                   |
      | 2         | 2026-01-03 | 50.00                 | 8910.00         | 5.00                       | 990.00                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4500.00         | 5.00                       | 500.00                     |                     |               |                          |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 5.00                       | 0.00                       |                     |               |                          |                          |
    # --- Discount fee adjustment ---
    Then Admin adds Discount fee adjustment with "1000" amount on transaction date "03 January 2026" on Working Capital loan account for last discount
    # --- With the ratio at zero, the restated schedule (9000/50=180 periods) earns and defers nothing ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 0.00                       |                     | 9000.00       |                          | 0.00                     |
      | 1         | 2026-01-02 | 50.00                 | 8950.00         | 0.00                       | 0.00                       | 50.00               | 8950.00       | 0.00                     | 0.00                     |
      | 2         | 2026-01-03 | 50.00                 | 8900.00         | 0.00                       | 0.00                       |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4000.00         | 0.00                       | 0.00                       |                     |               |                          |                          |
      | 180       | 2026-06-30 | 50.00                 | 0.00            | 0.00                       | 0.00                       |                     |               |                          |                          |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           | 5.0   |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 5.0    |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 50.0               | 0.0            | 0.0              | 0.0               |
    Then Admin closes the Working Capital loan with a full repayment on "04 January 2026"

  @TestRailId:C106669
  Scenario: Verify Working Capital Flat amortization - UC8: undo of a discount fee adjustment restores the flat ratio and reverses the linked amortization adjustment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_FLAT_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9500            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9500" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9500" EUR transaction amount
    # --- Discount fee ---
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9500.00              | 9500.00         |                            | 1000.00                    |                     | 9500.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 9454.76         | 4.76                       | 995.24                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4976.19         | 4.76                       | 523.81                     |                     |               |                          |                          |
      | 210       | 2026-07-30 | 50.00                 | 0.00            | 4.76                       | 0.00                       |                     |               |                          |                          |
    # --- Repayment ---
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    # ratio 1000/10500 x 50 = 4.7619 -> 4.76
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9500.0            | 9500.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 4.76              |                  |                   |                       | false    |
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9500.00              | 9500.00         |                            | 1000.00                    |                     | 9500.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 9454.76         | 4.76                       | 995.24                     | 50.00               | 9454.76       | 4.76                     | 995.24                   |
      | 2         | 2026-01-03 | 50.00                 | 9409.52         | 4.76                       | 990.48                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4976.19         | 4.76                       | 523.81                     |                     |               |                          |                          |
      | 210       | 2026-07-30 | 50.00                 | 0.00            | 4.76                       | 0.00                       |                     |               |                          |                          |
    # --- Discount fee adjustment ---
    Then Admin adds Discount fee adjustment with "500" amount on transaction date "03 January 2026" on Working Capital loan account for last discount
    # --- The restated schedule now runs on the new 500/10000 ratio, 200 periods (10000/50) ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9500.00              | 9500.00         |                            | 500.00                     |                     | 9500.00       |                          | 500.00                   |
      | 1         | 2026-01-02 | 50.00                 | 9452.50         | 2.50                       | 497.50                     | 50.00               | 9452.50       | 2.50                     | 497.50                   |
      | 2         | 2026-01-03 | 50.00                 | 9405.00         | 2.50                       | 495.00                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4750.00         | 2.50                       | 250.00                     |                     |               |                          |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 2.50                       | 0.00                       |                     |               |                          |                          |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           | 2.26  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 2.26   |
    # --- Undo transaction --- undoing the discount fee adjustment restores the original 1000/10500 ratio
    When Customer undo "1"th "DISCOUNT_FEE_ADJUSTMENT" transaction made on "03 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 2.26   |
      | INCOME    | 404000       | Interest Income           | 2.26  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue | 2.26  |        |
      | INCOME    | 404000       | Interest Income           |       | 2.26   |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10500.0   | 50.0               | 4.76           | 995.24           | 0.0               |
    # --- Undoing the adjustment restores the schedule to the original 1000/10500 ratio and 210-period term ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9500.00              | 9500.00         |                            | 1000.00                    |                     | 9500.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 9454.76         | 4.76                       | 995.24                     | 50.00               | 9454.76       | 4.76                     | 995.24                   |
      | 2         | 2026-01-03 | 50.00                 | 9409.52         | 4.76                       | 990.48                     |                     |               |                          |                          |
      | 3         | 2026-01-04 | 50.00                 | 9364.29         | 4.77                       | 985.71                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4976.19         | 4.76                       | 523.81                     |                     |               |                          |                          |
      | 210       | 2026-07-30 | 50.00                 | 0.00            | 4.76                       | 0.00                       |                     |               |                          |                          |
    Then Admin closes the Working Capital loan with a full repayment on "04 January 2026"

  @TestRailId:C106670
  Scenario: Verify Working Capital Flat amortization - UC9: undo of a repayment reverses its flat amortization via an amortization adjustment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_FLAT_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    # --- Discount fee ---
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4500.00         | 5.00                       | 500.00                     |                     |               |                          |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 5.00                       | 0.00                       |                     |               |                          |                          |
    # --- Repayment ---
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 5.0               |                  |                   |                       | false    |
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     | 50.00               | 8955.00       | 5.00                     | 995.00                   |
      | 2         | 2026-01-03 | 50.00                 | 8910.00         | 5.00                       | 990.00                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4500.00         | 5.00                       | 500.00                     |                     |               |                          |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 5.00                       | 0.00                       |                     |               |                          |                          |
    # --- Undo transaction ---
    When Customer undo "1"th "REPAYMENT" transaction made on "02 January 2026" on Working Capital loan
    # --- Undoing the repayment leaves 02 January as an elapsed zero-payment day, shifting the whole
    # trajectory by one period (the schedule now terminates at payment 201 instead of 200) ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
      | 2         | 2026-01-03 | 50.00                 | 8955.00         | 5.00                       | 995.00                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4545.00         | 5.00                       | 505.00                     |                     |               |                          |                          |
      | 201       | 2026-07-21 | 50.00                 | 0.00            | 5.00                       | 0.00                       |                     |               |                          |                          |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           | 5.0   |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 5.0    |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 0.0                | 0.0            | 1000.0           | 0.0               |
    Then Admin closes the Working Capital loan with a full repayment on "04 January 2026"

  @TestRailId:C106671
  Scenario: Verify Working Capital Flat amortization - UC10: a period payment rate change resizes payments but keeps the flat ratio unchanged
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_FLAT_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    # --- Discount fee ---
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     |                     |               |                          |                          |
      | 100       | 2026-04-11 | 50.00                 | 4500.00         | 5.00                       | 500.00                     |                     |               |                          |                          |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 5.00                       | 0.00                       |                     |               |                          |                          |
    # --- Repayment ---
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    # --- Rate change: 25% -> (100000 x 25%) / 360 = 69.44 a day ; the flat ratio (10%) is unaffected by the rate ---
    And Admin update Working Capital period payment rate with "25" value
    # --- The schedule now recomputes future periods at the new daily payment; the new segment closes at
    # period 145 (1 old-rate period + ceil(9950/69.44)=144 new-rate periods) instead of the original 200 ---
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     | 50.00               | 8955.00       | 5.00                     | 995.00                   |
      | 2         | 2026-01-03 | 69.44                 | 8892.50         | 6.94                       | 988.06                     |                     |               |                          |                          |
      | 145       | 2026-05-26 | 20.08                 | 0.00            | 2.01                       | 0.00                       |                     |               |                          |                          |
    # cumulative 10% x 119.44 = 11.944 -> 11.94 ; 11.94 - 5.00 = 6.94
    And Customer makes repayment on "03 January 2026" with 69.44 transaction amount on Working Capital loan
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 5.0               |                  |                   |                       | false    |
      | 03 January 2026 | Repayment                 | 69.44             | 69.44            | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization | 6.94              |                  |                   |                       | false    |
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8955.00         | 5.00                       | 995.00                     | 50.00               | 8955.00       | 5.00                     | 995.00                   |
      | 2         | 2026-01-03 | 69.44                 | 8892.50         | 6.94                       | 988.06                     | 69.44               | 8892.50       | 6.94                     | 988.06                   |
      | 3         | 2026-01-04 | 69.44                 | 8830.01         | 6.95                       | 981.11                     |                     |               |                          |                          |
      | 145       | 2026-05-26 | 20.08                 | 0.00            | 2.01                       | 0.00                       |                     |               |                          |                          |
    Then Admin closes the Working Capital loan with a full repayment on "04 January 2026"
