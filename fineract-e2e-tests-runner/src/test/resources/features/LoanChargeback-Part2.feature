@LoanChargeback
Feature: LoanChargeback - Part2

  @TestRailId:C3080 @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - partial chargeback on overpaid loan
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2024   | 500            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "500" EUR transaction amount
    When Admin sets the business date to "10 January 2024"
    When Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "10 January 2024" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 125 overpaid amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024 | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2024  | 10 January 2024 | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 125.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 January 2024  | 10 January 2024 | 125.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 125.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 February 2024 | 10 January 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 125.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 500.0 | 375.0      | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 January 2024  | Down Payment     | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 10 January 2024  | Payout Refund    | 500.0  | 375.0     | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 500.0  |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 125.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 125.0 |        |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "10 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 375.0  |
      | LIABILITY | l1           | Overpayment account       |       | 125.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
#    --- Chargeback transaction added on same day ---
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 25 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024 | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2024  | 10 January 2024 | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 125.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 January 2024  | 10 January 2024 | 125.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 125.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 February 2024 | 10 January 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 125.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 500.0 | 375.0      | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 January 2024  | Down Payment     | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 10 January 2024  | Payout Refund    | 500.0  | 375.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 10 January 2024  | Chargeback       | 25.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 500.0  |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 125.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 125.0 |        |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "10 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 375.0  |
      | LIABILITY | l1           | Overpayment account       |       | 125.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "10 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       | 25.0  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 25.0   |

  @TestRailId:C3081 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation UC1: partial, prior chargeback
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 500            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "500" EUR transaction amount
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "16 January 2024"
    And Customer makes "AUTOPAY" repayment on "16 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "20 January 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount
    When Admin sets the business date to "31 January 2024"
    And Customer makes "AUTOPAY" repayment on "31 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 125 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2024  | 04 January 2024  | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 125.0 | 0.0         |
      | 2  | 15   | 16 January 2024  | 16 January 2024  | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 15   | 31 January 2024  | 31 January 2024  | 125.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 4  | 15   | 15 February 2024 | 15 February 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 625.0         | 0.0      | 0.0  | 0.0       | 625.0 | 625.0 | 0.0        | 125.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 04 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 20 January 2024  | Chargeback       | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 31 January 2024  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 125.0        |
      | 15 February 2024 | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 0.0          |

  @TestRailId:C3082 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation UC2: partial, prior chargeback with penalty
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 500            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "500" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 January 2024" due date and 3 EUR transaction amount
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "16 January 2024"
    And Customer makes "AUTOPAY" repayment on "16 January 2024" with 128 EUR transaction amount
    When Admin sets the business date to "20 January 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 128 EUR transaction amount
    When Admin sets the business date to "31 January 2024"
    And Customer makes "AUTOPAY" repayment on "31 January 2024" with 253 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 125 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2024  | 04 January 2024  | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 125.0 | 0.0         |
      | 2  | 15   | 16 January 2024  | 16 January 2024  | 250.0           | 125.0         | 0.0      | 0.0  | 3.0       | 128.0 | 128.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 15   | 31 January 2024  | 31 January 2024  | 125.0           | 250.0         | 0.0      | 0.0  | 3.0       | 253.0 | 253.0 | 0.0        | 0.0   | 0.0         |
      | 4  | 15   | 15 February 2024 | 15 February 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 625.0         | 0.0      | 0.0  | 6.0       | 631.0 | 631.0 | 0.0        | 125.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 04 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 January 2024  | Repayment        | 128.0  | 125.0     | 0.0      | 0.0  | 3.0       | 250.0        |
      | 20 January 2024  | Chargeback       | 128.0  | 125.0     | 0.0      | 0.0  | 3.0       | 375.0        |
      | 31 January 2024  | Repayment        | 253.0  | 250.0     | 0.0      | 0.0  | 3.0       | 125.0        |
      | 15 February 2024 | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 15 February 2024 | Accrual          | 3.0    | 0.0       | 0.0      | 0.0  | 3.0       | 0.0          |

  @TestRailId:C3083 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation UC3: partial, prior chargeback with penalty (2)
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 500            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "500" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 January 2024" due date and 3 EUR transaction amount
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "16 January 2024"
    And Customer makes "AUTOPAY" repayment on "16 January 2024" with 128 EUR transaction amount
    When Admin sets the business date to "20 January 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 53 EUR transaction amount
    When Admin sets the business date to "31 January 2024"
    And Customer makes "AUTOPAY" repayment on "31 January 2024" with 175 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 128 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2024  | 04 January 2024  | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 125.0 | 0.0         |
      | 2  | 15   | 16 January 2024  | 16 January 2024  | 250.0           | 125.0         | 0.0      | 0.0  | 3.0       | 128.0 | 128.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 15   | 31 January 2024  | 15 February 2024 | 125.0           | 175.0         | 0.0      | 0.0  | 3.0       | 178.0 | 178.0 | 0.0        | 3.0   | 0.0         |
      | 4  | 15   | 15 February 2024 | 15 February 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 550.0         | 0.0      | 0.0  | 6.0       | 556.0 | 556.0 | 0.0        | 128.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 04 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 January 2024  | Repayment        | 128.0  | 125.0     | 0.0      | 0.0  | 3.0       | 250.0        |
      | 20 January 2024  | Chargeback       | 53.0   | 50.0      | 0.0      | 0.0  | 3.0       | 300.0        |
      | 31 January 2024  | Repayment        | 175.0  | 172.0     | 0.0      | 0.0  | 3.0       | 128.0        |
      | 15 February 2024 | Repayment        | 128.0  | 128.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 15 February 2024 | Accrual          | 3.0    | 0.0       | 0.0      | 0.0  | 3.0       | 0.0          |

  @TestRailId:C3084 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation UC4: partial, prior chargeback with penalty and fee
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 500            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "500" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 January 2024" due date and 3 EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 January 2024" due date and 4 EUR transaction amount
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "16 January 2024"
    And Customer makes "AUTOPAY" repayment on "16 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "20 January 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 50 EUR transaction amount
    When Admin sets the business date to "31 January 2024"
    And Customer makes "AUTOPAY" repayment on "31 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 182 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2024  | 04 January 2024  | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 125.0 | 0.0         |
      | 2  | 15   | 16 January 2024  | 31 January 2024  | 250.0           | 125.0         | 0.0      | 4.0  | 3.0       | 132.0 | 132.0 | 0.0        | 7.0   | 0.0         |
      | 3  | 15   | 31 January 2024  | 15 February 2024 | 125.0           | 168.0         | 0.0      | 4.0  | 3.0       | 175.0 | 175.0 | 0.0        | 57.0  | 0.0         |
      | 4  | 15   | 15 February 2024 | 15 February 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 543.0         | 0.0      | 8.0  | 6.0       | 557.0 | 557.0 | 0.0        | 189.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 04 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 January 2024  | Repayment        | 125.0  | 118.0     | 0.0      | 4.0  | 3.0       | 257.0        |
      | 20 January 2024  | Chargeback       | 50.0   | 43.0      | 0.0      | 4.0  | 3.0       | 300.0        |
      | 31 January 2024  | Repayment        | 125.0  | 118.0     | 0.0      | 4.0  | 3.0       | 182.0        |
      | 15 February 2024 | Repayment        | 182.0  | 182.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 15 February 2024 | Accrual          | 7.0    | 0.0       | 0.0      | 4.0  | 3.0       | 0.0          |

  @TestRailId:C3085 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation UC5: full, prior chargeback
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 500            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "500" EUR transaction amount
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "16 January 2024"
    And Customer makes "AUTOPAY" repayment on "16 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "20 January 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount for Payment nr. 1
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount for Payment nr. 2
    When Admin sets the business date to "31 January 2024"
    And Customer makes "AUTOPAY" repayment on "31 January 2024" with 375 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 125 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2024  | 04 January 2024  | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 125.0 | 0.0         |
      | 2  | 15   | 16 January 2024  | 16 January 2024  | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 15   | 31 January 2024  | 31 January 2024  | 125.0           | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 375.0 | 0.0        | 0.0   | 0.0         |
      | 4  | 15   | 15 February 2024 | 15 February 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 750.0         | 0.0      | 0.0  | 0.0       | 750.0 | 750.0 | 0.0        | 125.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 04 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 20 January 2024  | Chargeback       | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 20 January 2024  | Chargeback       | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 31 January 2024  | Repayment        | 375.0  | 375.0     | 0.0      | 0.0  | 0.0       | 125.0        |
      | 15 February 2024 | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 0.0          |

  @TestRailId:C3086 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation UC6: full, after chargeback
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 500            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "500" EUR transaction amount
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "16 January 2024"
    And Customer makes "AUTOPAY" repayment on "16 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "31 January 2024"
    And Customer makes "AUTOPAY" repayment on "31 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 125 EUR transaction amount
    When Admin sets the business date to "20 February 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount for Payment nr. 1
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount for Payment nr. 2
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount for Payment nr. 3
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount for Payment nr. 4
    When Admin sets the business date to "28 February 2024"
    And Customer makes "AUTOPAY" repayment on "28 February 2024" with 500 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2024  | 04 January 2024  | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 125.0 | 0.0         |
      | 2  | 15   | 16 January 2024  | 16 January 2024  | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 15   | 31 January 2024  | 31 January 2024  | 125.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
      | 4  | 15   | 15 February 2024 | 15 February 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
      | 5  | 5    | 20 February 2024 | 28 February 2024 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 500.0 | 0.0        | 500.0 | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 0.0        | 625.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 04 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 31 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 125.0        |
      | 15 February 2024 | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 20 February 2024 | Chargeback       | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 125.0        |
      | 20 February 2024 | Chargeback       | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 20 February 2024 | Chargeback       | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 20 February 2024 | Chargeback       | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 28 February 2024 | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 0.0          |

  @TestRailId:C3087 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation UC7: partial, after chargeback
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 500            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "500" EUR transaction amount
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "16 January 2024"
    And Customer makes "AUTOPAY" repayment on "16 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "31 January 2024"
    And Customer makes "AUTOPAY" repayment on "31 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 125 EUR transaction amount
    When Admin sets the business date to "20 February 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount for Payment nr. 2
    When Admin sets the business date to "28 February 2024"
    And Customer makes "AUTOPAY" repayment on "28 February 2024" with 125 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2024  | 04 January 2024  | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 125.0 | 0.0         |
      | 2  | 15   | 16 January 2024  | 16 January 2024  | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 15   | 31 January 2024  | 31 January 2024  | 125.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
      | 4  | 15   | 15 February 2024 | 15 February 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
      | 5  | 5    | 20 February 2024 | 28 February 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 125.0 | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 625.0         | 0.0      | 0.0  | 0.0       | 625.0 | 625.0 | 0.0        | 250.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 04 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 31 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 125.0        |
      | 15 February 2024 | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 20 February 2024 | Chargeback       | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 125.0        |
      | 28 February 2024 | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 0.0          |

  @TestRailId:C3088 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation UC8: multiple, prior chargebacks on same payment
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 500            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "500" EUR transaction amount
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "16 January 2024"
    And Customer makes "AUTOPAY" repayment on "16 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "20 January 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 75 EUR transaction amount for Payment nr. 1
    When Admin sets the business date to "25 January 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 50 EUR transaction amount for Payment nr. 1
    When Admin sets the business date to "31 January 2024"
    And Customer makes "AUTOPAY" repayment on "31 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 125 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2024  | 04 January 2024  | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 125.0 | 0.0         |
      | 2  | 15   | 16 January 2024  | 16 January 2024  | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 15   | 31 January 2024  | 31 January 2024  | 125.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 4  | 15   | 15 February 2024 | 15 February 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 625.0         | 0.0      | 0.0  | 0.0       | 625.0 | 625.0 | 0.0        | 125.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 04 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 20 January 2024  | Chargeback       | 75.0   | 75.0      | 0.0      | 0.0  | 0.0       | 325.0        |
      | 25 January 2024  | Chargeback       | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 375.0        |
      | 31 January 2024  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 125.0        |
      | 15 February 2024 | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 0.0          |

  @TestRailId:C3094 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC1: chargeback on principal
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 250 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Payment nr. 3
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |

  @TestRailId:C3095 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC2: chargeback on principal and fees
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 March 2024" due date and 30 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 280 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 250 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 280 EUR transaction amount for Payment nr. 3
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 30.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 280.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 280.0  |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 30.0  |        |

  @TestRailId:C3096 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC3: chargeback on principal and penalties
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 March 2024" due date and 30 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 280 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 250 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 280 EUR transaction amount for Payment nr. 3
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 30.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 280.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 280.0  |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 30.0  |        |

  @TestRailId:C3097 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC4: when overpayment amount is smaller than chargeback
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 400 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 150 overpaid amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Payment nr. 3
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | l1           | Overpayment account       |       | 150.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 400.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
      | LIABILITY | l1           | Overpayment account       | 150.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |

  @TestRailId:C3098 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC5: with fees- when overpayment amount is bigger than chargeback
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 March 2024" due date and 30 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 400 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 120 overpaid amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 100 EUR transaction amount for Payment nr. 3
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 220.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 30.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 280.0  |
      | LIABILITY | l1           | Overpayment account       |       | 120.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 400.0 |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "01 April 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 30.0  |        |
      | INCOME | 404007       | Fee Income              |       | 30.0   |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
      | LIABILITY | l1           | Overpayment account       | 100.0 |        |

  @TestRailId:C3099 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC6: when overpayment amount is bigger than chargeback
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 400 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 150 overpaid amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 100 EUR transaction amount for Payment nr. 3
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | l1           | Overpayment account       |       | 150.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 400.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
      | LIABILITY | l1           | Overpayment account       | 100.0 |        |

  @TestRailId:C3100 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC7: chargeback on charge-off loan account with principal
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 250 EUR transaction amount
    When Admin sets the business date to "15 March 2024"
    And Admin does charge-off the loan on "15 March 2024"
    When Admin sets the business date to "01 April 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Payment nr. 3
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 March 2024" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable     |       | 250.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 250.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 250.0 |        |

  @TestRailId:C3101 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC8: chargeback on charge-off loan account with principal and Fees
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 March 2024" due date and 30 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 280 EUR transaction amount
    When Admin sets the business date to "15 March 2024"
    And Admin does charge-off the loan on "15 March 2024"
    When Admin sets the business date to "01 April 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 280 EUR transaction amount for Payment nr. 3
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 30.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 280.0 |        |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 March 2024" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable     |       | 250.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 250.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 280.0  |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 250.0 |        |
      | INCOME    | 404008       | Fee Charge Off            | 30.0  |        |

  @TestRailId:C3102 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC9: chargeback on charge-off loan account with principal and Penalties
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 March 2024" due date and 30 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 280 EUR transaction amount
    When Admin sets the business date to "15 March 2024"
    And Admin does charge-off the loan on "15 March 2024"
    When Admin sets the business date to "01 April 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 280 EUR transaction amount for Payment nr. 3
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 30.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 280.0 |        |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 March 2024" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable     |       | 250.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 250.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 280.0  |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 250.0 |        |
      | INCOME    | 404008       | Fee Charge Off            | 30.0  |        |

  @TestRailId:C3111 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC10: chargeback on overpaid loan
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 100            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 January 2024" due date and 5 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 0.0  | 0.0        | 0.0  | 30.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 105 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
      | 01 February 2024 | Repayment        | 105.0  | 100.0     | 0.0      | 0.0  | 5.0       | 0.0          |
      | 01 February 2024 | Accrual          | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          |
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "01 March 2024"
    When Admin makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "19 January 2024" with 10 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 19 January 2024  | Merchant Issued Refund | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 90.0         | 0.0         |
      | 01 February 2024 | Repayment              | 105.0  | 90.0      | 0.0      | 0.0  | 5.0       | 0.0          | 10.0        |
      | 01 February 2024 | Accrual                | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
    Then Loan has 0 outstanding amount
    Then Loan has 10 overpaid amount
    Then Loan status will be "OVERPAID"
    When Admin sets the business date to "15 April 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 7 EUR transaction amount for Payment nr. 1
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "MERCHANT_ISSUED_REFUND" transaction with date "19 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 10.0  |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 90.0   |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 5.0    |
      | LIABILITY | l1           | Overpayment account       |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 105.0 |        |
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 5  | 14   | 15 April 2024    | 15 April 2024    | 0.0             | 2.0           | 0.0      | 0.0  | 5.0       | 7.0  | 7.0  | 0.0        | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 19 January 2024  | Merchant Issued Refund | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 90.0         | 0.0         |
      | 01 February 2024 | Repayment              | 105.0  | 90.0      | 0.0      | 0.0  | 5.0       | 0.0          | 10.0        |
      | 01 February 2024 | Accrual                | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 15 April 2024    | Chargeback             | 7.0    | 2.0       | 0.0      | 0.0  | 5.0       | 0.0          | 7.0         |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "15 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       | 7.0   |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 7.0    |
    Then Loan has 0 outstanding amount
    Then Loan has 3 overpaid amount
    Then Loan status will be "OVERPAID"
    When Admin sets the business date to "16 April 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_REFUND" chargeback with 7 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 5  | 15   | 16 April 2024    |                  | 0.0             | 9.0           | 0.0      | 0.0  | 5.0       | 14.0 | 10.0 | 0.0        | 0.0  | 4.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 19 January 2024  | Merchant Issued Refund | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 90.0         | 0.0         |
      | 01 February 2024 | Repayment              | 105.0  | 90.0      | 0.0      | 0.0  | 5.0       | 0.0          | 10.0        |
      | 01 February 2024 | Accrual                | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 15 April 2024    | Chargeback             | 7.0    | 2.0       | 0.0      | 0.0  | 5.0       | 0.0          | 7.0         |
      | 16 April 2024    | Chargeback             | 7.0    | 7.0       | 0.0      | 0.0  | 0.0       | 4.0          | 3.0         |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "16 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       | 3.0   |        |
      | ASSET     | 112601       | Loans Receivable          | 4.0   |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 7.0    |
    Then Loan has 4 outstanding amount
    Then Loan status will be "ACTIVE"

  @TestRailId:C3116 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC11: chargeback before maturity on payment with fee
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 100            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 January 2024" due date and 5 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 0.0  | 0.0        | 0.0  | 30.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 105 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
      | 01 February 2024 | Repayment        | 105.0  | 100.0     | 0.0      | 0.0  | 5.0       | 0.0          |
      | 01 February 2024 | Accrual          | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          |
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
#    ---Chargeback with amount < fee ---
    When Admin sets the business date to "01 March 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_REFUND" chargeback with 3 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    |                  | 0.0             | 25.0          | 0.0      | 0.0  | 3.0       | 28.0 | 25.0 | 25.0       | 0.0  | 3.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 0.0  | 8.0       | 108.0 | 105.0 | 50.0       | 25.0 | 3.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 01 February 2024 | Repayment        | 105.0  | 100.0     | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 01 February 2024 | Accrual          | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 01 March 2024    | Chargeback       | 3.0    | 0.0       | 0.0      | 0.0  | 3.0       | 0.0          | 0.0         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 105.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 3.0    |
      | ASSET     | 112603       | Interest/Fee Receivable   | 3.0   |        |
    Then Loan has 3 outstanding amount
    Then Loan status will be "ACTIVE"
