@LoanChargesDownPaymentFeeRefundFeature
Feature: LoanChargesDownPaymentFeeRefund

  Scenario: Verify Fee Refund is created on Merchant Issued Refund for flat down payment fee - UC1
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

  Scenario: Verify Fee Refund is NOT created on Merchant Issued Refund when product has no fee refund config - UC2
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

  Scenario: Verify Fee Refund is created on Payout Refund for flat down payment fee - UC3
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

  Scenario: Verify multiple refunds create proportional Fee Refunds and the total Fee Refund is capped by the Down Payment fee - UC4
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

  Scenario: Verify Fee Refund with multi-disburse and multiple Down Payment fees - UC5
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

  Scenario: Verify Fee Refund with multiple flat fees on the same Down Payment period - UC6
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

  Scenario: Verify Fee Refund is created when the Down Payment fee is still unpaid at refund time - UC7
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

  Scenario: Verify Fee Refund is calculated on the original amount of a waived Down Payment fee - UC8
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

  Scenario: Verify undo of Merchant Issued Refund reverses the linked Fee Refund - UC9
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

  Scenario: Verify Fee Refund is created on backdated Merchant Issued Refund - UC10
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

  Scenario: Verify Fee Refund is created only on Merchant Issued Refund when product supports only MERCHANT_ISSUED_REFUND fee refund type - UC11
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND_MIR | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "10 January 2026" with 200 EUR transaction amount and system-generated Idempotency key
    # PAYOUT_REFUND is not in supportedFeeRefundTypes -> no Fee Refund
    Then Loan has 0 "FEE_REFUND" transactions on Transactions tab
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 200 EUR transaction amount and system-generated Idempotency key
    # 200 / 1000 * 25 = 5.0
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 10 January 2026  | Payout Refund          | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 550.0        | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 350.0        | false    | false    |
      | 15 January 2026  | Fee Refund             | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 345.0        | false    | false    |
    Then LoanTransactionFeeRefundPostBusinessEvent is raised on "15 January 2026"
    Then Loan has 1 "FEE_REFUND" transactions on Transactions tab
    Then Loan has 5.0 total fee refund amount
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 5.0    |
      | INCOME | 404007       | Fee Income       | 5.0   |        |
    When Loan Pay-off is made on "15 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  Scenario: Verify Fee Refund is created only on Payout Refund when product supports only PAYOUT_REFUND fee refund type - UC12
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND_PAYOUT | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 January 2026" with 200 EUR transaction amount and system-generated Idempotency key
    # MERCHANT_ISSUED_REFUND is not in supportedFeeRefundTypes -> no Fee Refund
    Then Loan has 0 "FEE_REFUND" transactions on Transactions tab
    When Admin sets the business date to "15 January 2026"
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 400 EUR transaction amount and system-generated Idempotency key
    # 400 / 1000 * 25 = 10.0
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 10 January 2026  | Merchant Issued Refund | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 550.0        | false    | false    |
      | 15 January 2026  | Payout Refund          | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 15 January 2026  | Fee Refund             | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 140.0        | false    | false    |
    Then LoanTransactionFeeRefundPostBusinessEvent is raised on "15 January 2026"
    Then Loan has 10.0 total fee refund amount
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 10.0   |
      | INCOME | 404007       | Fee Income       | 10.0  |        |
    When Loan Pay-off is made on "15 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  Scenario: Verify Fee Refund is NOT created on Payout Refund when product has no fee refund config - UC13
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    When Admin sets the business date to "15 January 2026"
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan has 0 "FEE_REFUND" transactions on Transactions tab
    Then Loan has 0.0 total fee refund amount
    When Loan Pay-off is made on "15 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  Scenario: Verify full Merchant Issued Refund creates full Fee Refund which goes to overpayment when nothing is outstanding - UC14
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 1000 EUR transaction amount and system-generated Idempotency key
    # 1000 / 1000 * 25 = 25.0; principal is fully repaid by the MIR, so the Fee Refund is overpayment
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 1000.0 | 750.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2026  | Fee Refund             | 25.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2026  | Accrual                | 25.0   | 0.0       | 0.0      | 25.0 | 0.0       | 0.0          | false    | false    |
    Then Loan status will be "OVERPAID"
    Then Loan has 275.0 overpaid amount
    Then Loan has 25.0 total fee refund amount
    Then LoanTransactionFeeRefundPostBusinessEvent is raised on "15 January 2026"
    Then Loan Transactions tab has a "MERCHANT_ISSUED_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 750.0  |
      | LIABILITY | l1           | Overpayment account       |        | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name        | Debit | Credit |
      | LIABILITY | l1           | Overpayment account |       | 25.0   |
      | INCOME    | 404007       | Fee Income          | 25.0  |        |
    When Admin makes Credit Balance Refund transaction on "15 January 2026" with 275 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"

  Scenario: Verify no Fee Refund is created once the cumulative Fee Refund reached the total Down Payment fee amount - UC15
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 January 2026" with 600 EUR transaction amount and system-generated Idempotency key
    # 1st: 600 / 1000 * 25 = 15.0
    When Admin sets the business date to "12 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "12 January 2026" with 600 EUR transaction amount and system-generated Idempotency key
    # 2nd: 15.0 calculated, capped to 25 - 15 = 10.0
    When Admin sets the business date to "14 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "14 January 2026" with 100 EUR transaction amount and system-generated Idempotency key
    # 3rd: 2.5 calculated, but cap (25.0) already reached -> no Fee Refund
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 10 January 2026  | Merchant Issued Refund | 600.0  | 600.0     | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 10 January 2026  | Fee Refund             | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 135.0        | false    | false    |
      | 12 January 2026  | Merchant Issued Refund | 600.0  | 135.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2026  | Fee Refund             | 10.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2026  | Accrual                | 25.0   | 0.0       | 0.0      | 25.0 | 0.0       | 0.0          | false    | false    |
      | 14 January 2026  | Merchant Issued Refund | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan has 3 "MERCHANT_ISSUED_REFUND" transactions on Transactions tab
    Then Loan has 2 "FEE_REFUND" transactions on Transactions tab
    Then Loan has 25.0 total fee refund amount
    Then Loan status will be "OVERPAID"
    Then Loan has 575.0 overpaid amount
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "12 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name        | Debit | Credit |
      | LIABILITY | l1           | Overpayment account |       | 10.0   |
      | INCOME    | 404007       | Fee Income          | 10.0  |        |
    When Admin makes Credit Balance Refund transaction on "14 January 2026" with 575 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"

  Scenario: Verify Merchant Issued Refund and Payout Refund share the same Fee Refund cap - UC16
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 January 2026" with 300 EUR transaction amount and system-generated Idempotency key
    # MIR: 300 / 1000 * 25 = 7.5
    When Admin sets the business date to "12 January 2026"
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "12 January 2026" with 900 EUR transaction amount and system-generated Idempotency key
    # Payout: 900 / 1000 * 25 = 22.5, capped to 25 - 7.5 = 17.5
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 10 January 2026  | Merchant Issued Refund | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 450.0        | false    | false    |
      | 10 January 2026  | Fee Refund             | 7.5    | 7.5       | 0.0      | 0.0  | 0.0       | 442.5        | false    | false    |
      | 12 January 2026  | Payout Refund          | 900.0  | 442.5     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2026  | Fee Refund             | 17.5   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2026  | Accrual                | 25.0   | 0.0       | 0.0      | 25.0 | 0.0       | 0.0          | false    | false    |
    Then Loan has 25.0 total fee refund amount
    Then Loan status will be "OVERPAID"
    Then Loan has 475.0 overpaid amount
    When Admin makes Credit Balance Refund transaction on "12 January 2026" with 475 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"

  Scenario: Verify Fee Refund with multiple disbursements of different amounts and percentage Down Payment fees (requirement example) - UC17
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_PERCENTAGE_FEE" with "PERCENTAGE_AMOUNT" calculation type and 10.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type          | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1500           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_PERCENTAGE_FEE | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1500" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment percentage fee" charge due on "01 January 2026"
    When Admin sets the business date to "02 January 2026"
    When Admin successfully disburse the loan on "02 January 2026" with "500" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment percentage fee" charge due on "02 January 2026"
    Then Loan Charges tab has the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | % Amount         | 25.0 | 25.0 | 0.0    | 0.0         |
      | Down Payment percentage fee | false     | Down Payment   | 02 January 2026 | % Amount         | 12.5 | 12.5 | 0.0    | 0.0         |
    When Admin sets the business date to "10 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 January 2026" with 750 EUR transaction amount and system-generated Idempotency key
    # ratio = 750 / 1500 = 50%; Fee Refund = 50% * (25 + 12.5) = 18.75 (12.5 from fee 1 + 6.25 from fee 2)
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 02 January 2026  | Disbursement           | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       | false    | false    |
      | 02 January 2026  | Down Payment           | 137.5  | 125.0     | 0.0      | 12.5 | 0.0       | 1125.0       | false    | false    |
      | 10 January 2026  | Merchant Issued Refund | 750.0  | 750.0     | 0.0      | 0.0  | 0.0       | 375.0        | false    | false    |
      | 10 January 2026  | Fee Refund             | 18.75  | 18.75     | 0.0      | 0.0  | 0.0       | 356.25       | false    | false    |
    Then Loan has 18.75 total fee refund amount
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 18.75  |
      | INCOME | 404007       | Fee Income       | 18.75 |        |
    When Loan Pay-off is made on "10 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  Scenario: Verify Fee Refund amount is rounded according to the loan currency - UC18
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 123.45 EUR transaction amount and system-generated Idempotency key
    # 123.45 / 1000 * 25 = 3.08625 -> 3.09
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 123.45 | 123.45    | 0.0      | 0.0  | 0.0       | 626.55       | false    | false    |
      | 15 January 2026  | Fee Refund             | 3.09   | 3.09      | 0.0      | 0.0  | 0.0       | 623.46       | false    | false    |
    Then Loan has 3.09 total fee refund amount
    When Loan Pay-off is made on "15 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  Scenario: Verify Fee Refund is allocated to the still outstanding Down Payment fee - UC19
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 20 EUR transaction amount and system-generated Idempotency key
    # MIR pays 20.0 of the overdue Down Payment fee; Fee Refund = 20 / 1000 * 25 = 0.5 is allocated to the same fee
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 1000.0       | false    | false    |
      | 15 January 2026  | Fee Refund             | 0.5    | 0.0       | 0.0      | 0.5  | 0.0       | 1000.0       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 25.0 | 20.5 | 0.0    | 4.5         |
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 0.5    |
      | INCOME | 404007       | Fee Income              | 0.5   |        |
    When Loan Pay-off is made on "15 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  Scenario: Verify Fee Refund is calculated on the original amount of an adjusted Down Payment fee - UC20
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    When Admin makes a charge adjustment for the last "LOAN_DOWN_PAYMENT_FEE" type charge which is due on "01 January 2026" with 10 EUR transaction amount and externalId ""
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 500 EUR transaction amount and system-generated Idempotency key
    # charge adjustment does not reduce the base: 500 / 1000 * 25 = 12.5
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 01 January 2026  | Charge Adjustment      | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 740.0        | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 240.0        | false    | false    |
      | 15 January 2026  | Fee Refund             | 12.5   | 12.5      | 0.0      | 0.0  | 0.0       | 227.5        | false    | false    |
    Then Loan has 12.5 total fee refund amount
    When Loan Pay-off is made on "15 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  Scenario: Verify fees other than Down Payment fee are not part of the Fee Refund base - UC21
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    When Admin sets the business date to "10 January 2026"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 January 2026" due date and 100 EUR transaction amount
    When Admin sets the business date to "15 January 2026"
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 500 EUR transaction amount and system-generated Idempotency key
    # Snooze fee is not refunded: 500 / 1000 * 25 = 12.5
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0   | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 275.0  | 250.0     | 0.0      | 25.0  | 0.0       | 750.0        | false    | false    |
      | 15 January 2026  | Payout Refund    | 500.0  | 400.0     | 0.0      | 100.0 | 0.0       | 350.0        | false    | false    |
      | 15 January 2026  | Fee Refund       | 12.5   | 12.5      | 0.0      | 0.0   | 0.0       | 337.5        | false    | false    |
    Then Loan has 12.5 total fee refund amount
    When Loan Pay-off is made on "15 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  Scenario: Verify undo of Payout Refund reverses the linked Fee Refund and the reversed amount no longer counts against the cap - UC22
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    When Admin sets the business date to "15 January 2026"
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 1000 EUR transaction amount and system-generated Idempotency key
    When Customer undo "1"th "Payout Refund" transaction made on "15 January 2026" with linked "Fee Refund" transaction
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 15 January 2026  | Payout Refund    | 1000.0 | 750.0     | 0.0      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 15 January 2026  | Fee Refund       | 25.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 15 January 2026  | Accrual          | 25.0   | 0.0       | 0.0      | 25.0 | 0.0       | 0.0          | false    | false    |
    Then Loan has 0.0 total fee refund amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "16 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "16 January 2026" with 200 EUR transaction amount and system-generated Idempotency key
    # cap is restored after the reversal: 200 / 1000 * 25 = 5.0
    Then Loan Transactions tab has a transaction with date: "16 January 2026", and with the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 16 January 2026  | Merchant Issued Refund | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 550.0        | false    | false    |
      | 16 January 2026  | Fee Refund             | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 545.0        | false    | false    |
    Then Loan has 5.0 total fee refund amount
    When Loan Pay-off is made on "16 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  Scenario: Verify Fee Refund on backdated Payout Refund keeps its amount when later repayments are reverse-replayed - UC23
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    When Admin sets the business date to "10 January 2026"
    And Customer makes "AUTOPAY" repayment on "10 January 2026" with 100 EUR transaction amount
    When Admin sets the business date to "12 January 2026"
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "05 January 2026" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has a transaction with date: "05 January 2026", and with the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 05 January 2026  | Payout Refund    | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 05 January 2026  | Fee Refund       | 12.5   | 12.5      | 0.0      | 0.0  | 0.0       | 237.5        |
    Then Loan Transactions tab has a transaction with date: "10 January 2026", and with the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 10 January 2026  | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 137.5        |
    Then Loan has 12.5 total fee refund amount
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "05 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 12.5   |
      | INCOME | 404007       | Fee Income       | 12.5  |        |
    When Loan Pay-off is made on "12 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @Skip @FeeRefundRecalculation
  Scenario: Verify undo of the first refund recalculates the capped Fee Refund of the later refund - UC24
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 January 2026" with 600 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "12 January 2026"
  #  And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "12 January 2026" with 600 EUR transaction amount and system-generated Idempotency key
  # before undo: 15.0 + 10.0 (capped)
    Then Loan has 25.0 total fee refund amount
    When Customer undo "1"th "Merchant Issued Refund" transaction made on "10 January 2026" with linked "Fee Refund" transaction
  # TODO - Fee Refund correct, but - shall be 2nd  Fee Refund be recalculated? see how Interest refund works
    Then Loan Transactions tab has a transaction with date: "12 January 2026", and with the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 12 January 2026  | Merchant Issued Refund | 600.0  | 600.0     | 0.0      | 0.0  | 0.0       | 150.0        |
      | 12 January 2026  | Fee Refund             | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 135.0        |
    Then Loan has 15.0 total fee refund amount

  @Skip @FeeRefundRecalculation
  Scenario: Verify backdated Merchant Issued Refund before an existing refund recalculates the Fee Refunds chronologically with the cap - UC25
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 600 EUR transaction amount and system-generated Idempotency key
    Then Loan has 15.0 total fee refund amount
    When Admin sets the business date to "20 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 January 2026" with 600 EUR transaction amount and system-generated Idempotency key
  # expected chronologically: 10 January -> 15.0, 15 January -> capped to 10.0
    #TODO - reprocessing, double check how it works
    Then Loan Transactions tab has a transaction with date: "10 January 2026", and with the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 10 January 2026  | Merchant Issued Refund | 600.0  | 600.0     | 0.0      | 0.0  | 0.0       | 150.0        |
      | 10 January 2026  | Fee Refund             | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 135.0        |
    Then Loan Transactions tab has a transaction with date: "15 January 2026", and with the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 15 January 2026  | Merchant Issued Refund | 600.0  | 135.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 15 January 2026  | Fee Refund             | 10.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan has 25.0 total fee refund amount
    Then Loan status will be "OVERPAID"
    Then Loan has 475.0 overpaid amount

  @Skip @FeeRefundRecalculation
  Scenario: Verify backdated second disbursement recalculates the Fee Refund ratio and base - UC26
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1500           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1500" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 500 EUR transaction amount and system-generated Idempotency key
  # 500 / 1000 * 25 = 12.5
    Then Loan has 12.5 total fee refund amount
    When Admin sets the business date to "20 January 2026"
    When Admin successfully disburse the loan on "10 January 2026" with "500" EUR transaction amount
  # expected after reverse-replay: 500 / 1500 * (25 + 25) = 16.67
  # actual Fee Refund stays 12.5, balance 612.5
    #TODO - double check reprocessing
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        |
      | 10 January 2026  | Disbursement           | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
      | 10 January 2026  | Down Payment           | 150.0  | 125.0     | 0.0      | 25.0 | 0.0       | 1125.0       |
      | 15 January 2026  | Merchant Issued Refund | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 625.0        |
      | 15 January 2026  | Fee Refund             | 16.67  | 16.67     | 0.0      | 0.0  | 0.0       | 608.33       |
    Then Loan has 16.67 total fee refund amount

  @Skip @FeeRefundCapChronology
  Scenario: Verify Fee Refund cap is consumed by the later refund when an earlier dated refund is backdated (current behavior) - UC27
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 1000 EUR transaction amount and system-generated Idempotency key
    Then Loan has 25.0 total fee refund amount
    When Admin sets the business date to "20 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 January 2026" with 400 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 10 January 2026  | Merchant Issued Refund | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 350.0        | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 1000.0 | 350.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 15 January 2026  | Fee Refund             | 25.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 15 January 2026  | Accrual                | 25.0   | 0.0       | 0.0      | 25.0 | 0.0       | 0.0          | false    | false    |
    Then Loan has 2 "MERCHANT_ISSUED_REFUND" transactions on Transactions tab
    Then Loan has 1 "FEE_REFUND" transactions on Transactions tab
    Then Loan has 25.0 total fee refund amount
    Then Loan status will be "OVERPAID"
    Then Loan has 675.0 overpaid amount
    When Admin makes Credit Balance Refund transaction on "20 January 2026" with 675 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"

  @FeeRefundOriginalAmountBase
  Scenario: Verify full refund after a charge adjustment returns more than the Down Payment fee amount - UC28
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin makes a charge adjustment for the last "LOAN_DOWN_PAYMENT_FEE" type charge which is due on "01 January 2026" with 10 EUR transaction amount and externalId ""
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 1000 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 01 January 2026  | Charge Adjustment      | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 740.0        | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 1000.0 | 740.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2026  | Fee Refund             | 25.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2026  | Accrual                | 25.0   | 0.0       | 0.0      | 25.0 | 0.0       | 0.0          | false    | false    |
    Then Loan has 25.0 total fee refund amount
    Then Loan status will be "OVERPAID"
    Then Loan has 285.0 overpaid amount
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 10.0   |
      | INCOME | 404007       | Fee Income       | 10.0  |        |
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name        | Debit | Credit |
      | LIABILITY | l1           | Overpayment account |       | 25.0   |
      | INCOME    | 404007       | Fee Income          | 25.0  |        |
    When Admin makes Credit Balance Refund transaction on "15 January 2026" with 285 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"

  @FeeRefundOriginalAmountBase
  Scenario: Verify full refund creates Fee Refund for a fully waived Down Payment fee that was never paid - UC29
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    And Admin waives the last unpaid "LOAN_DOWN_PAYMENT_FEE" type charge which is due on "01 January 2026"
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 25.0 | 0.0  | 25.0   | 0.0         |
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 1000 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Waive loan charges     | 25.0   | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2026  | Fee Refund             | 25.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan has 25.0 total fee refund amount
    Then Loan status will be "OVERPAID"
    Then Loan has 25.0 overpaid amount
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name        | Debit | Credit |
      | LIABILITY | l1           | Overpayment account |       | 25.0   |
      | INCOME    | 404007       | Fee Income          | 25.0  |        |
    When Admin makes Credit Balance Refund transaction on "15 January 2026" with 25 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"

  @FeeRefundLoanStates
  Scenario: Verify Fee Refund is kept when its Merchant Issued Refund is charged back (current behavior) - UC30
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 January 2026" with 400 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "12 January 2026"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 400 EUR transaction amount
    #the chargeback restores the 400.0 principal and the linked Fee Refund of 10.0 stays
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 10 January 2026  | Merchant Issued Refund | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 350.0        | false    | false    |
      | 10 January 2026  | Fee Refund             | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 340.0        | false    | false    |
      | 12 January 2026  | Chargeback             | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 740.0        | false    | false    |
    Then Loan has 10.0 total fee refund amount
    Then Loan has 1 "FEE_REFUND" transactions on Transactions tab
    Then Loan status will be "ACTIVE"
    Then Loan has 740.0 outstanding amount
    When Loan Pay-off is made on "12 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @FeeRefundLoanStates
  Scenario: Verify undo disbursement after a Fee Refund (current behavior) - UC31
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 January 2026" with 400 EUR transaction amount and system-generated Idempotency key
    When Admin successfully undo disbursal
    # current behavior: undo disbursal is allowed after a Fee Refund; all transactions, including the Fee Refund, are removed
    Then Loan status will be "APPROVED"
    Then Loan has 0.0 total fee refund amount

  @FeeRefundLoanStates
  Scenario: Verify Fee Refund on a charged-off loan (current behavior) - UC32
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Admin does charge-off the loan on "10 January 2026"
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 400 EUR transaction amount and system-generated Idempotency key
    # current behavior: Fee Refund is created on a charged-off loan; its credit goes to the charge-off expense account
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 10 January 2026  | Charge-off             | 750.0  | 750.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 350.0        | false    | false    |
      | 15 January 2026  | Fee Refund             | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 340.0        | false    | false    |
    Then Loan has 10.0 total fee refund amount
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |       | 10.0   |
      | INCOME    | 404007       | Fee Income                | 10.0  |        |

  @FeeRefundLoanStates
  Scenario: Verify Fee Refund on a contract terminated loan (current behavior) - UC33
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Admin successfully terminates loan contract
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 400 EUR transaction amount and system-generated Idempotency key
    # current behavior: Fee Refund is created on a contract terminated loan as on an active loan
    # TODO -  is MIR at all allowed on contract terminated loan?
    Then Loan has sub status "Contract Termination"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 10 January 2026  | Contract Termination   | 750.0  | 750.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 350.0        | false    | false    |
      | 15 January 2026  | Fee Refund             | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 340.0        | false    | false    |
    Then Loan has 10.0 total fee refund amount
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 10.0   |
      | INCOME    | 404007       | Fee Income                | 10.0  |        |

  @FeeRefundLoanStates
  Scenario: Verify Fee Refund on an already overpaid loan (current behavior) - UC34
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "05 January 2026"
    And Customer makes "AUTOPAY" repayment on "05 January 2026" with 800 EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 January 2026" with 200 EUR transaction amount and system-generated Idempotency key
    # current behavior: on an already overpaid loan the MIR and its Fee Refund (200 / 1000 * 25 = 5.0) both go to overpayment
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 05 January 2026  | Repayment              | 800.0  | 750.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2026  | Accrual                | 25.0   | 0.0       | 0.0      | 25.0 | 0.0       | 0.0          | false    | false    |
      | 10 January 2026  | Merchant Issued Refund | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2026  | Fee Refund             | 5.0    | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan has 5.0 total fee refund amount
    Then Loan status will be "OVERPAID"
    Then Loan has 255.0 overpaid amount
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       |       | 5.0    |
      | INCOME    | 404007       | Fee Income                | 5.0   |        |
    When Admin makes Credit Balance Refund transaction on "10 January 2026" with 255 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"

  @FeeRefundLoanStates
  Scenario: Verify Fee Refund is kept when its Payout Refund is charged back (current behavior) - UC35
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "10 January 2026" with 400 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "12 January 2026"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 400 EUR transaction amount
    # current behavior: the chargeback restores the 400.0 principal, but the linked Fee Refund of 10.0 stays
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 10 January 2026  | Payout Refund          | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 350.0        | false    | false    |
      | 10 January 2026  | Fee Refund             | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 340.0        | false    | false    |
      | 12 January 2026  | Chargeback             | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 740.0        | false    | false    |
    Then Loan has 10.0 total fee refund amount
    Then Loan has 1 "FEE_REFUND" transactions on Transactions tab
    Then Loan has 740.0 outstanding amount
    When Loan Pay-off is made on "12 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @FeeRefundLoanStates
  Scenario: Verify Fee Refund on a charged-off fraud loan (current behavior) - UC36
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then Admin can successfully set Fraud flag to the loan
    When Admin sets the business date to "10 January 2026"
    And Admin does charge-off the loan on "10 January 2026"
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 400 EUR transaction amount and system-generated Idempotency key
    # current behavior: on a charged-off fraud loan the Fee Refund credit goes to the fraud charge-off expense account
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 10 January 2026  | Charge-off             | 750.0  | 750.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 350.0        | false    | false    |
      | 15 January 2026  | Fee Refund             | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 340.0        | false    | false    |
    Then Loan has 10.0 total fee refund amount
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | EXPENSE   | 744037       | Credit Loss/Bad Debt-Fraud |       | 10.0   |
      | INCOME    | 404007       | Fee Income                | 10.0  |        |

  @FeeRefundLoanStates
  Scenario: Verify Fee Refund made before the charge-off is kept after undo of the charge-off (current behavior) - UC37
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "05 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "05 January 2026" with 400 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "10 January 2026"
    And Admin does charge-off the loan on "10 January 2026"
    When Admin sets the business date to "20 January 2026"
    Then Admin does a charge-off undo the loan
    # current behavior: undo charge-off is rejected when a refund was made after the charge-off ("User transaction was found after charge-off"),
    # so the refund is made before the charge-off; the charge-off amount (340.0) already includes the Fee Refund, which is kept after the undo
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 05 January 2026  | Merchant Issued Refund | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 350.0        | false    | false    |
      | 05 January 2026  | Fee Refund             | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 340.0        | false    | false    |
      | 10 January 2026  | Charge-off             | 340.0  | 340.0     | 0.0      | 0.0  | 0.0       | 0.0          | true     | false    |
    Then Loan has 10.0 total fee refund amount
    Then Loan status will be "ACTIVE"
    Then Loan has 340.0 outstanding amount
    When Loan Pay-off is made on "20 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @FeeRefundLoanStates
  Scenario: Verify Fee Refund made before the contract termination is kept after undo of the contract termination (current behavior) - UC38
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "05 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "05 January 2026" with 400 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "10 January 2026"
    And Admin successfully terminates loan contract
    # current behavior: contract termination reverse-replays the MIR, but the Fee Refund is not taken into account:
    # the termination amount is 350.0 instead of 340.0 and the loan ends up with 350.0 outstanding while the API also reports 10.0 overpaid
    Then Loan has sub status "Contract Termination"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        |
      | 05 January 2026  | Fee Refund             | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 740.0        |
      | 05 January 2026  | Merchant Issued Refund | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 340.0        |
      | 10 January 2026  | Contract Termination   | 350.0  | 350.0     | 0.0      | 0.0  | 0.0       | 0.0          |
    # the loan API also returns totalOverpaid = 10.0 here
    Then Loan has 350.0 outstanding amount
    Then Loan has 10.0 total fee refund amount
    When Admin sets the business date to "20 January 2026"
    When Admin successfully undoes loan contract termination
    # after the undo the Fee Refund is kept
    Then Loan has 10.0 total fee refund amount
    Then Loan has 340.0 outstanding amount
    Then Loan status will be "ACTIVE"

  @FeeRefundLoanStates
  Scenario: Verify Fee Refund on a refund made after the loan is closed (current behavior) - UC39
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_FEE_REFUND | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    When Loan Pay-off is made on "10 January 2026"
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 200 EUR transaction amount and system-generated Idempotency key
    # current behavior: a refund on a closed loan creates a Fee Refund (200 / 1000 * 25 = 5.0); both go to overpayment
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 10 January 2026  | Repayment              | 750.0  | 750.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2026  | Accrual                | 25.0   | 0.0       | 0.0      | 25.0 | 0.0       | 0.0          | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2026  | Fee Refund             | 5.0    | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan has 5.0 total fee refund amount
    Then Loan status will be "OVERPAID"
    Then Loan has 205.0 overpaid amount
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       |       | 5.0    |
      | INCOME    | 404007       | Fee Income                | 5.0   |        |
    When Admin makes Credit Balance Refund transaction on "15 January 2026" with 205 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"

  @FeeRefundWithInterestRefund
  Scenario: Verify Merchant Issued Refund creates both Interest Refund and Fee Refund when both are enabled (current behavior) - UC40
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADV_PYMNT_INTEREST_RECALCULATION_INTEREST_REFUND_FEE_REFUND | 01 January 2026   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 500 EUR transaction amount and system-generated Idempotency key
    # current behavior: both derived transactions are created and both are linked to the MIR;
    # Interest Refund = 2.3, Fee Refund = 500 / 1000 * 25 = 12.5; the Interest Refund does not take the Fee Refund into account
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 250.0        | false    | false    |
      | 15 January 2026  | Interest Refund        | 2.3    | 2.3       | 0.0      | 0.0  | 0.0       | 247.7        | false    | false    |
      | 15 January 2026  | Fee Refund             | 12.5   | 12.5      | 0.0      | 0.0  | 0.0       | 235.2        | false    | false    |
    Then Loan has 12.5 total fee refund amount
    Then Loan has 1 "INTEREST_REFUND" transactions on Transactions tab
    Then Loan has 1 "FEE_REFUND" transactions on Transactions tab
    Then Loan Transactions tab has a "INTEREST_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 2.3    |
      | INCOME    | 404000       | Interest Income           | 2.3   |        |
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 12.5   |
      | INCOME    | 404007       | Fee Income                | 12.5  |        |
    When Loan Pay-off is made on "15 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @FeeRefundWithInterestRefund
  Scenario: Verify full Payout Refund creates both Interest Refund and Fee Refund which go to overpayment (current behavior) - UC41
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADV_PYMNT_INTEREST_RECALCULATION_INTEREST_REFUND_FEE_REFUND | 01 January 2026   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "15 January 2026"
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 1000 EUR transaction amount and system-generated Idempotency key
    # current behavior: the Payout Refund pays principal and accrued interest; Interest Refund (3.45) and Fee Refund (25.0) both go to overpayment
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 15 January 2026  | Payout Refund          | 1000.0 | 750.0     | 3.45     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2026  | Interest Refund        | 3.45   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2026  | Fee Refund             | 25.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2026  | Accrual                | 28.45  | 0.0       | 3.45     | 25.0 | 0.0       | 0.0          | false    | false    |
    Then Loan has 25.0 total fee refund amount
    Then Loan status will be "OVERPAID"
    Then Loan has 275.0 overpaid amount
    Then Loan Transactions tab has a "INTEREST_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       |       | 3.45   |
      | INCOME    | 404000       | Interest Income           | 3.45  |        |
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       |       | 25.0   |
      | INCOME    | 404007       | Fee Income                | 25.0  |        |
    When Admin makes Credit Balance Refund transaction on "15 January 2026" with 275 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"

  @FeeRefundWithInterestRefund
  Scenario: Verify undo of Merchant Issued Refund reverses both the linked Interest Refund and Fee Refund (current behavior) - UC42
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADV_PYMNT_INTEREST_RECALCULATION_INTEREST_REFUND_FEE_REFUND | 01 January 2026   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 500 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "16 January 2026"
    When Customer undo "1"th "Merchant Issued Refund" transaction made on "15 January 2026"
    # current behavior: undo of the MIR reverses both the Interest Refund and the Fee Refund
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 250.0        | true     | false    |
      | 15 January 2026  | Interest Refund        | 2.3    | 2.3       | 0.0      | 0.0  | 0.0       | 247.7        | true     | false    |
      | 15 January 2026  | Fee Refund             | 12.5   | 12.5      | 0.0      | 0.0  | 0.0       | 235.2        | true     | false    |
    Then Loan has 0.0 total fee refund amount
    Then Loan status will be "ACTIVE"
    When Loan Pay-off is made on "16 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @FeeRefundCashAccounting
  Scenario: Verify Merchant Issued Refund on cash based product creates Interest Refund and Fee Refund without journal entries (current behavior) - UC43
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADV_PYMNT_INTEREST_RECALCULATION_INTEREST_REFUND_FEE_REFUND_CASH_ACCOUNTING | 01 January 2026   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "15 January 2026"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 15 January 2026  | Merchant Issued Refund | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 250.0        | false    | false    |
      | 15 January 2026  | Interest Refund        | 2.3    | 2.3       | 0.0      | 0.0  | 0.0       | 247.7        | false    | false    |
      | 15 January 2026  | Fee Refund             | 12.5   | 12.5      | 0.0      | 0.0  | 0.0       | 235.2        | false    | false    |
    Then Loan has 12.5 total fee refund amount
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | INCOME    | 404007       | Fee Income                |       | 25.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 275.0 |        |
    Then Loan Transactions tab has a "MERCHANT_ISSUED_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    # current behavior: cash based accounting creates no journal entries for Interest Refund, and Fee Refund follows it
    Then Loan Transactions tab has a "INTEREST_REFUND" transaction with date "15 January 2026" which has no Journal entries
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "15 January 2026" which has no Journal entries
    # after the pay-off the loan is closed, but 14.8 (2.3 + 12.5) stays on Loans Receivable in GL
    When Loan Pay-off is made on "15 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @FeeRefundCashAccounting
  Scenario: Verify full Payout Refund on cash based product creates Interest Refund and Fee Refund to overpayment without journal entries (current behavior) - UC44
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 25.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADV_PYMNT_INTEREST_RECALCULATION_INTEREST_REFUND_FEE_REFUND_CASH_ACCOUNTING | 01 January 2026   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 25.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "15 January 2026"
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "15 January 2026" with 1000 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment           | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
      | 15 January 2026  | Payout Refund          | 1000.0 | 750.0     | 3.45     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2026  | Interest Refund        | 3.45   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2026  | Fee Refund             | 25.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan has 25.0 total fee refund amount
    Then Loan status will be "OVERPAID"
    Then Loan has 275.0 overpaid amount
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 750.0  |
      | INCOME    | 404000       | Interest Income           |        | 3.45   |
      | LIABILITY | l1           | Overpayment account       |        | 246.55 |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
    # current behavior: cash based accounting creates no journal entries for Interest Refund, and Fee Refund follows it,
    # so the overpayment liability in GL (246.55) differs from the loan overpaid amount (275.0) by 3.45 + 25.0
    Then Loan Transactions tab has a "INTEREST_REFUND" transaction with date "15 January 2026" which has no Journal entries
    Then Loan Transactions tab has a "FEE_REFUND" transaction with date "15 January 2026" which has no Journal entries
    When Admin makes Credit Balance Refund transaction on "15 January 2026" with 275 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
