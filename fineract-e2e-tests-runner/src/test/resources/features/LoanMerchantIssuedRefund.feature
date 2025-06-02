Feature: MerchantIssuedRefund

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
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 22 April 2025    | Disbursement           | 187.99 | 0.0       | 0.0      | 0.0  | 0.0       | 187.99       |
      | 29 April 2025    | Repayment              | 12.0   | 11.59     | 0.41     | 0.0  | 0.0       | 176.4        |
      | 22 May 2025      | Repayment              | 63.85  | 62.57     | 1.28     | 0.0  | 0.0       | 113.83       |
      | 22 May 2025      | Accrual Activity       | 1.69   | 0.0       | 1.69     | 0.0  | 0.0       | 0.0          |
      | 28 May 2025      | Accrual                | 1.9    | 0.0       | 1.9      | 0.0  | 0.0       | 0.0          |
      | 28 May 2025      | Interest Refund        | 2.01   | 0.0       | 0.0      | 0.0  | 0.0       | 176.4        |
      | 28 May 2025      | Accrual                | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          |
      | 28 May 2025      | Merchant Issued Refund | 187.99 | 176.4     | 1.6      | 0.0  | 2.8       | 0.0          |
      | 28 May 2025      | Accrual Activity       | 3.12   | 0.0       | 0.32     | 0.0  | 2.8       | 0.0          |
      | 28 May 2025      | Accrual                | 2.8    | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          |