#    --- Chargeback with amount > fee ---
    When Admin sets the business date to "05 March 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_REFUND" chargeback with 10 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    |                  | 0.0             | 33.0          | 0.0      | 0.0  | 5.0       | 38.0 | 25.0 | 25.0       | 0.0  | 13.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 108.0         | 0.0      | 0.0  | 10.0      | 118.0 | 105.0 | 50.0       | 25.0 | 13.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 01 February 2024 | Repayment        | 105.0  | 100.0     | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 01 February 2024 | Accrual          | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 01 March 2024    | Chargeback       | 3.0    | 0.0       | 0.0      | 0.0  | 3.0       | 0.0          | 0.0         |
      | 05 March 2024    | Chargeback       | 10.0   | 8.0       | 0.0      | 0.0  | 2.0       | 8.0          | 0.0         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 105.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 3.0    |
      | ASSET     | 112603       | Interest/Fee Receivable   | 3.0   |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "05 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 10.0   |
      | ASSET     | 112601       | Loans Receivable          | 8.0   |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 2.0   |        |
    Then Loan has 13 outstanding amount
    Then Loan status will be "ACTIVE"

  @TestRailId:C3117 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC12: chargeback after maturity on payment with fee
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 100            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 January 2024" due date and 5 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 0.0  | 0.0        | 0.0  | 30.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 105 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
      | 01 February 2024 | Repayment        | 105.0  | 100.0     | 0.0      | 0.0  | 5.0       | 0.0          |
      | 01 February 2024 | Accrual          | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          |
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
#    ---Chargeback with amount < fee ---
    When Admin sets the business date to "15 April 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_REFUND" chargeback with 3 EUR transaction amount for Payment nr. 1
    When Admin sets the business date to "16 April 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 5  | 14   | 15 April 2024    |                  | 0.0             | 0.0           | 0.0      | 0.0  | 3.0       | 3.0  | 0.0  | 0.0        | 0.0  | 3.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 0.0  | 8.0       | 108.0 | 105.0 | 50.0       | 25.0 | 3.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 01 February 2024 | Repayment        | 105.0  | 100.0     | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 01 February 2024 | Accrual          | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 15 April 2024    | Chargeback       | 3.0    | 0.0       | 0.0      | 0.0  | 3.0       | 0.0          | 0.0         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 105.0 |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "01 February 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 5.0   |        |
      | INCOME | 404007       | Fee Income              |       | 5.0    |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "15 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 3.0    |
      | ASSET     | 112603       | Interest/Fee Receivable   | 3.0   |        |
    Then Loan has 3 outstanding amount
    Then Loan status will be "ACTIVE"
