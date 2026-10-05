@WorkingCapital
@WorkingCapitalPaymentAmountCalculationStrategyPaymentAmountFeature
Feature: Working Capital Payment Amount Calculation Strategy - Payment Amount

  Background:
    Given Global configuration "enable-business-date" is enabled

  @TestRailId:C106746
  Scenario: Verify WC LP payment amount calculation strategy (Payment amount) - UC1: Daily payment and Annual EIR derived from the reference example
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with Payment Amount strategy, paymentAmount "47.22" and discount "1000"
    And Admin creates a working capital loan with payment amount using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | paymentAmount | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 47.22         | 1000     |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    Then Working Capital loan status will be "ACTIVE"
    # The strategy and its input are disclosed on the loan, the schedule and the loan account business events.
    Then Working capital loan details has the following field values:
      | paymentAmountCalculationStrategy.id | PAYMENT_AMOUNT |
      | paymentAmount                       | 47.22          |
      | annualEir                           | null           |
    And Working Capital loan account event schema contains field "paymentAmountCalculationStrategy"
    And Working Capital loan account event schema contains field "paymentAmount"
    And Working Capital loan account event schema contains field "annualEir"
    And a Working Capital Loan Status Changed business event carries the same payment amount calculation strategy and inputs as loan details
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | expectedPaymentAmount | originalPaymentNumber | paymentAmountCalculationStrategy | paymentAmount |
      | 1000.00           | 9000.00               | 47.22                 | 212                   | PAYMENT_AMOUNT                   | 47.22         |
    # Reference example: net 9000 + fee 1000, payment 47.22 -> 211 x 47.22 + a 36.58 final payment.
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |
      | 1         | 2026-01-02 | 47.22                 | 8961.86         | 9.08                       | 990.92                     |
      | 2         | 2026-01-03 | 47.22                 | 8923.68         | 9.04                       | 981.88                     |
      | 3         | 2026-01-04 | 47.22                 | 8885.46         | 9.00                       | 972.88                     |
      | 210       | 2026-07-30 | 47.22                 | 83.68           | 0.13                       | 0.12                       |
      | 211       | 2026-07-31 | 47.22                 | 36.54           | 0.08                       | 0.04                       |
      | 212       | 2026-08-01 | 36.58                 | 0.00            | 0.04                       | 0.00                       |
    And The retrieved amortization schedule has no negative amounts
    And The retrieved amortization schedule expected amortization sums to the discount fee and both expected balances close to zero
    # Annual EIR is derived from the cash flows via IRR, not provided - matches the reference example.
    Then Working capital loan details has annual EIR "43.756245"
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C106747
  Scenario: Verify WC LP payment amount calculation strategy (Payment amount) - UC2: Payment amount schedule divides evenly with no remainder
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with Payment Amount strategy, paymentAmount "50.00" and discount "1000"
    And Admin creates a working capital loan with payment amount using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | paymentAmount | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 50.00         | 1000     |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 50.00                 | 200                   |
    # 10000 / 50.00 = 200 exactly, so no extra closing flow is emitted and the last payment is still a full one.
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |
      | 1         | 2026-01-02 | 50.00                 | 8959.61         | 9.61                       | 990.39                     |
      | 199       | 2026-07-19 | 50.00                 | 49.95           | 0.11                       | 0.05                       |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 0.05                       | 0.00                       |
    And The retrieved amortization schedule has no negative amounts
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C106748
  Scenario Outline: Verify WC LP payment amount calculation strategy (Payment amount) - UC3: Product validations for payment amount and discount fee (Negative)
    When Admin sets the business date to "01 January 2026"
    Then Admin creates a Working Capital Loan Product with the following payment amount strategy data expecting error:
      | paymentAmountCalculationStrategy | paymentAmount   | discount   | httpCode   | errorMessage   |
      | PAYMENT_AMOUNT                   | <paymentAmount> | <discount> | <httpCode> | <errorMessage> |

    Examples:
      | paymentAmount | discount | httpCode | errorMessage                                                                         |
      | 0             | 1000     | 400      | The parameter `paymentAmount` must be greater than 0.                                |
      | -1            | 1000     | 400      | The parameter `paymentAmount` must be greater than 0.                                |
      | 47.225        | 1000     | 400      | decimal place must not be more than 2 places                                         |
      |               | 1000     | 400      | The parameter `paymentAmount` is mandatory.                                          |
      | 47.22         | 0        | 400      | Failed data validation due to: must.be.greater.than.zero.for.payment.amount.strategy |
      | 47.22         |          | 400      | Failed data validation due to: must.be.greater.than.zero.for.payment.amount.strategy |

  @TestRailId:C106749
  Scenario Outline: Verify WC LP payment amount calculation strategy (Payment amount) - UC4: Product rejects fields not allowed for the selected strategy (Negative)
    When Admin sets the business date to "01 January 2026"
    Then Admin creates a Working Capital Loan Product with the following payment amount strategy data expecting error:
      | paymentAmountCalculationStrategy | paymentAmount   | discount | periodPaymentRate   | annualEir   | httpCode   | errorMessage   |
      | <strategy>                       | <paymentAmount> | 1000     | <periodPaymentRate> | <annualEir> | <httpCode> | <errorMessage> |

    Examples:
      | strategy       | paymentAmount | periodPaymentRate | annualEir | httpCode | errorMessage                                                           |
      | PAYMENT_AMOUNT | 47.22         | 18                |           | 400      | Failed data validation due to: not.allowed.for.payment.amount.strategy |
      | PAYMENT_AMOUNT | 47.22         |                   | 43.7562   | 400      | Failed data validation due to: not.allowed.for.payment.amount.strategy |
      | TPV            | 47.22         |                   |           | 400      | Failed data validation due to: not.allowed.for.tpv.strategy            |
      | ANNUAL_EIR     | 47.22         |                   | 43.7562   | 400      | Failed data validation due to: not.allowed.for.annual.eir.strategy     |

  @TestRailId:C106750
  Scenario Outline: Verify WC LP payment amount calculation strategy (Payment amount) - UC5: Loan application rejects fields not allowed for the payment amount strategy (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with Payment Amount strategy, paymentAmount "47.22" and discount "1000"
    Then Admin creates a working capital loan with payment amount using created product with the following data expecting error:
      | submittedOnDate | expectedDisbursementDate | principalAmount | paymentAmount   | discount | totalPaymentVolume   | periodPaymentRate   | annualEir   | httpCode   | errorMessage   |
      | 01 January 2026 | 01 January 2026          | 9000            | <paymentAmount> | 1000     | <totalPaymentVolume> | <periodPaymentRate> | <annualEir> | <httpCode> | <errorMessage> |

    Examples:
      | paymentAmount | totalPaymentVolume | periodPaymentRate | annualEir | httpCode | errorMessage                                                                                             |
      | 47.22         | 10500              |                   |           | 400      | Failed data validation due to: not.allowed.for.payment.amount.strategy                                   |
      | 47.22         |                    | 18                |           | 400      | Failed data validation due to: not.allowed.for.payment.amount.strategy                                   |
      | 47.22         |                    |                   | 43.7562   | 400      | Failed data validation due to: not.allowed.for.payment.amount.strategy                                   |
      | 0             |                    |                   |           | 400      | The parameter `paymentAmount` must be greater than 0.                                                    |
      | 47.225        |                    |                   |           | 400      | decimal place must not be more than 2 places                                                             |
      # 0.05/day on a 10000 gross payable would take 200000 days to close - above the 100000-day calculable cap.
      | 0.05          |                    |                   |           | 400      | Please check the input values - unable to build a repayment schedule from the configured payment amount. |

  @TestRailId:C106751
  Scenario: Verify WC LP payment amount calculation strategy (Payment amount) - UC6: Loan-level payment amount overrides the product default
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with Payment Amount strategy, paymentAmount "47.22" and discount "1000"
    And Admin creates a working capital loan with payment amount using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | paymentAmount | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 50.00         | 1000     |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    Then Working Capital loan status will be "ACTIVE"
    # The loan-level 50.00 must win over the product's 47.22 default and reschedule to 200 days (was 212).
    Then Working capital loan details has the following field values:
      | periodPaymentAmount | 50.0 |
      | numberOfRepayments  | 200  |
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 50.00                 | 200                   |
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |
      | 1         | 2026-01-02 | 50.00                 | 8959.61         | 9.61                       | 990.39                     |
      | 199       | 2026-07-19 | 50.00                 | 49.95           | 0.11                       | 0.05                       |
      | 200       | 2026-07-20 | 50.00                 | 0.00            | 0.05                       | 0.00                       |
    And The retrieved amortization schedule has no negative amounts
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C106752
  Scenario: Verify WC LP payment amount calculation strategy (Payment amount) - UC7: Period payment rate change is rejected on a payment amount loan (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with Payment Amount strategy, paymentAmount "47.22" and discount "1000"
    And Admin creates a working capital loan with payment amount using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | paymentAmount | discount |
      | 01 January 2026 | 01 January 2026          | 9000            |               | 1000     |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Admin update Working Capital period payment rate with "20" value expecting error:
      | httpCode | errorMessage                                                                       |
      | 400      | Failed data validation due to: rate.change.not.allowed.for.payment.amount.strategy |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C106753
  Scenario Outline: Verify WC LP payment amount calculation strategy (Payment amount) - UC8: Product payment amount must lie inside configured min/max bounds (Negative)
    When Admin sets the business date to "01 January 2026"
    Then Admin creates a Working Capital Loan Product with the following payment amount strategy data expecting error:
      | paymentAmountCalculationStrategy | paymentAmount   | discount | minPaymentAmount   | maxPaymentAmount   | httpCode   | errorMessage   |
      | PAYMENT_AMOUNT                   | <paymentAmount> | 1000     | <minPaymentAmount> | <maxPaymentAmount> | <httpCode> | <errorMessage> |

    Examples:
      | paymentAmount | minPaymentAmount | maxPaymentAmount | httpCode | errorMessage                                                        |
      | 39.99         | 40.00            | 60.00            | 400      | Failed data validation due to: must.be.greater.than.or.equal.to.min |
      | 60.01         | 40.00            | 60.00            | 400      | Failed data validation due to: must.be.less.than.or.equal.to.max    |

  @TestRailId:C106754
  Scenario: Verify WC LP payment amount calculation strategy (Payment amount) - UC9: Loan-level payment amount must lie inside the product's configured range
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with Payment Amount strategy, paymentAmount "47.22", discount "1000", minPaymentAmount "40.00" and maxPaymentAmount "60.00"
    Then Admin creates a working capital loan with payment amount using created product with the following data expecting error:
      | submittedOnDate | expectedDisbursementDate | principalAmount | paymentAmount | discount | httpCode | errorMessage                                                        |
      | 01 January 2026 | 01 January 2026          | 9000            | 39.99         | 1000     | 400      | Failed data validation due to: must.be.greater.than.or.equal.to.min |
    And Admin creates a working capital loan with payment amount using created product with the following data expecting error:
      | submittedOnDate | expectedDisbursementDate | principalAmount | paymentAmount | discount | httpCode | errorMessage                                                     |
      | 01 January 2026 | 01 January 2026          | 9000            | 60.01         | 1000     | 400      | Failed data validation due to: must.be.less.than.or.equal.to.max |
    And Admin creates a working capital loan with payment amount using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | paymentAmount | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 50.00         | 1000     |
    Then Working capital loan creation was successful

  @TestRailId:C106755
  Scenario: Verify WC LP payment amount calculation strategy (Payment amount) - UC10: A monetary transaction re-projects the term but never changes the configured payment amount
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with Payment Amount strategy, paymentAmount "47.22" and discount "1000"
    And Admin creates a working capital loan with payment amount using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | paymentAmount | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 47.22         | 1000     |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    Then Working Capital loan status will be "ACTIVE"
    # A large overpayment relative to the daily 47.22 diverges reality from the plan and forces a mid-life re-solve
    # of the term/EIR (AmortizationWalk's projectionStale branch), but the walk re-solves from the SAME fixed daily
    # payment it was written with - a monetary transaction re-projects the term, never the configured payment amount.
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 500 transaction amount on Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment    | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
    Then Working capital loan details has the following field values:
      | periodPaymentAmount | 47.22 |
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | expectedPaymentAmount | originalPaymentNumber |
      | 1000.00           | 9000.00               | 47.22                 | 212                   |
    # Day 1 is untouched (identical to UC1's day 1): a settled day's own "expected" row is computed from the plan
    # before the payment lands on it. From day 2 the walk re-solves from the real position, so the loan now closes
    # in 203 days total (1 settled + 202 re-solved) instead of 212, on a smaller closing payment.
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | actualPaymentAmount | actualAmortizationAmount | expectedDiscountFeeBalance | actualBalance | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            |                     |                          | 1000.00                    | 9000.00       | 1000.00                  |
      | 1         | 2026-01-02 | 47.22                 | 8961.86         | 9.08                       | 500.00              | 94.16                    | 990.92                     | 8594.16       | 905.84                   |
      | 202       | 2026-07-22 | 47.22                 | 8.77            | 0.06                       |                     |                          | 0.01                       |               |                          |
      | 203       | 2026-07-23 | 8.78                  | 0.00            | 0.01                       |                     |                          | 0.00                       |               |                          |
    And The retrieved amortization schedule has no negative amounts
    Then Admin closes the Working Capital loan with a full repayment on "02 January 2026"
