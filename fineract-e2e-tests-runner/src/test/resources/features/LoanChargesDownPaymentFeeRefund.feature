@LoanChargesDownPaymentFeeRefundFeature
Feature: LoanChargesDownPaymentFeeRefund

  Scenario: Verify Fee Refund is created on Merchant Issued Refund for flat down payment fee
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 25.0 | 0.0  | 0.0    | 25.0        |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026  | 01 January 2026 | 750.0           | 250.0         | 0.0      | 25.0 | 0.0       | 275.0 | 275.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2026  |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 25.0 | 0.0       | 1025.0 | 275.0 | 0.0        | 0.0  | 750.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 25.0 | 25.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 25.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 275.0 |        |
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 250.0        | false    | false    |
      | 15 January 2026  | Fee Refund             | 12.5   | 12.5      | 0.0      | 0.0  | 0.0       | 237.5        | false    | false    |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026  | 01 January 2026 | 750.0           | 250.0         | 0.0      | 25.0 | 0.0       | 275.0 | 275.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2026  |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 179.17| 179.17     | 0.0  | 70.83       |
      | 3  | 15   | 31 January 2026  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 166.67| 166.67     | 0.0  | 83.33       |
      | 4  | 15   | 15 February 2026 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 166.66| 166.66     | 0.0  | 83.34       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 25.0 | 0.0       | 1025.0 | 787.5  | 512.5      | 0.0  | 237.5       |
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 25.0 | 25.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has a "MERCHANT_ISSUED_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 12.5   |
      | INCOME | 404007       | Fee Income       | 12.5  |        |
    When Loan Pay-off is made on "15 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  Scenario: Verify Fee Refund is NOT created on Merchant Issued Refund when product has no fee refund config
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 250.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 25.0 | 25.0 | 0.0    | 0.0         |
    When Loan Pay-off is made on "15 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  Scenario: Verify Fee Refund is created on Payout Refund for flat down payment fee
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
    When Admin sets the business date to "15 January 2026"
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 15 January 2026  | Payout Refund    | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 250.0        | false    | false    |
      | 15 January 2026  | Fee Refund       | 12.5   | 12.5      | 0.0      | 0.0  | 0.0       | 237.5        | false    | false    |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026  | 01 January 2026 | 750.0           | 250.0         | 0.0      | 25.0 | 0.0       | 275.0 | 275.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2026  | 15 January 2026 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 January 2026  | 15 January 2026 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 February 2026 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 12.5  | 12.5       | 0.0  | 237.5       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 25.0 | 0.0       | 1025.0 | 787.5 | 512.5      | 0.0  | 237.5       |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 12.5   |
      | INCOME | 404007       | Fee Income       | 12.5  |        |
    When Loan Pay-off is made on "15 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  Scenario: Verify multiple refunds create proportional Fee Refunds and the total Fee Refund is capped by the Down Payment fee
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026  | 01 January 2026 | 750.0           | 250.0         | 0.0      | 25.0 | 0.0       | 275.0 | 275.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2026  |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 25.0 | 0.0       | 1025.0 | 275.0 | 0.0        | 0.0  | 750.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
    When Admin sets the business date to "10 January 2026"
    When Admin adds "LOAN_NSF_FEE" due date charge with "10 January 2026" due date and 500.0 EUR transaction amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 January 2026" with 600 EUR transaction amount and system-generated Idempotency key
    # 1st Fee Refund: 600 / 1000 * 25 = 15.0 (Fee Refund credit is allocated to outstanding NSF penalty)
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 10 January 2026  | Merchant Issued Refund | 600.0  | 400.0     | 0.0      | 0.0  | 200.0     | 350.0        | false    | false    |
      | 10 January 2026  | Fee Refund             | 15.0   | 0.0       | 0.0      | 0.0  | 15.0      | 350.0        | false    | false    |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0    |            |      |             |
      | 1  | 0    | 01 January 2026  | 01 January 2026 | 750.0           | 250.0         | 0.0      | 25.0 | 0.0       | 275.0 | 275.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2026  |                 | 500.0           | 250.0         | 0.0      | 0.0  | 500.0     | 750.0 | 348.33 | 348.33     | 0.0  | 401.67      |
      | 3  | 15   | 31 January 2026  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 133.33 | 133.33     | 0.0  | 116.67      |
      | 4  | 15   | 15 February 2026 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 133.34 | 133.34     | 0.0  | 116.66      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 25.0 | 500.0     | 1525.0 | 890.0 | 615.0      | 0.0  | 635.0       |
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 15.0   |
      | INCOME | 404007       | Fee Income              | 15.0  |        |
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 600 EUR transaction amount and system-generated Idempotency key
    # 2nd Fee Refund would be 15.0, but only 25 - 15 = 10.0 remains under the DP-fee cap
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 10 January 2026  | Merchant Issued Refund | 600.0  | 400.0     | 0.0      | 0.0  | 200.0     | 350.0        | false    | false    |
      | 10 January 2026  | Fee Refund             | 15.0   | 0.0       | 0.0      | 0.0  | 15.0      | 350.0        | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 600.0  | 350.0     | 0.0      | 0.0  | 250.0     | 0.0          | false    | false    |
      | 15 January 2026  | Fee Refund             | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          | false    | false    |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026  | 01 January 2026 | 750.0           | 250.0         | 0.0      | 25.0 | 0.0       | 275.0 | 275.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2026  |                 | 500.0           | 250.0         | 0.0      | 0.0  | 500.0     | 750.0 | 725.0 | 725.0      | 0.0  | 25.0        |
      | 3  | 15   | 31 January 2026  | 15 January 2026 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 February 2026 | 15 January 2026 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 25.0 | 500.0     | 1525.0 | 1500.0 | 1225.0     | 0.0  | 25.0        |
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at     | Due as of       | Calculation type | Due   | Paid  | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment       | 01 January 2026 | Flat             | 25.0  | 25.0  | 0.0    | 0.0         |
      | NSF fee          | true      | Specified due date | 10 January 2026 | Flat             | 500.0 | 475.0 | 0.0    | 25.0        |
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 10.0   |
      | INCOME | 404007       | Fee Income              | 10.0  |        |
    When Loan Pay-off is made on "15 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  Scenario: Verify Fee Refund with multi-disburse and multiple Down Payment fees
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "500" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 01 January 2026  | Down Payment     | 150.0  | 125.0     | 0.0      | 25.0 | 0.0       | 375.0        | false    | false    |
    When Admin sets the business date to "02 January 2026"
    When Admin successfully disburse the loan on "02 January 2026" with "500" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "02 January 2026"
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 25.0 | 25.0 | 0.0    | 0.0         |
      | Down Payment Fee | false     | Down Payment   | 02 January 2026 | Flat             | 25.0 | 25.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 01 January 2026  | Down Payment     | 150.0  | 125.0     | 0.0      | 25.0 | 0.0       | 375.0        | false    | false    |
      | 02 January 2026  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 875.0        | false    | false    |
      | 02 January 2026  | Down Payment     | 150.0  | 125.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
    When Admin sets the business date to "10 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 January 2026" with 500 EUR transaction amount and system-generated Idempotency key
    # 500 / 1000 (total disbursed) * (25 + 25) = 25.0
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 01 January 2026  | Down Payment           | 150.0  | 125.0     | 0.0      | 25.0 | 0.0       | 375.0        | false    | false    |
      | 02 January 2026  | Disbursement           | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 875.0        | false    | false    |
      | 02 January 2026  | Down Payment           | 150.0  | 125.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 10 January 2026  | Merchant Issued Refund | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 250.0        | false    | false    |
      | 10 January 2026  | Fee Refund             | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 225.0        | false    | false    |
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 25.0   |
      | INCOME | 404007       | Fee Income       | 25.0  |        |
    When Loan Pay-off is made on "10 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  Scenario: Verify Fee Refund with multiple flat fees on the same Down Payment period
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    When Admin adds "LOAN_DOWN_PAYMENT_FEE" due date charge with "01 January 2026" due date and 15.0 EUR transaction amount
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 25.0 | 0.0  | 0.0    | 25.0        |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 15.0 | 0.0  | 0.0    | 15.0        |
    And Customer makes "DOWN_PAYMENT" repayment on "01 January 2026" with 290 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Repayment        | 290.0  | 250.0     | 0.0      | 40.0 | 0.0       | 750.0        |
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 25.0 | 25.0 | 0.0    | 0.0         |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 15.0 | 15.0 | 0.0    | 0.0         |
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 500 EUR transaction amount and system-generated Idempotency key
    # 500 / 1000 * (25 + 15) = 20.0
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Repayment              | 290.0  | 250.0     | 0.0      | 40.0 | 0.0       | 750.0        | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 250.0        | false    | false    |
      | 15 January 2026  | Fee Refund             | 20.0   | 20.0      | 0.0      | 0.0  | 0.0       | 230.0        | false    | false    |
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 20.0   |
      | INCOME | 404007       | Fee Income       | 20.0  |        |
    When Loan Pay-off is made on "15 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  Scenario: Verify Fee Refund is created when the Down Payment fee is still unpaid at refund time
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 25.0 | 0.0  | 0.0    | 25.0        |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2026  |           | 750.0           | 250.0         | 0.0      | 25.0 | 0.0       | 275.0 | 0.0  | 0.0        | 0.0  | 275.0       |
      | 2  | 15   | 16 January 2026  |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026  |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 500 EUR transaction amount and system-generated Idempotency key
    # Fee refund amount is based on original fee (500/1000*25=12.5). MIR pays overdue fee first, then Fee Refund reduces principal.
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 500.0  | 475.0     | 0.0      | 25.0 | 0.0       | 525.0        | false    | false    |
      | 15 January 2026  | Fee Refund             | 12.5   | 12.5      | 0.0      | 0.0  | 0.0       | 512.5        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 25.0 | 25.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 12.5   |
      | INCOME | 404007       | Fee Income       | 12.5  |        |
    When Loan Pay-off is made on "15 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  Scenario: Verify Fee Refund is calculated on the original amount of a waived Down Payment fee
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    And Admin waives the last unpaid "LOAN_DOWN_PAYMENT_FEE" type charge which is due on "01 January 2026"
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 25.0 | 0.0  | 25.0   | 0.0         |
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Waive loan charges     | 25.0   | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 15 January 2026  | Fee Refund             | 12.5   | 12.5      | 0.0      | 0.0  | 0.0       | 487.5        | false    | false    |
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 12.5   |
      | INCOME | 404007       | Fee Income       | 12.5  |        |
    When Loan Pay-off is made on "15 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  Scenario: Verify undo of Merchant Issued Refund reverses the linked Fee Refund
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 250.0        | false    | false    |
      | 15 January 2026  | Fee Refund             | 12.5   | 12.5      | 0.0      | 0.0  | 0.0       | 237.5        | false    | false    |
    When Customer undo "1"th "Merchant Issued Refund" transaction made on "15 January 2026" with linked "Fee Refund" transaction
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 250.0        | true     | false    |
      | 15 January 2026  | Fee Refund             | 12.5   | 12.5      | 0.0      | 0.0  | 0.0       | 237.5        | true     | false    |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026  | 01 January 2026 | 750.0           | 250.0         | 0.0      | 25.0 | 0.0       | 275.0 | 275.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2026  |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 25.0 | 0.0       | 1025.0 | 275.0 | 0.0        | 0.0  | 750.0       |
    When Loan Pay-off is made on "15 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  Scenario: Verify Fee Refund is created on backdated Merchant Issued Refund
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Customer makes "AUTOPAY" repayment on "10 January 2026" with 100 EUR transaction amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "05 January 2026" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has a transaction with date: "05 January 2026", and with the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 05 January 2026  | Merchant Issued Refund | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 250.0        | false    | false    |
      | 05 January 2026  | Fee Refund             | 12.5   | 12.5      | 0.0      | 0.0  | 0.0       | 237.5        | false    | false    |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 05 January 2026  | Merchant Issued Refund | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 250.0        | false    | false    |
      | 05 January 2026  | Fee Refund             | 12.5   | 12.5      | 0.0      | 0.0  | 0.0       | 237.5        | false    | false    |
      | 10 January 2026  | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 137.5        | false    | false    |
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "05 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 12.5   |
      | INCOME | 404007       | Fee Income       | 12.5  |        |
    When Loan Pay-off is made on "10 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