#    --- Chargeback with amount > fee ---
    When Admin sets the business date to "20 April 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_REFUND" chargeback with 10 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 5  | 19   | 20 April 2024    |                  | 0.0             | 8.0           | 0.0      | 0.0  | 5.0       | 13.0 | 0.0  | 0.0        | 0.0  | 13.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 108.0         | 0.0      | 0.0  | 10.0      | 118.0 | 105.0 | 50.0       | 25.0 | 13.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 01 February 2024 | Repayment        | 105.0  | 100.0     | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 01 February 2024 | Accrual          | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 15 April 2024    | Chargeback       | 3.0    | 0.0       | 0.0      | 0.0  | 3.0       | 0.0          | 0.0         |
      | 20 April 2024    | Chargeback       | 10.0   | 8.0       | 0.0      | 0.0  | 2.0       | 8.0          | 0.0         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 105.0 |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "01 February 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 5.0   |        |
      | INCOME | 404007       | Fee Income              |       | 5.0    |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "15 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 3.0    |
      | ASSET     | 112603       | Interest/Fee Receivable   | 3.0   |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "20 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 10.0   |
      | ASSET     | 112601       | Loans Receivable          | 8.0   |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 2.0   |        |
    Then Loan has 13 outstanding amount
    Then Loan status will be "ACTIVE"

  @TestRailId:C3118 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC13: loan goes overpaid after maturity then chargeback applied on payment with fee
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 100            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 January 2024" due date and 5 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 0.0  | 0.0        | 0.0  | 30.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 105 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
      | 01 February 2024 | Repayment        | 105.0  | 100.0     | 0.0      | 0.0  | 5.0       | 0.0          |
      | 01 February 2024 | Accrual          | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          |
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
#    --- Loan goes overpaid after maturity ---
    When Admin sets the business date to "10 April 2024"
    When Admin makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "19 January 2024" with 10 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 0.0  | 5.0       | 105.0 | 105.0 | 50.0       | 25.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 19 January 2024  | Merchant Issued Refund | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 90.0         | 0.0         |
      | 01 February 2024 | Accrual                | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 01 February 2024 | Repayment              | 105.0  | 90.0      | 0.0      | 0.0  | 5.0       | 0.0          | 10.0        |
    Then Loan has 0 outstanding amount
    Then Loan has 10 overpaid amount
    Then Loan status will be "OVERPAID"
