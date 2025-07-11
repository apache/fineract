Feature: MerchantIssuedRefund

  @TestRailId:C3731
  Scenario: Merchant Issued Refund reverse replayed with penalty charge and interest recalculation
    When Admin sets the business date to "22 April 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 22 April 2025     | 187.99         | 11.3                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "22 April 2025" with "187.99" amount and expected disbursement date on "22 April 2025"
    When Admin successfully disburse the loan on "22 April 2025" with "187.99" EUR transaction amount
    When Admin sets the business date to "29 April 2025"
    When Customer makes "REPAYMENT" transaction with "REAL_TIME" payment type on "29 April 2025" with 12 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "22 May 2025"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "22 May 2025" with 63.85 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "28 May 2025"
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "28 May 2025" with 187.99 EUR transaction amount and system-generated Idempotency key
    When Customer undo "2"th repayment on "28 May 2025"
    When Admin adds "LOAN_NSF_FEE" due date charge with "28 May 2025" due date and 2.80 EUR transaction amount
    Then Loan status will be "OVERPAID"
    And Loan has 9.2 overpaid amount
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |  Reverted | Replayed |
      | 22 April 2025    | Disbursement           | 187.99 | 0.0       | 0.0      | 0.0  | 0.0       | 187.99       | false     | false    |
      | 29 April 2025    | Repayment              | 12.0   | 11.59     | 0.41     | 0.0  | 0.0       | 176.4        | false     | false    |
      | 22 May 2025      | Repayment              | 63.85  | 62.57     | 1.28     | 0.0  | 0.0       | 113.83       | true      | false    |
      | 22 May 2025      | Accrual Activity       | 1.69   | 0.0       | 1.69     | 0.0  | 0.0       | 0.0          | false     | false    |
      | 28 May 2025      | Accrual                | 1.9    | 0.0       | 1.9      | 0.0  | 0.0       | 0.0          | false     | false    |
      | 28 May 2025      | Interest Refund        | 2.01   | 0.0       | 0.0      | 0.0  | 0.0       | 176.4        | false     | true     |
      | 28 May 2025      | Accrual                | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false     | false    |
      | 28 May 2025      | Merchant Issued Refund | 187.99 | 176.4     | 1.6      | 0.0  | 2.8       | 0.0          | false     | true     |
      | 28 May 2025      | Accrual Activity       | 3.12   | 0.0       | 0.32     | 0.0  | 2.8       | 0.0          | false     | true     |
      | 28 May 2025      | Accrual                | 2.8    | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          | false     | false    |

  @TestRailId:C3774
  Scenario: Verify that the MIR works correctly when last installment principal got updated to null - 360/30
    When Admin sets the business date to "09 November 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_INT_RECALCULATION_ZERO_INT_CHARGE_OFF_INT_RECOGNITION_FROM_DISB_DATE" loan product "MERCHANT_ISSUED_REFUND" transaction type to "LAST_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INT_RECALCULATION_ZERO_INT_CHARGE_OFF_INT_RECOGNITION_FROM_DISB_DATE | 09 November 2024  | 600            | 11.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 24                | MONTHS                | 1              | MONTHS                 | 24                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "09 November 2024" with "600" amount and expected disbursement date on "09 November 2024"
    And Admin successfully disburse the loan on "09 November 2024" with "600" EUR transaction amount
    And Admin sets the business date to "09 June 2025"
    And Customer makes "AUTOPAY" repayment on "09 June 2025" with 10 EUR transaction amount
    And Admin sets the business date to "10 June 2025"
    And Admin does charge-off the loan on "10 June 2025"
    And Customer makes "AUTOPAY" repayment on "09 June 2025" with 187.68 EUR transaction amount
    Then Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 June 2025" with 100 EUR transaction amount and system-generated Idempotency key
    And Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_INT_RECALCULATION_ZERO_INT_CHARGE_OFF_INT_RECOGNITION_FROM_DISB_DATE" loan product "MERCHANT_ISSUED_REFUND" transaction type to "NEXT_INSTALLMENT" future installment allocation rule

  @TestRailId:C3775
  Scenario: Verify that the MIR works correctly when last installment principal got updated to null - Actual/Actual
    When Admin sets the business date to "09 November 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_INT_REFUND_FULL_ZERO_INT_CHARGE_OFF" loan product "MERCHANT_ISSUED_REFUND" transaction type to "LAST_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_INT_REFUND_FULL_ZERO_INT_CHARGE_OFF | 09 November 2024  | 600            | 11.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 24                | MONTHS                | 1              | MONTHS                 | 24                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "09 November 2024" with "600" amount and expected disbursement date on "09 November 2024"
    And Admin successfully disburse the loan on "09 November 2024" with "600" EUR transaction amount
    And Admin sets the business date to "09 June 2025"
    And Customer makes "AUTOPAY" repayment on "09 June 2025" with 10 EUR transaction amount
    And Admin sets the business date to "10 June 2025"
    And Admin does charge-off the loan on "10 June 2025"
    And Customer makes "AUTOPAY" repayment on "09 June 2025" with 187.68 EUR transaction amount
    Then Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 June 2025" with 100 EUR transaction amount and system-generated Idempotency key
    And Admin set "LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_INT_REFUND_FULL_ZERO_INT_CHARGE_OFF" loan product "MERCHANT_ISSUED_REFUND" transaction type to "NEXT_INSTALLMENT" future installment allocation rule

  @TestRailId:C3842
  Scenario: Merchant Issued Refund with interestRefundCalculation = false (Interest Refund transaction should NOT be created)
    When Admin sets the business date to "01 July 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 July 2024      | 1000           | 10                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2024" with "1000" amount and expected disbursement date on "01 July 2024"
    And Admin successfully disburse the loan on "01 July 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2024    |           | 669.43          | 330.57        | 8.33     | 0.0  | 0.0       | 338.9  | 0.0  | 0.0        | 0.0  | 338.9       |
      | 2  | 31   | 01 September 2024 |           | 336.11          | 333.32        | 5.58     | 0.0  | 0.0       | 338.9  | 0.0  | 0.0        | 0.0  | 338.9       |
      | 3  | 30   | 01 October 2024   |           | 0.0             | 336.11        | 2.8      | 0.0  | 0.0       | 338.91 | 0.0  | 0.0        | 0.0  | 338.91      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 16.71    | 0.0  | 0.0       | 1016.71 | 0.0  | 0.0        | 0.0  | 1016.71     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 July 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "10 July 2024"
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "10 July 2024" with 338.9 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "15 July 2024"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 July 2024" with 50 EUR transaction amount and system-generated Idempotency key and interestRefundCalculation false
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 July 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 August 2024    | 10 July 2024 | 663.52          | 336.48        | 2.42     | 0.0  | 0.0       | 338.9  | 338.9 | 338.9      | 0.0  | 0.0         |
      | 2  | 31   | 01 September 2024 |              | 333.42          | 330.1         | 8.8      | 0.0  | 0.0       | 338.9  | 0.0   | 0.0        | 0.0  | 338.9       |
      | 3  | 30   | 01 October 2024   |              | 0.0             | 333.42        | 2.36     | 0.0  | 0.0       | 335.78 | 50.0  | 50.0       | 0.0  | 285.78      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 13.58    | 0.0  | 0.0       | 1013.58 | 388.9  | 388.9      | 0.0  | 624.68      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 July 2024     | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 July 2024     | Repayment              | 338.9  | 336.48    | 2.42     | 0.0  | 0.0       | 663.52       | false    | false    |
      | 15 July 2024     | Merchant Issued Refund | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 613.52       | false    | false    |

  @TestRailId:C3843
  Scenario: Merchant Issued Refund with interestRefundCalculation = true (Interest Refund transaction SHOULD be created)
    When Admin sets the business date to "01 July 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 July 2024      | 1000           | 10                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2024" with "1000" amount and expected disbursement date on "01 July 2024"
    And Admin successfully disburse the loan on "01 July 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2024    |           | 669.43          | 330.57        | 8.33     | 0.0  | 0.0       | 338.9  | 0.0  | 0.0        | 0.0  | 338.9       |
      | 2  | 31   | 01 September 2024 |           | 336.11          | 333.32        | 5.58     | 0.0  | 0.0       | 338.9  | 0.0  | 0.0        | 0.0  | 338.9       |
      | 3  | 30   | 01 October 2024   |           | 0.0             | 336.11        | 2.8      | 0.0  | 0.0       | 338.91 | 0.0  | 0.0        | 0.0  | 338.91      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 16.71    | 0.0  | 0.0       | 1016.71 | 0.0  | 0.0        | 0.0  | 1016.71     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 July 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "10 July 2024"
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "10 July 2024" with 338.9 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "15 July 2024"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 July 2024" with 50 EUR transaction amount and system-generated Idempotency key and interestRefundCalculation true
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 July 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 August 2024    | 10 July 2024 | 663.52          | 336.48        | 2.42     | 0.0  | 0.0       | 338.9  | 338.9 | 338.9      | 0.0  | 0.0         |
      | 2  | 31   | 01 September 2024 |              | 333.42          | 330.1         | 8.8      | 0.0  | 0.0       | 338.9  | 0.19  | 0.19       | 0.0  | 338.71      |
      | 3  | 30   | 01 October 2024   |              | 0.0             | 333.42        | 2.36     | 0.0  | 0.0       | 335.78 | 50.0  | 50.0       | 0.0  | 285.78      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 13.58    | 0.0  | 0.0       | 1013.58 | 389.09 | 389.09     | 0.0  | 624.49      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 July 2024     | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 July 2024     | Repayment              | 338.9  | 336.48    | 2.42     | 0.0  | 0.0       | 663.52       | false    | false    |
      | 15 July 2024     | Merchant Issued Refund | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 613.52       | false    | false    |
      | 15 July 2024     | Interest Refund        | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 613.52       | false    | false    |

  @TestRailId:C3844
  Scenario: Merchant Issued Refund without interestRefundCalculation (should fallback to loan product config)
    When Admin sets the business date to "01 July 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 July 2024      | 1000           | 10                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2024" with "1000" amount and expected disbursement date on "01 July 2024"
    And Admin successfully disburse the loan on "01 July 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2024    |           | 669.43          | 330.57        | 8.33     | 0.0  | 0.0       | 338.9  | 0.0  | 0.0        | 0.0  | 338.9       |
      | 2  | 31   | 01 September 2024 |           | 336.11          | 333.32        | 5.58     | 0.0  | 0.0       | 338.9  | 0.0  | 0.0        | 0.0  | 338.9       |
      | 3  | 30   | 01 October 2024   |           | 0.0             | 336.11        | 2.8      | 0.0  | 0.0       | 338.91 | 0.0  | 0.0        | 0.0  | 338.91      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 16.71    | 0.0  | 0.0       | 1016.71 | 0.0  | 0.0        | 0.0  | 1016.71     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 July 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "10 July 2024"
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "10 July 2024" with 338.9 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "15 July 2024"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 July 2024" with 50 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 July 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 August 2024    | 10 July 2024 | 663.52          | 336.48        | 2.42     | 0.0  | 0.0       | 338.9  | 338.9 | 338.9      | 0.0  | 0.0         |
      | 2  | 31   | 01 September 2024 |              | 333.42          | 330.1         | 8.8      | 0.0  | 0.0       | 338.9  | 0.19  | 0.19       | 0.0  | 338.71      |
      | 3  | 30   | 01 October 2024   |              | 0.0             | 333.42        | 2.36     | 0.0  | 0.0       | 335.78 | 50.0  | 50.0       | 0.0  | 285.78      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 13.58    | 0.0  | 0.0       | 1013.58 | 389.09 | 389.09     | 0.0  | 624.49      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 July 2024     | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 July 2024     | Repayment              | 338.9  | 336.48    | 2.42     | 0.0  | 0.0       | 663.52       | false    | false    |
      | 15 July 2024     | Merchant Issued Refund | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 613.52       | false    | false    |
      | 15 July 2024     | Interest Refund        | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 613.52       | false    | false    |
