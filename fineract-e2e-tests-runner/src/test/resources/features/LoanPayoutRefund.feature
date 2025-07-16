Feature: PayoutRefund

  @TestRailId:C3845
  Scenario: Payout Refund with interestRefundCalculation = false (Interest Refund transaction should NOT be created)
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
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "15 July 2024" with 50 EUR transaction amount and system-generated Idempotency key and interestRefundCalculation false
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 July 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 August 2024    | 10 July 2024 | 663.52          | 336.48        | 2.42     | 0.0  | 0.0       | 338.9 | 338.9 | 338.9      | 0.0  | 0.0         |
      | 2  | 31   | 01 September 2024 |              | 333.42          | 330.1         | 8.8      | 0.0  | 0.0       | 338.9 | 50.0  | 50.0       | 0.0  | 288.9       |
      | 3  | 30   | 01 October 2024   |              | 0.0             | 333.42        | 2.78     | 0.0  | 0.0       | 336.2 | 0.0   | 0.0        | 0.0  | 336.2       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 14.0     | 0.0  | 0.0       | 1014.0 | 388.9  | 388.9      | 0.0  | 625.1       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 July 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 July 2024     | Repayment        | 338.9  | 336.48    | 2.42     | 0.0  | 0.0       | 663.52       | false    | false    |
      | 15 July 2024     | Payout Refund    | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 613.52       | false    | false    |

  @TestRailId:C3846
  Scenario: Payout Refund with interestRefundCalculation = true (Interest Refund transaction SHOULD be created)
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
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "15 July 2024" with 50 EUR transaction amount and system-generated Idempotency key and interestRefundCalculation true
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 July 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 August 2024    | 10 July 2024 | 663.52          | 336.48        | 2.42     | 0.0  | 0.0       | 338.9 | 338.9 | 338.9      | 0.0  | 0.0         |
      | 2  | 31   | 01 September 2024 |              | 333.42          | 330.1         | 8.8      | 0.0  | 0.0       | 338.9 | 50.19 | 50.19      | 0.0  | 288.71      |
      | 3  | 30   | 01 October 2024   |              | 0.0             | 333.42        | 2.78     | 0.0  | 0.0       | 336.2 | 0.0   | 0.0        | 0.0  | 336.2       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 14.0     | 0.0  | 0.0       | 1014.0 | 389.09 | 389.09     | 0.0  | 624.91      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 July 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 July 2024     | Repayment        | 338.9  | 336.48    | 2.42     | 0.0  | 0.0       | 663.52       | false    | false    |
      | 15 July 2024     | Payout Refund    | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 613.52       | false    | false    |
      | 15 July 2024     | Interest Refund  | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 613.52       | false    | false    |

  @TestRailId:C3847
  Scenario: Payout Refund without interestRefundCalculation (should fallback to loan product config)
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
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "15 July 2024" with 50 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 July 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 August 2024    | 10 July 2024 | 663.52          | 336.48        | 2.42     | 0.0  | 0.0       | 338.9 | 338.9 | 338.9      | 0.0  | 0.0         |
      | 2  | 31   | 01 September 2024 |              | 333.42          | 330.1         | 8.8      | 0.0  | 0.0       | 338.9 | 50.19 | 50.19      | 0.0  | 288.71      |
      | 3  | 30   | 01 October 2024   |              | 0.0             | 333.42        | 2.78     | 0.0  | 0.0       | 336.2 | 0.0   | 0.0        | 0.0  | 336.2       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 14.0     | 0.0  | 0.0       | 1014.0 | 389.09 | 389.09     | 0.0  | 624.91      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 July 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 July 2024     | Repayment        | 338.9  | 336.48    | 2.42     | 0.0  | 0.0       | 663.52       | false    | false    |
      | 15 July 2024     | Payout Refund    | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 613.52       | false    | false    |
      | 15 July 2024     | Interest Refund  | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 613.52       | false    | false    |

  @TestRailId:C3857
  Scenario: Verify reversal of Payout Refund with linked Interest Refund when subsequent transactions exist
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
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "10 July 2024" with 50 EUR transaction amount and system-generated Idempotency key and interestRefundCalculation true
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 July 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 August 2024    |           | 669.14          | 330.86        | 8.04     | 0.0  | 0.0       | 338.9  | 50.12 | 50.12      | 0.0  | 288.78      |
      | 2  | 31   | 01 September 2024 |           | 335.82          | 333.32        | 5.58     | 0.0  | 0.0       | 338.9  | 0.0   | 0.0        | 0.0  | 338.9       |
      | 3  | 30   | 01 October 2024   |           | 0.0             | 335.82        | 2.8      | 0.0  | 0.0       | 338.62 | 0.0   | 0.0        | 0.0  | 338.62      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 16.42    | 0.0  | 0.0       | 1016.42 | 50.12 | 50.12      | 0.0  | 966.3       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 July 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 July 2024     | Payout Refund    | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 950.0        | false    | false    |
      | 10 July 2024     | Interest Refund  | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 950.0        | false    | false    |
    When Admin sets the business date to "15 July 2024"
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "15 July 2024" with 100 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 July 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 August 2024    |           | 668.7           | 331.3         | 7.6      | 0.0  | 0.0       | 338.9  | 150.12 | 150.12     | 0.0  | 188.78      |
      | 2  | 31   | 01 September 2024 |           | 335.37          | 333.33        | 5.57     | 0.0  | 0.0       | 338.9  | 0.0    | 0.0        | 0.0  | 338.9       |
      | 3  | 30   | 01 October 2024   |           | 0.0             | 335.37        | 2.79     | 0.0  | 0.0       | 338.16 | 0.0    | 0.0        | 0.0  | 338.16      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 15.96    | 0.0  | 0.0       | 1015.96 | 150.12 | 150.12     | 0.0  | 865.84      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 July 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 July 2024     | Payout Refund    | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 950.0        | false    | false    |
      | 10 July 2024     | Interest Refund  | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 950.0        | false    | false    |
      | 15 July 2024     | Repayment        | 100.0  | 96.42     | 3.58     | 0.0  | 0.0       | 853.58       | false    | false    |
    Then In Loan Transactions the "3"th Transaction has relationship type=RELATED with the "2"th Transaction
    When Customer undo "1"th "Payout Refund" transaction made on "10 July 2024" with linked "Interest Refund" transaction
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 July 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 August 2024    |           | 668.99          | 331.01        | 7.89     | 0.0  | 0.0       | 338.9  | 100.0 | 100.0      | 0.0  | 238.9       |
      | 2  | 31   | 01 September 2024 |           | 335.66          | 333.33        | 5.57     | 0.0  | 0.0       | 338.9  | 0.0   | 0.0        | 0.0  | 338.9       |
      | 3  | 30   | 01 October 2024   |           | 0.0             | 335.66        | 2.8      | 0.0  | 0.0       | 338.46 | 0.0   | 0.0        | 0.0  | 338.46      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 16.26    | 0.0  | 0.0       | 1016.26 | 100.0 | 100.0      | 0.0  | 916.26      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 July 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 10 July 2024     | Payout Refund    | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 950.0        | true     | false    |
      | 10 July 2024     | Interest Refund  | 0.12   | 0.0       | 0.12     | 0.0  | 0.0       | 950.0        | true     | false    |
      | 15 July 2024     | Repayment        | 100.0  | 96.24     | 3.76     | 0.0  | 0.0       | 903.76        | false    | true     |