#    ---Chargeback with amount < overpaid amount and fee amount ---
    When Admin sets the business date to "15 April 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_REFUND" chargeback with 3 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 5  | 14   | 15 April 2024    | 15 April 2024    | 0.0             | 0.0           | 0.0      | 0.0  | 3.0       | 3.0  | 3.0  | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 0.0  | 8.0       | 108.0 | 108.0 | 50.0       | 25.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 19 January 2024  | Merchant Issued Refund | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 90.0         | 0.0         |
      | 01 February 2024 | Accrual                | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 01 February 2024 | Repayment              | 105.0  | 90.0      | 0.0      | 0.0  | 5.0       | 0.0          | 10.0        |
      | 15 April 2024    | Chargeback             | 3.0    | 0.0       | 0.0      | 0.0  | 3.0       | 0.0          | 3.0         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "MERCHANT_ISSUED_REFUND" transaction with date "19 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 10.0  |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "01 February 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 5.0   |        |
      | INCOME | 404007       | Fee Income              |       | 5.0    |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 90.0   |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 5.0    |
      | LIABILITY | l1           | Overpayment account       |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 105.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "15 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 3.0    |
      | LIABILITY | l1           | Overpayment account       | 3.0   |        |
    Then Loan has 0 outstanding amount
    Then Loan has 7 overpaid amount
    Then Loan status will be "OVERPAID"
#    --- Chargeback with amount > overpaid amount and fee amount ---
    When Admin sets the business date to "20 April 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_REFUND" chargeback with 10 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 5  | 19   | 20 April 2024    |                  | 0.0             | 8.0           | 0.0      | 0.0  | 5.0       | 13.0 | 10.0 | 0.0        | 0.0  | 3.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 108.0         | 0.0      | 0.0  | 10.0      | 118.0 | 115.0 | 50.0       | 25.0 | 3.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 19 January 2024  | Merchant Issued Refund | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 90.0         | 0.0         |
      | 01 February 2024 | Repayment              | 105.0  | 90.0      | 0.0      | 0.0  | 5.0       | 0.0          | 10.0        |
      | 01 February 2024 | Accrual                | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 15 April 2024    | Chargeback             | 3.0    | 0.0       | 0.0      | 0.0  | 3.0       | 0.0          | 3.0         |
      | 20 April 2024    | Chargeback             | 10.0   | 8.0       | 0.0      | 0.0  | 2.0       | 3.0          | 7.0         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "MERCHANT_ISSUED_REFUND" transaction with date "19 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 10.0  |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 90.0   |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 5.0    |
      | LIABILITY | l1           | Overpayment account       |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 105.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "15 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 3.0    |
      | LIABILITY | l1           | Overpayment account       | 3.0   |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "20 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 10.0   |
      | LIABILITY | l1           | Overpayment account       | 7.0   |        |
      | ASSET     | 112601       | Loans Receivable          | 3.0   |        |
    Then Loan has 3 outstanding amount
    Then Loan status will be "ACTIVE"

  @TestRailId:C3138
  Scenario: Verify chargeback function when reverse replay happens with Advanced payment allocation and set allocation rules for Chargeback transaction
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 100            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 January 2024" due date and 5 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 0.0  | 0.0        | 0.0  | 30.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin sets the business date to "05 January 2024"
    And Customer makes "AUTOPAY" repayment on "05 January 2024" with 28 EUR transaction amount
    When Admin sets the business date to "06 January 2024"
    And Customer makes "AUTOPAY" repayment on "06 January 2024" with 10 EUR transaction amount
    When Admin sets the business date to "07 January 2024"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 05 January 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 |                 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 13.0 | 13.0       | 0.0  | 17.0        |
      | 3  | 29   | 01 March 2024    |                 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |                 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 05 January 2024  | Repayment        | 28.0   | 25.0      | 0.0      | 0.0  | 3.0       | 75.0         | false    | false    |
      | 06 January 2024  | Repayment        | 10.0   | 8.0       | 0.0      | 0.0  | 2.0       | 67.0         | false    | false    |
    When Admin makes "REPAYMENT_ADJUSTMENT_REFUND" chargeback with 10 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 05 January 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 |                 | 50.0            | 33.0          | 0.0      | 0.0  | 7.0       | 40.0 | 13.0 | 13.0       | 0.0  | 27.0        |
      | 3  | 29   | 01 March 2024    |                 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |                 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 05 January 2024  | Repayment        | 28.0   | 25.0      | 0.0      | 0.0  | 3.0       | 75.0         | false    | false    |
      | 06 January 2024  | Repayment        | 10.0   | 8.0       | 0.0      | 0.0  | 2.0       | 67.0         | false    | false    |
      | 07 January 2024  | Chargeback       | 10.0   | 8.0       | 0.0      | 0.0  | 2.0       | 75.0         | false    | false    |
    When Customer undo "1"th repayment on "05 January 2024"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 10.0 | 0.0        | 10.0 | 15.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 35.0          | 0.0      | 0.0  | 5.0       | 40.0 | 0.0  | 0.0        | 0.0  | 40.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 05 January 2024  | Repayment        | 28.0   | 25.0      | 0.0      | 0.0  | 3.0       | 75.0         | true     | false    |
      | 06 January 2024  | Repayment        | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 90.0         | false    | true     |
      | 07 January 2024  | Chargeback       | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
    And Customer makes "AUTOPAY" repayment on "03 January 2024" with 25 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 03 January 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 |                 | 50.0            | 30.0          | 0.0      | 0.0  | 10.0      | 40.0 | 10.0 | 10.0       | 0.0  | 30.0        |
      | 3  | 29   | 01 March 2024    |                 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |                 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 03 January 2024  | Repayment        | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    | false    |
      | 05 January 2024  | Repayment        | 28.0   | 25.0      | 0.0      | 0.0  | 3.0       | 75.0         | true     | false    |
      | 06 January 2024  | Repayment        | 10.0   | 5.0       | 0.0      | 0.0  | 5.0       | 70.0         | false    | true     |
      | 07 January 2024  | Chargeback       | 10.0   | 5.0       | 0.0      | 0.0  | 5.0       | 75.0         | false    | true     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 105.0         | 0.0      | 0.0  | 10.0      | 115.0 | 35.0 | 10.0       | 25.0 | 80.0        |

  @TestRailId:C3406
  Scenario: Verify full chargeback before maturity date - no chargeback rule - interest recalculation disabled
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 33.63         | 0.39     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 117.01        | 2.05     | 0.0  | 0.0       | 119.06 | 34.02 | 0.0        | 0.0  | 85.04       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 84.06        | false    | false    |

  @TestRailId:C3407
  Scenario: Verify partial chargeback before maturity date-1 - no chargeback rule - interest recalculation disabled
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 15 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 31.62         | 0.39     | 0.0  | 0.0       | 32.01 | 0.0   | 0.0        | 0.0  | 32.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 115.0         | 2.05     | 0.0  | 0.0       | 117.05 | 34.02 | 0.0        | 0.0  | 83.03       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 82.05        | false    | false    |

  @TestRailId:C3408
  Scenario: Verify two chargebacks before maturity - no chargeback rule - interest recalculation disabled
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 15 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 31.62         | 0.39     | 0.0  | 0.0       | 32.01 | 0.0   | 0.0        | 0.0  | 32.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 115.0         | 2.05     | 0.0  | 0.0       | 117.05 | 34.02 | 0.0        | 0.0  | 83.03       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | 0.0         |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | 0.0         |
      | 01 March 2024    | Chargeback       | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 82.05        | 0.0         |
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 48.63         | 0.39     | 0.0  | 0.0       | 49.02 | 0.0   | 0.0        | 0.0  | 49.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 132.01        | 2.05     | 0.0  | 0.0       | 134.06 | 34.02 | 0.0        | 0.0  | 100.04      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 82.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 99.06        | false    | false    |

  @TestRailId:C3409
  Scenario: Verify full chargeback before maturity date on different business date - no chargeback rule - interest recalculation disabled
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "15 March 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 33.63         | 0.39     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 117.01        | 2.05     | 0.0  | 0.0       | 119.06 | 34.02 | 0.0        | 0.0  | 85.04       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 15 March 2024    | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 84.06        | false    | false    |

  @TestRailId:C3410
  Scenario: Verify full chargeback after maturity date - no chargeback rule - interest recalculation disabled
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 May 2024"
    And Customer makes "AUTOPAY" repayment on "01 May 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 June 2024"
    And Customer makes "AUTOPAY" repayment on "01 June 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 July 2024"
    And Customer makes "AUTOPAY" repayment on "01 July 2024" with 17.00 EUR transaction amount
    When Admin sets the business date to "15 July 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 5
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024     | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 July 2024     | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 17.0  | 0.0        | 0.0  | 0.0         |
      | 7  | 14   | 15 July 2024     |                  | 0.0             | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 117.01        | 2.05     | 0.0  | 0.0       | 119.06 | 102.05 | 0.0        | 0.0  | 17.01       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 50.43        | false    | false    |
      | 01 May 2024      | Repayment        | 17.01  | 16.72     | 0.29     | 0.0  | 0.0       | 33.71        | false    | false    |
      | 01 June 2024     | Repayment        | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 16.9         | false    | false    |
      | 01 July 2024     | Repayment        | 17.0   | 16.9      | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 July 2024     | Accrual          | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 July 2024     | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 17.01        | false    | false    |

  @TestRailId:C3411
  Scenario: Verify two chargebacks after maturity date - no chargeback rule - interest recalculation disabled
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 May 2024"
    And Customer makes "AUTOPAY" repayment on "01 May 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 June 2024"
    And Customer makes "AUTOPAY" repayment on "01 June 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 July 2024"
    And Customer makes "AUTOPAY" repayment on "01 July 2024" with 17.00 EUR transaction amount
    When Admin sets the business date to "15 July 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 5
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024     | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 July 2024     | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 17.0  | 0.0        | 0.0  | 0.0         |
      | 7  | 14   | 15 July 2024     |                  | 0.0             | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 117.01        | 2.05     | 0.0  | 0.0       | 119.06 | 102.05 | 0.0        | 0.0  | 17.01       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 50.43        | false    | false    |
      | 01 May 2024      | Repayment        | 17.01  | 16.72     | 0.29     | 0.0  | 0.0       | 33.71        | false    | false    |
      | 01 June 2024     | Repayment        | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 16.9         | false    | false    |
      | 01 July 2024     | Repayment        | 17.0   | 16.9      | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 July 2024     | Accrual          | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 July 2024     | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 17.01        | false    | false    |
    When Admin sets the business date to "15 July 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17 EUR transaction amount for Payment nr. 6
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024     | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 July 2024     | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 17.0  | 0.0        | 0.0  | 0.0         |
      | 7  | 14   | 15 July 2024     |                  | 0.0             | 34.01         | 0.0      | 0.0  | 0.0       | 34.01 | 0.0   | 0.0        | 0.0  | 34.01       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 134.01        | 2.05     | 0.0  | 0.0       | 136.06 | 102.05 | 0.0        | 0.0  | 34.01       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 50.43        | false    | false    |
      | 01 May 2024      | Repayment        | 17.01  | 16.72     | 0.29     | 0.0  | 0.0       | 33.71        | false    | false    |
      | 01 June 2024     | Repayment        | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 16.9         | false    | false    |
      | 01 July 2024     | Repayment        | 17.0   | 16.9      | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 July 2024     | Accrual          | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 July 2024     | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 17.01        | false    | false    |
      | 15 July 2024     | Chargeback       | 17.0   | 17.0      | 0.0      | 0.0  | 0.0       | 34.01        | false    | false    |

  @TestRailId:C3417
  Scenario: Full chargeback before the maturity date and interest recalculation DISABLED - Allocation priority: interest, fees, principal - UC1
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0     |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72	       | 0.29     | 0.0   | 0.0       | 17.01	| 0.0  | 0.0        | 0.0  | 17.01	     |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0   | 0.0       | 17.01	| 0.0  | 0.0        | 0.0  | 17.01	     |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0   | 0.0       | 17.0 	| 0.0  | 0.0        | 0.0  | 17.0 	     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05  | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "1 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0     | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0   | 0.0       | 17.01   | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0   | 0.0       | 17.01   | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0   | 0.0       | 17.01   | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72	      | 0.29     | 0.0   | 0.0       | 17.01   | 0.0   | 0.0        | 0.0  | 17.01	     |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0   | 0.0       | 17.01   | 0.0   | 0.0        | 0.0  | 17.01	     |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0   | 0.0       | 17.0    | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05  | 34.02 | 0          | 0    | 68.03     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount  | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement      | 100.0   | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment         | 17.01   | 16.43     | 0.58     | 0.0   | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment         | 17.01   | 16.52     | 0.49     | 0.0   | 0.0       | 67.05        | false    | false    |
# --- full chargeback for 2nd repayment made on  March, 1st --- #
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 33.14         | 0.88     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 116.52        | 2.54     | 0    | 0         | 119.06 | 34.02 | 0          | 0    | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 83.57        | false    | false    |

  @TestRailId:C3418
  Scenario: Partial chargeback before the maturity date and interest recalculation DISABLED - Allocation priority: interest, fees, principal - UC2
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "1 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 34.02 | 0          | 0    | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    # --- partial chargeback for 2nd repayment made on  March, 1st --- #
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 15 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 31.13         | 0.88     | 0.0  | 0.0       | 32.01 | 0.0   | 0.0        | 0.0  | 32.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 114.51        | 2.54     | 0    | 0         | 117.05 | 34.02 | 0          | 0    | 83.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 15.0   | 14.51     | 0.49     | 0.0  | 0.0       | 81.56        | false    | false    |

  @TestRailId:C3419
  Scenario: Partial chargeback before the maturity date and interest recalculation DISABLED - Allocation priority: principal, interest, fees - UC3
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_PRINCIPAL_INTEREST_FEE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "1 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 34.02 | 0          | 0    | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
# --- partial chargeback for 2nd repayment made on  March, 1st --- #
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 15 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 31.62         | 0.39     | 0.0  | 0.0       | 32.01 | 0.0   | 0.0        | 0.0  | 32.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 115.0         | 2.05     | 0    | 0         | 117.05 | 34.02 | 0          | 0    | 83.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 82.05        | false    | false    |

  @TestRailId:C3420
  Scenario: Two chargeback before the maturity date and interest recalculation DISABLED - Allocation priority: principal, interest, fees - UC4
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_PRINCIPAL_INTEREST_FEE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "1 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 34.02 | 0          | 0    | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
# --- partial chargeback for 1st repayment made on  February, 1st --- #
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 15 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 31.62         | 0.39     | 0.0  | 0.0       | 32.01 | 0.0   | 0.0        | 0.0  | 32.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 115.0         | 2.05     | 0    | 0         | 117.05 | 34.02 | 0          | 0    | 83.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 82.05        | false    | false    |
# --- full chargeback for 2nd repayment made on  March, 1st --- #
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 48.14         | 0.88     | 0.0  | 0.0       | 49.02 | 0.0   | 0.0        | 0.0  | 49.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 131.52        | 2.54     | 0    | 0         | 134.06 | 34.02 | 0          | 0    | 100.04      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 82.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 98.57        | false    | false    |

  @TestRailId:C3421
  Scenario: Full chargeback before the maturity date on different business date and interest recalculation DISABLED - Allocation priority: interest, fees, principal - UC5
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "1 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 34.02 | 0          | 0    | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
# --- full chargeback for 2nd repayment made on  March, 1st --- #
    When Admin sets the business date to "15 March 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 33.14         | 0.88     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 116.52        | 2.54     | 0    | 0         | 119.06 | 34.02 | 0          | 0    | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 15 March 2024    | Chargeback       | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 83.57        | false    | false    |

  @TestRailId:C3422
  Scenario: Full chargeback after the maturity date and interest recalculation DISABLED - Allocation priority: interest, fees, principal - UC6
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 May 2024"
    And Customer makes "AUTOPAY" repayment on "01 May 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 June 2024"
    And Customer makes "AUTOPAY" repayment on "01 June 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 July 2024"
    And Customer makes "AUTOPAY" repayment on "01 July 2024" with 17 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024     | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 July 2024     | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 17.0  | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 102.05 | 0          | 0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 50.43        | false    | false    |
      | 01 May 2024      | Repayment        | 17.01  | 16.72     | 0.29     | 0.0  | 0.0       | 33.71        | false    | false    |
      | 01 June 2024     | Repayment        | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 16.9         | false    | false    |
      | 01 July 2024     | Repayment        | 17.0   | 16.9      | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 July 2024     | Accrual          | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
# --- full chargeback for 5th repayment made on  June, 1st --- #
    When Admin sets the business date to "15 July 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 5
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024     | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 July 2024     | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 17.0  | 0.0        | 0.0  | 0.0         |
      | 7  | 14   | 15 July 2024     |                  | 0.0             | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 116.81        | 2.25     | 0    | 0         | 119.06 | 102.05 | 0          | 0    | 17.01       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 50.43        | false    | false    |
      | 01 May 2024      | Repayment        | 17.01  | 16.72     | 0.29     | 0.0  | 0.0       | 33.71        | false    | false    |
      | 01 June 2024     | Repayment        | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 16.9         | false    | false    |
      | 01 July 2024     | Repayment        | 17.0   | 16.9      | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 July 2024     | Accrual          | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 July 2024     | Chargeback       | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 16.81        | false    | false    |

  @TestRailId:C3423
  Scenario: Two chargebacks after the maturity date and interest recalculation DISABLED - Allocation priority: interest, fees, principal - UC7
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 May 2024"
    And Customer makes "AUTOPAY" repayment on "01 May 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 June 2024"
    And Customer makes "AUTOPAY" repayment on "01 June 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 July 2024"
    And Customer makes "AUTOPAY" repayment on "01 July 2024" with 17 EUR transaction amount
# --- full chargeback for 5th repayment made on  June, 1st --- #
    When Admin sets the business date to "15 July 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 5
# --- full chargeback for 6th repayment made on  July, 1st --- #
    When Admin sets the business date to "30 July 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17 EUR transaction amount for Payment nr. 6
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024     | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 July 2024     | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 17.0  | 0.0        | 0.0  | 0.0         |
      | 7  | 29   | 30 July 2024     |                  | 0.0             | 33.71         | 0.3      | 0.0  | 0.0       | 34.01 | 0.0   | 0.0        | 0.0  | 34.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 133.71        | 2.35     | 0    | 0         | 136.06 | 102.05 | 0          | 0    | 34.01       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 50.43        | false    | false    |
      | 01 May 2024      | Repayment        | 17.01  | 16.72     | 0.29     | 0.0  | 0.0       | 33.71        | false    | false    |
      | 01 June 2024     | Repayment        | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 16.9         | false    | false    |
      | 01 July 2024     | Repayment        | 17.0   | 16.9      | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 July 2024     | Accrual          | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 July 2024     | Chargeback       | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 16.81        | false    | false    |
      | 30 July 2024     | Chargeback       | 17.0   | 16.9      | 0.1      | 0.0  | 0.0       | 33.71        | false    | false    |

  @TestRailId:C3490
  Scenario: Full chargeback with no interest recalculation, interest allocation first, before maturity date
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_INTEREST_FIRST | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "1 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 34.02 | 0          | 0    | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 33.14         | 0.88     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 116.52        | 2.54     | 0    | 0         | 119.06 | 34.02 | 0          | 0    | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 83.57        | false    | false    |

  @TestRailId:C3491
  Scenario: Partial chargeback with no interest recalculation, interest allocation first, before maturity date
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_INTEREST_FIRST | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "1 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 34.02 | 0          | 0    | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 15 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 31.13         | 0.88     | 0.0  | 0.0       | 32.01 | 0.0   | 0.0        | 0.0  | 32.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 114.51        | 2.54     | 0    | 0         | 117.05 | 34.02 | 0          | 0    | 83.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 15.0   | 14.51     | 0.49     | 0.0  | 0.0       | 81.56        | false    | false    |

  @TestRailId:C3492
  Scenario: Partial chargeback with disabled interest recalculation, allocation principal first, before maturity date
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_PRINCIPAL_FIRST | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "1 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 34.02 | 0          | 0    | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 15 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 31.62         | 0.39     | 0.0  | 0.0       | 32.01 | 0.0   | 0.0        | 0.0  | 32.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 115.0         | 2.05     | 0    | 0         | 117.05 | 34.02 | 0          | 0    | 83.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 82.05        | false    | false    |

  @TestRailId:C3493
  Scenario: Two chargebacks with disabled interest recalculation, allocation principal first
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_PRINCIPAL_FIRST | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "1 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 34.02 | 0          | 0    | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 15 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 31.62         | 0.39     | 0.0  | 0.0       | 32.01 | 0.0   | 0.0        | 0.0  | 32.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 115.0         | 2.05     | 0    | 0         | 117.05 | 34.02 | 0          | 0    | 83.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 82.05        | false    | false    |
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 48.14         | 0.88     | 0.0  | 0.0       | 49.02 | 0.0   | 0.0        | 0.0  | 49.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 131.52        | 2.54     | 0    | 0         | 134.06 | 34.02 | 0          | 0    | 100.04      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 82.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 98.57        | false    | false    |

  @TestRailId:C3427
  Scenario: Verify full chargeback before maturity date - no chargeback rule - interest recalculation enabled
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.53           | 33.53         | 0.49     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.81           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.1      | 0.0  | 0.0       | 17.1  | 0.0   | 0.0        | 0.0  | 17.1        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 117.01        | 2.15     | 0.0  | 0.0       | 119.16 | 34.02 | 0.0        | 0.0  | 85.14       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 84.06        | false    | false    |

  @TestRailId:C3428
  Scenario: Verify partial chargeback before maturity date-1 - no chargeback rule - interest recalculation enabled
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 15 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.52           | 31.53         | 0.48     | 0.0  | 0.0       | 32.01 | 0.0   | 0.0        | 0.0  | 32.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.8            | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.99           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.99         | 0.1      | 0.0  | 0.0       | 17.09 | 0.0   | 0.0        | 0.0  | 17.09       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 115.0         | 2.14     | 0.0  | 0.0       | 117.14 | 34.02 | 0.0        | 0.0  | 83.12       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 82.05        | false    | false    |

  @TestRailId:C3429
  Scenario: Verify two chargebacks before maturity - no chargeback rule - interest recalculation enabled
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 15 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.52           | 31.53         | 0.48     | 0.0  | 0.0       | 32.01 | 0.0   | 0.0        | 0.0  | 32.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.8            | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.99           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.99         | 0.1      | 0.0  | 0.0       | 17.09 | 0.0   | 0.0        | 0.0  | 17.09       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 115.0         | 2.14     | 0.0  | 0.0       | 117.14 | 34.02 | 0.0        | 0.0  | 83.12       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | 0.0         |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | 0.0         |
      | 01 March 2024    | Chargeback       | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 82.05        | 0.0         |
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.62           | 48.44         | 0.58     | 0.0  | 0.0       | 49.02 | 0.0   | 0.0        | 0.0  | 49.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.91           | 16.71         | 0.3      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.1            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.1          | 0.1      | 0.0  | 0.0       | 17.2  | 0.0   | 0.0        | 0.0  | 17.2        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 132.01        | 2.25     | 0.0  | 0.0       | 134.26 | 34.02 | 0.0        | 0.0  | 100.24      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 82.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 99.06        | false    | false    |

  @TestRailId:C3430
  Scenario: Verify full chargeback before maturity date on different business date - no chargeback rule - interest recalculation enabled
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "15 March 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.49           | 33.57         | 0.45     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.77           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.96           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.96         | 0.1      | 0.0  | 0.0       | 17.06 | 0.0   | 0.0        | 0.0  | 17.06       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 117.01        | 2.11     | 0.0  | 0.0       | 119.12 | 34.02 | 0.0        | 0.0  | 85.1        |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 15 March 2024    | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 84.06        | false    | false    |

  @TestRailId:C3431
  Scenario: Verify full chargeback after maturity date - no chargeback rule - interest recalculation enabled
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 May 2024"
    And Customer makes "AUTOPAY" repayment on "01 May 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 June 2024"
    And Customer makes "AUTOPAY" repayment on "01 June 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 July 2024"
    And Customer makes "AUTOPAY" repayment on "01 July 2024" with 17.00 EUR transaction amount
    When Admin sets the business date to "15 July 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 5
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024     | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 July 2024     | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 17.0  | 0.0        | 0.0  | 0.0         |
      | 7  | 14   | 15 July 2024     |                  | 0.0             | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 117.01        | 2.05     | 0.0  | 0.0       | 119.06 | 102.05 | 0.0        | 0.0  | 17.01       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 50.43        | false    | false    |
      | 01 May 2024      | Repayment        | 17.01  | 16.72     | 0.29     | 0.0  | 0.0       | 33.71        | false    | false    |
      | 01 June 2024     | Repayment        | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 16.9         | false    | false    |
      | 01 July 2024     | Repayment        | 17.0   | 16.9      | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 July 2024     | Accrual          | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 July 2024     | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 17.01        | false    | false    |

  @TestRailId:C3432
  Scenario: Verify two chargebacks after maturity date - no chargeback rule - interest recalculation enabled
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 May 2024"
    And Customer makes "AUTOPAY" repayment on "01 May 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 June 2024"
    And Customer makes "AUTOPAY" repayment on "01 June 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 July 2024"
    And Customer makes "AUTOPAY" repayment on "01 July 2024" with 17.00 EUR transaction amount
    When Admin sets the business date to "15 July 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 5
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024     | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 July 2024     | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 17.0  | 0.0        | 0.0  | 0.0         |
      | 7  | 14   | 15 July 2024     |                  | 0.0             | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 117.01        | 2.05     | 0.0  | 0.0       | 119.06 | 102.05 | 0.0        | 0.0  | 17.01       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 50.43        | false    | false    |
      | 01 May 2024      | Repayment        | 17.01  | 16.72     | 0.29     | 0.0  | 0.0       | 33.71        | false    | false    |
      | 01 June 2024     | Repayment        | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 16.9         | false    | false    |
      | 01 July 2024     | Repayment        | 17.0   | 16.9      | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 July 2024     | Accrual          | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 July 2024     | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 17.01        | false    | false    |
    When Admin sets the business date to "15 July 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17 EUR transaction amount for Payment nr. 6
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024     | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 July 2024     | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 17.0  | 0.0        | 0.0  | 0.0         |
      | 7  | 14   | 15 July 2024     |                  | 0.0             | 34.01         | 0.0      | 0.0  | 0.0       | 34.01 | 0.0   | 0.0        | 0.0  | 34.01       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 134.01        | 2.05     | 0.0  | 0.0       | 136.06 | 102.05 | 0.0        | 0.0  | 34.01       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 50.43        | false    | false    |
      | 01 May 2024      | Repayment        | 17.01  | 16.72     | 0.29     | 0.0  | 0.0       | 33.71        | false    | false    |
      | 01 June 2024     | Repayment        | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 16.9         | false    | false    |
      | 01 July 2024     | Repayment        | 17.0   | 16.9      | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 July 2024     | Accrual          | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 July 2024     | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 17.01        | false    | false    |
      | 15 July 2024     | Chargeback       | 17.0   | 17.0      | 0.0      | 0.0  | 0.0       | 34.01        | false    | false    |

  @TestRailId:C3443
  Scenario: Verify that Accrual transactions are not created for the charged back interest - interest recalculation false + chargeback allocation rule is set
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_INTEREST_FIRST | 01 January 2024   | 2000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "2000" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "2000" EUR transaction amount
    And Admin sets the business date to "15 January 2024"
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 340.17 EUR transaction amount
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "15 February 2024"
    And Admin adds "LOAN_NSF_FEE" due date charge with "15 February 2024" due date and 5 EUR transaction amount
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "01 March 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 345.17 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 345.17 EUR transaction amount for Payment nr. 2
    And Admin sets the business date to "01 April 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 685.34 EUR transaction amount
    And Admin sets the business date to "01 May 2024"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 2000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2000.0       | false    | false    |
      | 14 January 2024  | Accrual          | 4.89   | 0.0       | 4.89     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Repayment        | 340.17 | 328.5     | 11.67    | 0.0  | 0.0       | 1671.5       | false    | false    |
      | 15 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Accrual          | 5.34   | 0.0       | 0.34     | 0.0  | 5.0       | 0.0          | false    | false    |
      | 16 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Repayment        | 345.17 | 330.42    | 9.75     | 0.0  | 5.0       | 1341.08      | false    | false    |
      | 01 March 2024    | Chargeback       | 345.17 | 330.42    | 9.75     | 0.0  | 5.0       | 1671.5       | false    | false    |
      | 01 March 2024    | Accrual          | 0.34   | 0.0       | 0.34     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 March 2024    | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 March 2024    | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 March 2024    | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 March 2024    | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 March 2024    | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2024    | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 March 2024    | Accrual          | 0.26   | 0.0       | 0.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Repayment        | 685.34 | 662.77    | 17.57    | 0.0  | 5.0       | 1008.73      | false    | false    |
      | 01 April 2024    | Accrual          | 0.25   | 0.0       | 0.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2024    | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2024    | Accrual          | 0.2    | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3445
  Scenario: Full chargeback before the maturity date with interest recalculation ENABLED - Allocation priority: interest, fees, principal - UC1
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_INTEREST_PENALTY_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0     |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72	       | 0.29     | 0.0   | 0.0       | 17.01	| 0.0  | 0.0        | 0.0  | 17.01	     |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0   | 0.0       | 17.01	| 0.0  | 0.0        | 0.0  | 17.01	     |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0   | 0.0       | 17.0 	| 0.0  | 0.0        | 0.0  | 17.0 	     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05  | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "1 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0     | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0   | 0.0       | 17.01   | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0   | 0.0       | 17.01   | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0   | 0.0       | 17.01   | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72	      | 0.29     | 0.0   | 0.0       | 17.01   | 0.0   | 0.0        | 0.0  | 17.01	     |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0   | 0.0       | 17.01   | 0.0   | 0.0        | 0.0  | 17.01	     |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0   | 0.0       | 17.0    | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05  | 34.02 | 0          | 0    | 68.03     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount  | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement      | 100.0   | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment         | 17.01   | 16.43     | 0.58     | 0.0   | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment         | 17.01   | 16.52     | 0.49     | 0.0   | 0.0       | 67.05        | false    | false    |
# --- full chargeback for 2nd repayment made on March, 1st --- #
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.53           | 33.04         | 0.98     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.81           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.1      | 0.0  | 0.0       | 17.1  | 0.0   | 0.0        | 0.0  | 17.1        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 116.52        | 2.64     | 0    | 0         | 119.16 | 34.02 | 0          | 0    | 85.14       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 83.57        | false    | false    |

  @TestRailId:C3446
  Scenario: Partial chargeback before the maturity date with interest recalculation ENABLED - Allocation priority: interest, fees, principal - UC2
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "1 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 34.02 | 0          | 0    | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
# --- partial chargeback for 2nd repayment made on  March, 1st --- #
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 15 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.52           | 31.04         | 0.97     | 0.0  | 0.0       | 32.01 | 0.0   | 0.0        | 0.0  | 32.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.8            | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.99           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.99         | 0.1      | 0.0  | 0.0       | 17.09 | 0.0   | 0.0        | 0.0  | 17.09       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 114.51        | 2.63     | 0    | 0         | 117.14 | 34.02 | 0          | 0    | 83.12       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 15.0   | 14.51     | 0.49     | 0.0  | 0.0       | 81.56        | false    | false    |

  @TestRailId:C3447
  Scenario: Partial chargeback before the maturity date with interest recalculation ENABLED - Allocation priority: principal, interest, fees - UC3
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_PRINCIPAL_INTEREST_FEE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "1 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 34.02 | 0          | 0    | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
# --- partial chargeback for 2nd repayment made on  March, 1st --- #
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 15 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.52           | 31.53         | 0.48     | 0.0  | 0.0       | 32.01 | 0.0   | 0.0        | 0.0  | 32.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.8            | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.99           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.99         | 0.1      | 0.0  | 0.0       | 17.09 | 0.0   | 0.0        | 0.0  | 17.09       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 115.0         | 2.14     | 0    | 0         | 117.14 | 34.02 | 0          | 0    | 83.12       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 82.05        | false    | false    |

