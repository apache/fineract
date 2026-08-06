@ReportingFeature
Feature: Reporting

  @TestRailId:C4686
  Scenario: Verify Transaction Summary Reports contain all buydown fee transaction types
    When Admin sets the business date to "01 January 2024"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "01 January 2024" with "50" EUR transaction amount
    And Admin sets the business date to "31 January 2024"
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "01 February 2024"
    And Admin runs inline COB job for Loan
    And Admin adds buy down fee adjustment with "AUTOPAY" payment type to the loan on "01 February 2024" with "10" EUR transaction amount
    And Admin sets the business date to "02 February 2024"
    And Admin adds buy down fee adjustment of buy down fee transaction made on "01 January 2024" with "AUTOPAY" payment type to the loan on "10 January 2024" with "25" EUR transaction amount
    And Admin runs inline COB job for Loan
#    --- Transaction Summary Report ---
    Then Transaction Summary Report for date "01 January 2024" has the following data:
      | TransactionDate | Product                                                  | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee         | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee         | AUTOPAY          |            | 0        | Interest                 |                      | 50.0               |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee         | AUTOPAY          |            | 0        | Principal                |                      | 0.0                |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement         |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement         |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement         |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement         |                  |            | 0        | Principal                |                      | 100.0              |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement         |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    And Transaction Summary Report for date "31 January 2024" has the following data:
      | TransactionDate | Product                                                  | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Apply Charges             |                  |            | 0        | Interest                 |                      |                    |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Interest                 |                      | 0.55               |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    And Transaction Summary Report for date "01 February 2024" has the following data:
      | TransactionDate | Product                                                  | TransactionType_Name                 | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Apply Charges                        |                  |            | 0        | Interest                 |                      |                    |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Adjustment              | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Adjustment              | AUTOPAY          |            | 0        | Interest                 |                      | -10.0              |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Adjustment              | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Adjustment              | AUTOPAY          |            | 0        | Principal                |                      | 0.0                |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Adjustment              | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization Adjustment |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization Adjustment |                  |            | 0        | Interest                 |                      | -2.03              |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization Adjustment |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization Adjustment |                  |            | 0        | Principal                |                      | 0.0                |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization Adjustment |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
#   --- Transaction Summary Report with Asset Owner ---
    And Transaction Summary Report with Asset Owner for date "01 January 2024" has the following data:
      | TransactionDate | Product                                                  | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee         | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |                |                     |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee         | AUTOPAY          |            | 0        | Interest                 |                      | 50.0               |                |                     |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee         | AUTOPAY          |            | 0        | Principal                |                      | 0.0                |                |                     |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement         |                  |            | 0        | Fees                     |                      | 0.0                |                |                     |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement         |                  |            | 0        | Interest                 |                      | 0.0                |                |                     |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement         |                  |            | 0        | Penalty                  |                      | 0.0                |                |                     |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement         |                  |            | 0        | Principal                |                      | 100.0              |                |                     |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement         |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |
    And Transaction Summary Report with Asset Owner for date "31 January 2024" has the following data:
      | TransactionDate | Product                                                  | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Apply Charges             |                  |            | 0        | Interest                 |                      |                    |                |                     |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Fees                     |                      | 0.0                |                |                     |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Interest                 |                      | 0.55               |                |                     |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |                |                     |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |                |                     |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |
    And Transaction Summary Report with Asset Owner for date "01 February 2024" has the following data:
      | TransactionDate | Product                                                  | TransactionType_Name                 | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Apply Charges                        |                  |            | 0        | Interest                 |                      |                    |                |                     |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Adjustment              | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |                |                     |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Adjustment              | AUTOPAY          |            | 0        | Interest                 |                      | -10.0              |                |                     |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Adjustment              | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Adjustment              | AUTOPAY          |            | 0        | Principal                |                      | 0.0                |                |                     |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Adjustment              | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization Adjustment |                  |            | 0        | Fees                     |                      | 0.0                |                |                     |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization Adjustment |                  |            | 0        | Interest                 |                      | -2.03              |                |                     |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization Adjustment |                  |            | 0        | Penalty                  |                      | 0.0                |                |                     |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization Adjustment |                  |            | 0        | Principal                |                      | 0.0                |                |                     |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization Adjustment |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |

  @TestRailId:C4687
  Scenario: Verify Transaction Summary Reports with buyDownFeeIncomeType = FEE
    When Admin sets the business date to "01 January 2024"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "01 January 2024" with "50" EUR transaction amount
    And Admin sets the business date to "31 January 2024"
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "01 February 2024"
    And Admin runs inline COB job for Loan
    And Admin adds buy down fee adjustment with "AUTOPAY" payment type to the loan on "01 February 2024" with "10" EUR transaction amount
    And Admin sets the business date to "02 February 2024"
    And Admin adds buy down fee adjustment of buy down fee transaction made on "01 January 2024" with "AUTOPAY" payment type to the loan on "10 January 2024" with "25" EUR transaction amount
    And Admin runs inline COB job for Loan
#    --- Transaction Summary Report ---
    Then Transaction Summary Report for date "01 January 2024" has the following data:
      | TransactionDate | Product                                                             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee         | AUTOPAY          |            | 0        | Fees                     |                      | 50.0               |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee         | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee         | AUTOPAY          |            | 0        | Principal                |                      | 0.0                |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Disbursement         |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Disbursement         |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Disbursement         |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Disbursement         |                  |            | 0        | Principal                |                      | 100.0              |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Disbursement         |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    And Transaction Summary Report for date "31 January 2024" has the following data:
      | TransactionDate | Product                                                             | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Apply Charges             |                  |            | 0        | Interest                 |                      |                    |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Fees                     |                      | 0.55               |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    And Transaction Summary Report for date "01 February 2024" has the following data:
      | TransactionDate | Product                                                             | TransactionType_Name                 | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Apply Charges                        |                  |            | 0        | Interest                 |                      |                    |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Adjustment              | AUTOPAY          |            | 0        | Fees                     |                      | -10.0              |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Adjustment              | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Adjustment              | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Adjustment              | AUTOPAY          |            | 0        | Principal                |                      | 0.0                |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Adjustment              | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization Adjustment |                  |            | 0        | Fees                     |                      | -2.03              |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization Adjustment |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization Adjustment |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization Adjustment |                  |            | 0        | Principal                |                      | 0.0                |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization Adjustment |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
#    --- Transaction Summary Report with Asset Owner ---
    And Transaction Summary Report with Asset Owner for date "01 January 2024" has the following data:
      | TransactionDate | Product                                                             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee         | AUTOPAY          |            | 0        | Fees                     |                      | 50.0               |                |                     |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee         | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee         | AUTOPAY          |            | 0        | Principal                |                      | 0.0                |                |                     |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Disbursement         |                  |            | 0        | Fees                     |                      | 0.0                |                |                     |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Disbursement         |                  |            | 0        | Interest                 |                      | 0.0                |                |                     |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Disbursement         |                  |            | 0        | Penalty                  |                      | 0.0                |                |                     |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Disbursement         |                  |            | 0        | Principal                |                      | 100.0              |                |                     |
      | 2024-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Disbursement         |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |
    And Transaction Summary Report with Asset Owner for date "31 January 2024" has the following data:
      | TransactionDate | Product                                                             | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Apply Charges             |                  |            | 0        | Interest                 |                      |                    |                |                     |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Fees                     |                      | 0.55               |                |                     |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Interest                 |                      | 0.0                |                |                     |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |                |                     |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |                |                     |
      | 2024-01-31      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |
    And Transaction Summary Report with Asset Owner for date "01 February 2024" has the following data:
      | TransactionDate | Product                                                             | TransactionType_Name                 | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Apply Charges                        |                  |            | 0        | Interest                 |                      |                    |                |                     |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Adjustment              | AUTOPAY          |            | 0        | Fees                     |                      | -10.0              |                |                     |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Adjustment              | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Adjustment              | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Adjustment              | AUTOPAY          |            | 0        | Principal                |                      | 0.0                |                |                     |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Adjustment              | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization Adjustment |                  |            | 0        | Fees                     |                      | -2.03              |                |                     |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization Adjustment |                  |            | 0        | Interest                 |                      | 0.0                |                |                     |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization Adjustment |                  |            | 0        | Penalty                  |                      | 0.0                |                |                     |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization Adjustment |                  |            | 0        | Principal                |                      | 0.0                |                |                     |
      | 2024-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization Adjustment |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |

  @TestRailId:C4688
  Scenario: Verify Transaction Summary Reports with Buydown fee - happy path
    When Admin sets the business date to "01 February 2026"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 February 2026  | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 February 2026" with "100" amount and expected disbursement date on "01 February 2026"
    And Admin successfully disburse the loan on "01 February 2026" with "100" EUR transaction amount
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "01 February 2026" with "50" EUR transaction amount
    And Admin sets the business date to "02 February 2026"
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "03 February 2026"
    And Admin runs inline COB job for Loan
    And Admin adds buy down fee adjustment with "AUTOPAY" payment type to the loan on "03 February 2026" with "25" EUR transaction amount
    And Admin sets the business date to "04 February 2026"
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "05 February 2026"
    And Admin runs inline COB job for Loan
#    --- Transaction Summary Report with Asset Owner ---
    Then Transaction Summary Report with Asset Owner for date "01 February 2026" has the following data:
      | TransactionDate | Product                                                  | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee              | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee              | AUTOPAY          |            | 0        | Interest                 |                      | 50.0               |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee              | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee              | AUTOPAY          |            | 0        | Principal                |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee              | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Interest                 |                      | 0.28               |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement              | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement              | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement              | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement              | AUTOPAY          |            | 0        | Principal                |                      | 100.0              |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement              | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    And Transaction Summary Report with Asset Owner for date "02 February 2026" has the following data:
      | TransactionDate | Product                                                  | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-02-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Apply Charges             |                  |            | 0        | Interest                 |                      | 0.02               |
      | 2026-02-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-02-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Interest                 |                      | 0.27               |
      | 2026-02-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-02-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-02-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    And Transaction Summary Report with Asset Owner for date "03 February 2026" has the following data:
      | TransactionDate | Product                                                  | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Apply Charges             |                  |            | 0        | Interest                 |                      | 0.02               |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Adjustment   | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Adjustment   | AUTOPAY          |            | 0        | Interest                 |                      | -25.0              |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Adjustment   | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Adjustment   | AUTOPAY          |            | 0        | Principal                |                      | 0.0                |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Adjustment   | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Interest                 |                      | 0.14               |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
#    --- Transaction Summary Report ---
    And Transaction Summary Report for date "01 February 2026" has the following data:
      | TransactionDate | Product                                                  | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee              | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee              | AUTOPAY          |            | 0        | Interest                 |                      | 50.0               |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee              | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee              | AUTOPAY          |            | 0        | Principal                |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee              | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Interest                 |                      | 0.28               |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement              | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement              | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement              | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement              | AUTOPAY          |            | 0        | Principal                |                      | 100.0              |
      | 2026-02-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement              | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    And Transaction Summary Report for date "02 February 2026" has the following data:
      | TransactionDate | Product                                                  | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-02-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Apply Charges             |                  |            | 0        | Interest                 |                      | 0.02               |
      | 2026-02-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-02-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Interest                 |                      | 0.27               |
      | 2026-02-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-02-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-02-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    And Transaction Summary Report for date "03 February 2026" has the following data:
      | TransactionDate | Product                                                  | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Apply Charges             |                  |            | 0        | Interest                 |                      | 0.02               |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Adjustment   | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Adjustment   | AUTOPAY          |            | 0        | Interest                 |                      | -25.0              |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Adjustment   | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Adjustment   | AUTOPAY          |            | 0        | Principal                |                      | 0.0                |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Adjustment   | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Interest                 |                      | 0.14               |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-02-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |

  @TestRailId:C85202
  Scenario: Verify Transaction Summary Report with Asset Owner contain correct originator_external_ids column
    When Admin sets the business date to "01 January 2025"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a new loan originator with external ID and name "Report Test Originator"
    And Admin creates a new default Loan with date: "01 January 2025"
    And Admin attaches the originator to the loan
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    Then Transaction Summary Report with Asset Owner for date "01 January 2025" column "Originator_External_Ids" has non-empty value for all rows
    And Transaction Summary Report with Asset Owner for date "01 January 2025" column "From_asset_owner_id" has empty value for all rows
    And Transaction Summary Report with Asset Owner for date "01 January 2025" column "Asset_owner_id" has empty value for all rows

  @TestRailId:C83086
  Scenario: Verify Transaction Summary Report with Asset Owner - UC01: Buydown fee amortization with multiple buydown fees should not duplicate
    When Admin sets the business date to "01 January 2026"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 January 2026   | 100            | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 100               | DAYS                  | 25             | DAYS                   | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "100" EUR transaction amount
    And Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Loan
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "02 January 2026" with "50" EUR transaction amount
    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Loan
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 January 2026" with "25" EUR transaction amount
    And Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Loan
    Then Transaction Summary Report with Asset Owner for date "02 January 2026" has the following data:
      | TransactionDate | Product                                                  | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Apply Charges             |                  |            | 0        | Interest                 |                      | 0.03               |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee              | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee              | AUTOPAY          |            | 0        | Interest                 |                      | 50.0               |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee              | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee              | AUTOPAY          |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee              | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Interest                 |                      | 0.51               |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    Then Transaction Summary Report with Asset Owner for date "03 January 2026" has the following data:
      | TransactionDate | Product                                                  | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Apply Charges             |                  |            | 0        | Interest                 |                      | 0.04               |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee              | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee              | AUTOPAY          |            | 0        | Interest                 |                      | 25.0               |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee              | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee              | AUTOPAY          |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee              | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Interest                 |                      | 0.76               |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    Then Transaction Summary Report with Asset Owner for date "04 January 2026" has the following data:
      | TransactionDate | Product                                                  | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-04      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Apply Charges             |                  |            | 0        | Interest                 |                      | 0.03               |
      | 2026-01-04      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-04      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Interest                 |                      | 0.76               |
      | 2026-01-04      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-04      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-04      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |

  @TestRailId:C83087
  Scenario: Verify Transaction Summary Report with Asset Owner - UC02: Buydown fee amortization with FEE_INCOME allocation type should not duplicate
    When Admin sets the business date to "01 January 2026"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | 01 January 2026   | 100            | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 100               | DAYS                  | 25             | DAYS                   | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "100" EUR transaction amount
    And Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Loan
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "02 January 2026" with "50" EUR transaction amount
    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Loan
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 January 2026" with "25" EUR transaction amount
    And Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Loan
    Then Transaction Summary Report with Asset Owner for date "02 January 2026" has the following data:
      | TransactionDate | Product                                                             | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Apply Charges             |                  |            | 0        | Interest                 |                      | 0.03               |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee              | AUTOPAY          |            | 0        | Fees                     |                      | 50.0               |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee              | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee              | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee              | AUTOPAY          |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee              | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Fees                     |                      | 0.51               |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    Then Transaction Summary Report with Asset Owner for date "03 January 2026" has the following data:
      | TransactionDate | Product                                                             | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Apply Charges             |                  |            | 0        | Interest                 |                      | 0.04               |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee              | AUTOPAY          |            | 0        | Fees                     |                      | 25.0               |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee              | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee              | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee              | AUTOPAY          |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee              | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Fees                     |                      | 0.76               |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    Then Transaction Summary Report with Asset Owner for date "04 January 2026" has the following data:
      | TransactionDate | Product                                                             | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-04      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Apply Charges             |                  |            | 0        | Interest                 |                      | 0.03               |
      | 2026-01-04      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Fees                     |                      | 0.76               |
      | 2026-01-04      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-04      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-04      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-04      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_FEE_INCOME | Buy Down Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |

  @TestRailId:C83088
  Scenario: Verify Transaction Summary Report with Asset Owner - UC03: Capitalized income amortization with multiple capitalized income transactions should not duplicate
    When Admin sets the business date to "01 January 2026"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | 01 January 2026   | 100            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 100               | DAYS                  | 25             | DAYS                   | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "25" EUR transaction amount
    And Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Loan
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "02 January 2026" with "50" EUR transaction amount
    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Loan
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "03 January 2026" with "25" EUR transaction amount
    And Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Loan
    Then Transaction Summary Report with Asset Owner for date "02 January 2026" has the following data:
      | TransactionDate | Product                                                        | TransactionType_Name            | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income              | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income              | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income              | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income              | AUTOPAY          |            | 0        | Principal                |                      | 50.0               |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income              | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income Amortization |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income Amortization |                  |            | 0        | Interest                 |                      | 0.51               |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income Amortization |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-02      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    Then Transaction Summary Report with Asset Owner for date "03 January 2026" has the following data:
      | TransactionDate | Product                                                        | TransactionType_Name            | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income              | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income              | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income              | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income              | AUTOPAY          |            | 0        | Principal                |                      | 25.0               |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income              | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income Amortization |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income Amortization |                  |            | 0        | Interest                 |                      | 0.76               |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income Amortization |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    Then Transaction Summary Report with Asset Owner for date "04 January 2026" has the following data:
      | TransactionDate | Product                                                        | TransactionType_Name            | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-04      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income Amortization |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-04      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income Amortization |                  |            | 0        | Interest                 |                      | 0.76               |
      | 2026-01-04      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-04      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income Amortization |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-04      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | Capitalized Income Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |

  @TestRailId:C85203
  Scenario: Verify Verify Transaction Summary Report with Asset Owner with previous owner for intermediarySale transfer with following SALES request and originatorId - UC1
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DELAYED_SETTLEMENT" for loan product "LP1_DUE_DATE"
    When Admin sets the business date to "1 May 2023"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    When Admin creates a new loan originator with external ID and name "Asset Owner with intermediarySale transfer and SALES request"
    When Admin creates a fully customized loan with the following data:
      | LoanProduct  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_DUE_DATE | 01 May 2023       | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    When Admin attaches the originator to the loan
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | intermediarySale | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 9999-12-31  | INTERMEDIARYSALE |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 9999-12-31  | INTERMEDIARYSALE |
    Then LoanOwnershipTransferBusinessEvent with transfer type: "INTERMEDIARYSALE" and transfer asset owner is created
    When Admin sets the business date to "14 June 2023"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-06-14     | 1                  |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 9999-12-31  | INTERMEDIARYSALE |
      | 2023-06-14     | 1                  | PENDING              | 2023-06-14    | 9999-12-31  | SALE             |
    When Admin sets the business date to "15 June 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 4 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 2023-06-14  | INTERMEDIARYSALE |
      | 2023-06-14     | 1                  | PENDING              | 2023-06-14    | 2023-06-14  | SALE             |
      | 2023-06-14     | 1                  | ACTIVE               | 2023-06-15    | 9999-12-31  | SALE             |
    Then LoanOwnershipTransferBusinessEvent with transfer type: "SALE" and transfer asset owner based on intermediarySale is created
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DEFAULT_SETTLEMENT" for loan product "LP1_DUE_DATE"
# --- run Journal Entry Aggregation job ---- #
    And Admin runs the "JOURNAL_ENTRY_AGGREGATION" job
# --- run Transaction Summary Report with Asset Owner report --- #
    Then Transaction Summary Report with Asset Owner for date "01 May 2023" has originatorId, asset owner externalId and the following data:
      | TransactionDate | Product      | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | asset_owner_id    | from_asset_owner_id       | originator_external_ids |
      | 2023-05-01      | LP1_DUE_DATE | Disbursement              | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |                   |                           | originator_external_id  |
      | 2023-05-01      | LP1_DUE_DATE | Disbursement              | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                   |                           | originator_external_id  |
      | 2023-05-01      | LP1_DUE_DATE | Disbursement              | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                   |                           | originator_external_id  |
      | 2023-05-01      | LP1_DUE_DATE | Disbursement              | AUTOPAY          |            | 0        | Principal                |                      | 1000.0             |                   |                           | originator_external_id  |
      | 2023-05-01      | LP1_DUE_DATE | Disbursement              | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                   |                           | originator_external_id  |
    Then Transaction Summary Report with Asset Owner for date "14 June 2023" has originatorId, asset owner externalId and the following data:
      | TransactionDate | Product      | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | asset_owner_id    | from_asset_owner_id        | originator_external_ids |
      | 2023-06-14      | LP1_DUE_DATE | Asset Transfer            |                  |            | 0        | Principal                |                      | 1000.0             | owner_external_id | previous_owner_external_id | originator_external_id  |

  @TestRailId:C85204
  Scenario: Verify Trial Balance Summary Report with Asset Owner with previous owner for intermediarySale transfer with following SALES and BUYBACK requests and originatorId - UC2
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DELAYED_SETTLEMENT" for loan product "LP1_DUE_DATE"
    When Admin sets the business date to "1 May 2023"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    When Admin creates a new loan originator with external ID and name "Asset Owner with intermediarySale transfer and SALES and BUYBACK request"
    When Admin creates a fully customized loan with the following data:
      | LoanProduct  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_DUE_DATE | 01 May 2023       | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    When Admin attaches the originator to the loan
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | intermediarySale | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 9999-12-31  | INTERMEDIARYSALE |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 9999-12-31  | INTERMEDIARYSALE |
    Then LoanOwnershipTransferBusinessEvent with transfer type: "INTERMEDIARYSALE" and transfer asset owner is created
    When Admin sets the business date to "14 June 2023"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-06-14     | 1                  |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 9999-12-31  | INTERMEDIARYSALE |
      | 2023-06-14     | 1                  | PENDING              | 2023-06-14    | 9999-12-31  | SALE             |
    When Admin sets the business date to "15 June 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 4 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 2023-06-14  | INTERMEDIARYSALE |
      | 2023-06-14     | 1                  | PENDING              | 2023-06-14    | 2023-06-14  | SALE             |
      | 2023-06-14     | 1                  | ACTIVE               | 2023-06-15    | 9999-12-31  | SALE             |
    Then LoanOwnershipTransferBusinessEvent with transfer type: "SALE" and transfer asset owner based on intermediarySale is created
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-06-16     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 5 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 2023-06-14  | INTERMEDIARYSALE |
      | 2023-06-14     | 1                  | PENDING              | 2023-06-14    | 2023-06-14  | SALE             |
      | 2023-06-14     | 1                  | ACTIVE               | 2023-06-15    | 9999-12-31  | SALE             |
      | 2023-06-16     | 1                  | BUYBACK              | 2023-06-15    | 9999-12-31  | BUYBACK          |
    When Admin sets the business date to "17 June 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 5 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 2023-06-14  | INTERMEDIARYSALE |
      | 2023-06-14     | 1                  | PENDING              | 2023-06-14    | 2023-06-14  | SALE             |
      | 2023-06-14     | 1                  | ACTIVE               | 2023-06-15    | 2023-06-16  | SALE             |
      | 2023-06-16     | 1                  | BUYBACK              | 2023-06-15    | 2023-06-16  | BUYBACK          |
    Then LoanOwnershipTransferBusinessEvent with transfer type: "BUYBACK" and transfer asset owner is created
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DEFAULT_SETTLEMENT" for loan product "LP1_DUE_DATE"
# --- run Journal Entry Aggregation job ---- #
    And Admin runs the "JOURNAL_ENTRY_AGGREGATION" job
# --- run Trial Balance Summary Report with Asset Owner report --- #
    Then Trial Balance Summary Report with Asset Owner for date "01 May 2023" has originatorId, asset owner externalId and the following data:
      | PostingDate     | Product      | glAcct   | Description               | AssetOwner                 | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2023-05-01      | LP1_DUE_DATE | 112601   | Loans Receivable          | self                       | 0.0              | 1000.0        | 0.0            | 1000.0        | originator_external_id  |
      | 2023-05-01      | LP1_DUE_DATE | 145023   | Suspense/Clearing account | self                       | 0.0              | 0.0           | -1000.0        | -1000.0       | originator_external_id  |
    Then Trial Balance Summary Report with Asset Owner for date "14 June 2023" has originatorId, asset owner externalId and the following data:
      | PostingDate     | Product      | glAcct   | Description               | AssetOwner                 | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2023-06-14      | LP1_DUE_DATE | 112601   | Loans Receivable          | owner_external_id          | 0.0              | 1000.0        | 0.0            | 1000.0        | originator_external_id  |
      | 2023-06-14      | LP1_DUE_DATE | 112601   | Loans Receivable          | previous_owner_external_id | 1000.0           | 0.0           | -1000.0        | 0.0           | originator_external_id  |
      | 2023-06-14      | LP1_DUE_DATE | 145023   | Suspense/Clearing account | self                       | -1000.0          | 0.0           | 0.0            | -1000.0       | originator_external_id  |
      | 2023-06-14      | LP1_DUE_DATE | 146000   | Asset transfer            | self                       | 0.0              | 1000.0        | -1000.0        | 0.0           | originator_external_id  |

  @TestRailId:C85205
  Scenario: Verify Transaction Summary Report with Asset Owner with previous owner for intermediarySale transfer with following BUYBACK requests and no originatorId - UC3
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DELAYED_SETTLEMENT" for loan product "LP1_DUE_DATE"
    When Admin sets the business date to "1 May 2023"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    When Admin creates a fully customized loan with the following data:
      | LoanProduct  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_DUE_DATE | 01 May 2023       | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | intermediarySale | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 9999-12-31  | INTERMEDIARYSALE |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 9999-12-31  | INTERMEDIARYSALE |
    Then LoanOwnershipTransferBusinessEvent with transfer type: "INTERMEDIARYSALE" and transfer asset owner is created
    When Admin sets the business date to "14 June 2023"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-06-14     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 9999-12-31  | INTERMEDIARYSALE |
      | 2023-06-14     | 1                  | BUYBACK_INTERMEDIATE | 2023-06-14    | 9999-12-31  | BUYBACK          |
    When Admin sets the business date to "15 June 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 2023-06-14  | INTERMEDIARYSALE |
      | 2023-06-14     | 1                  | BUYBACK_INTERMEDIATE | 2023-06-14    | 2023-06-14  | BUYBACK          |
    Then LoanOwnershipTransferBusinessEvent with transfer type: "BUYBACK" and transfer asset owner based on intermediarySale is created
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DEFAULT_SETTLEMENT" for loan product "LP1_DUE_DATE"
# --- run Journal Entry Aggregation job ---- #
    And Admin runs the "JOURNAL_ENTRY_AGGREGATION" job
# --- run Transaction Summary Report with Asset Owner report --- #
    Then Transaction Summary Report with Asset Owner for date "01 May 2023" has originatorId, asset owner externalId and the following data:
      | TransactionDate | Product      | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | asset_owner_id | from_asset_owner_id | originator_external_ids |
      | 2023-05-01      | LP1_DUE_DATE | Disbursement              | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2023-05-01      | LP1_DUE_DATE | Disbursement              | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2023-05-01      | LP1_DUE_DATE | Disbursement              | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2023-05-01      | LP1_DUE_DATE | Disbursement              | AUTOPAY          |            | 0        | Principal                |                      | 1000.0             |                |                     |                         |
      | 2023-05-01      | LP1_DUE_DATE | Disbursement              | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
    Then Transaction Summary Report with Asset Owner for date "14 June 2023" has originatorId, asset owner externalId and the following data:
      | TransactionDate | Product      | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | asset_owner_id | from_asset_owner_id | originator_external_ids |
      | 2023-06-14      | LP1_DUE_DATE | Asset Buyback             |                  |            | 0        | Principal                |                      | -1000.0            |                | owner_external_id   |                         |

  @TestRailId:C85206
  Scenario: Verify Trial Balance Summary Report with Asset Owner with owner-to-owner transfer completes via COB and next repayment accounting goes to new owner and no originatorId - UC4
    When Admin sets the business date to "1 May 2023"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin sets the business date to "25 May 2023"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-25     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "26 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then The latest asset externalization transaction with "ACTIVE" status has the following TRANSFER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | CREDIT    | 1000.00 |
      | ASSET         | 146000        | Asset transfer   | DEBIT     | 1000.00 |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
      | ASSET         | 146000        | Asset transfer   | CREDIT    | 1000.00 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "26 May 2023" with 200 EUR transaction amount and system-generated Idempotency key and check external owner
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName             | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable          | DEBIT     | 1000.00 |
      | ASSET         | 112601        | Loans Receivable          | CREDIT    | 200.00  |
      | LIABILITY     | 145023        | Suspense/Clearing account | DEBIT     | 200.00  |
# --- run Journal Entry Aggregation job ---- #
    And Admin runs the "JOURNAL_ENTRY_AGGREGATION" job
# --- run Transaction Summary Report with Asset Owner report --- #
    Then Trial Balance Summary Report with Asset Owner for date "01 May 2023" has originatorId, asset owner externalId and the following data:
      | PostingDate     | Product      | glAcct   | Description               | AssetOwner                 | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2023-05-01      | LP1          | 112601   | Loans Receivable          | self                       | 0.0              | 1000.0        | 0.0            | 1000.0        |                         |
      | 2023-05-01      | LP1          | 145023   | Suspense/Clearing account | self                       | 0.0              | 0.0           | -1000.0        | -1000.0       |                         |
    Then Trial Balance Summary Report with Asset Owner for date "22 May 2023" has originatorId, asset owner externalId and the following data:
      | PostingDate     | Product      | glAcct   | Description               | AssetOwner                 | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2023-05-22      | LP1          | 112601   | Loans Receivable          | previous_owner_external_id | 1000.0           | 0.0           | 0.0            | 1000.0        |                         |
      | 2023-05-22      | LP1          | 145023   | Suspense/Clearing account | self                       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
    Then Trial Balance Summary Report with Asset Owner for date "25 May 2023" has originatorId, asset owner externalId and the following data:
      | PostingDate     | Product      | glAcct   | Description               | AssetOwner                 | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2023-05-25      | LP1          | 112601   | Loans Receivable          | owner_external_id          | 0.0              | 1000.0        | 0.0            | 1000.0        |                         |
      | 2023-05-25      | LP1          | 112601   | Loans Receivable          | previous_owner_external_id | 1000.0           | 0.0           | -1000.0        | 0.0           |                         |
      | 2023-05-25      | LP1          | 145023   | Suspense/Clearing account | self                       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2023-05-25      | LP1          | 146000   | Asset transfer            | self                       | 0.0              | 1000.0        | -1000.0        | 0.0           |                         |
    Then Trial Balance Summary Report with Asset Owner for date "26 May 2023" has originatorId, asset owner externalId and the following data:
      | PostingDate     | Product      | glAcct   | Description               | AssetOwner                 | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2023-05-26      | LP1          | 112601   | Loans Receivable          | owner_external_id          | 1000.0           | 0.0           | -200.0         | 800.0         |                         |
      | 2023-05-26      | LP1          | 145023   | Suspense/Clearing account | self                       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2023-05-26      | LP1          | 145023   | Suspense/Clearing account | owner_external_id          | 0.0              | 200.0         | 0.0            | 200.0         |                         |

  @TestRailId:C85370
  Scenario: Verify Transaction Summary Report with Buy Down Fee amortization allocation mappings after loan re-open via repayment reversal - UC1
    When Admin sets the business date to "1 January 2026"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 January 2026   | 250            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2026" with "250" amount and expected disbursement date on "1 January 2026"
    And Admin successfully disburse the loan on "1 January 2026" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "1 January 2026" with "50" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2026  | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 January 2026  | Buy Down Fee Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Loan Amortization Allocation Mapping for "BUY_DOWN_FEE" transaction created on "01 January 2026" contains the following data:
      | Date            | Type | Amount |
      | 01 January 2026 | AM   | 0.56   |
    When Admin sets the business date to "03 January 2026"
    When Loan Pay-off is made on "03 January 2026"
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2026  | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 January 2026  | Buy Down Fee Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Repayment                 | 100.04 | 100.0     | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Accrual                   | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 49.44  | 0.0       | 49.44    | 0.0  | 0.0       | 0.0          | false    | false    |
    And Loan Amortization Allocation Mapping for "BUY_DOWN_FEE" transaction created on "01 January 2026" contains the following data:
      | Date            | Type | Amount |
      | 01 January 2026 | AM   | 0.56   |
      | 03 January 2026 | AM   | 49.44  |
    When Customer undo "1"th "Repayment" transaction made on "03 January 2026"
    Then Loan status will be "ACTIVE"
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2026  | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 January 2026  | Buy Down Fee Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Repayment                 | 100.04 | 100.0     | 0.04     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 03 January 2026  | Accrual                   | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 49.44  | 0.0       | 49.44    | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2026  | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 January 2026  | Buy Down Fee Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Repayment                 | 100.04 | 100.0     | 0.04     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 03 January 2026  | Accrual                   | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 49.44  | 0.0       | 49.44    | 0.0  | 0.0       | 0.0          | false    | false    |
    And Loan Amortization Allocation Mapping for "BUY_DOWN_FEE" transaction created on "01 January 2026" contains the following data:
      | Date            | Type | Amount |
      | 01 January 2026 | AM   | 0.56   |
      | 03 January 2026 | AM   | 49.44  |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2026  | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 January 2026  | Buy Down Fee Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Repayment                 | 100.04 | 100.0     | 0.04     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 03 January 2026  | Accrual                   | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 49.44  | 0.0       | 49.44    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2026  | Accrual                   | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Loan Amortization Allocation Mapping for "BUY_DOWN_FEE" transaction created on "01 January 2026" contains the following data:
      | Date            | Type | Amount |
      | 01 January 2026 | AM   | 0.56   |
      | 03 January 2026 | AM   | 49.44  |
    Then Transaction Summary Report for date "01 January 2026" has the following data:
      | TransactionDate | Product                                                  | TransactionType_Name            | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee                    | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee                    | AUTOPAY          |            | 0        | Interest                 |                      | 50.0               |
      | 2026-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee                    | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee                    | AUTOPAY          |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee                    | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization       |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization       |                  |            | 0        | Interest                 |                      | 0.56               |
      | 2026-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization       |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization       |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization       |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement                    | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement                    | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement                    | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement                    | AUTOPAY          |            | 0        | Principal                |                      | 100.0              |
      | 2026-01-01      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Disbursement                    | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    Then Transaction Summary Report for date "03 January 2026" has the following data:
      | TransactionDate | Product                                                  | TransactionType_Name            | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Apply Charges                   |                  |            | 0        | Interest                 |                      | 0.04               |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization       |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization       |                  |            | 0        | Interest                 |                      | 49.44              |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization       |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization       |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Buy Down Fee Amortization       |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Repayment                       | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Repayment                       | AUTOPAY          |            | 0        | Interest                 |                      | -0.04              |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Repayment                       | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Repayment                       | AUTOPAY          |            | 0        | Principal                |                      | -100.0             |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Repayment                       | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Repayment                       | AUTOPAY          |            | 1        | Fees                     |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Repayment                       | AUTOPAY          |            | 1        | Interest                 |                      | 0.04               |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Repayment                       | AUTOPAY          |            | 1        | Penalty                  |                      | 0.0                |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Repayment                       | AUTOPAY          |            | 1        | Principal                |                      | 100.0              |
      | 2026-01-03      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | Repayment                       | AUTOPAY          |            | 1        | Unallocated Credit (UNC) |                      | 0.0                |
    When Loan Pay-off is made on "05 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C85354
  Scenario: Verify Transaction Summary Report with Capitalized Income amortization allocation mappings after loan re-open via payout refund reversal - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | 01 January 2026   | 250            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "250" amount and expected disbursement date on "1 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "01 January 2026" with "50" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2026  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 01 January 2026  | Capitalized Income Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Loan Amortization Allocation Mapping for "CAPITALIZED_INCOME" transaction created on "01 January 2026" contains the following data:
      | Date            | Type | Amount |
      | 01 January 2026 | AM   | 0.56   |
    When Admin sets the business date to "03 January 2026"
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "03 January 2026" with 150.06 EUR transaction amount and self-generated Idempotency key
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2026  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 01 January 2026  | Capitalized Income Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Payout Refund                   | 150.06 | 150.0     | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Accrual                         | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Capitalized Income Amortization | 49.44  | 0.0       | 49.44    | 0.0  | 0.0       | 0.0          | false    | false    |
    When Customer undo "1"th "Payout Refund" transaction made on "03 January 2026"
    Then Loan status will be "ACTIVE"
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2026  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 01 January 2026  | Capitalized Income Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Payout Refund                   | 150.06 | 150.0     | 0.06     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 03 January 2026  | Accrual                         | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Capitalized Income Amortization | 49.44  | 0.0       | 49.44    | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2026  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 01 January 2026  | Capitalized Income Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Payout Refund                   | 150.06 | 150.0     | 0.06     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 03 January 2026  | Accrual                         | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Capitalized Income Amortization | 49.44  | 0.0       | 49.44    | 0.0  | 0.0       | 0.0          | false    | false    |
    And Loan Amortization Allocation Mapping for "CAPITALIZED_INCOME" transaction created on "01 January 2026" contains the following data:
      | Date            | Type | Amount |
      | 01 January 2026 | AM   | 0.56   |
      | 03 January 2026 | AM   | 49.44  |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2026  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 01 January 2026  | Capitalized Income Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Payout Refund                   | 150.06 | 150.0     | 0.06     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 03 January 2026  | Accrual                         | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Capitalized Income Amortization | 49.44  | 0.0       | 49.44    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2026  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Loan Amortization Allocation Mapping for "CAPITALIZED_INCOME" transaction created on "01 January 2026" contains the following data:
      | Date            | Type | Amount |
      | 01 January 2026 | AM   | 0.56   |
      | 03 January 2026 | AM   | 49.44  |
    Then Transaction Summary Report for date "01 January 2026" has the following data:
      | TransactionDate | Product                                                                                           | TransactionType_Name            | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-01      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Capitalized Income              | AUTOPAY          |            | 0        | Fees                     |                      |                    |
      | 2026-01-01      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Capitalized Income              | AUTOPAY          |            | 0        | Interest                 |                      |                    |
      | 2026-01-01      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Capitalized Income              | AUTOPAY          |            | 0        | Penalty                  |                      |                    |
      | 2026-01-01      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Capitalized Income              | AUTOPAY          |            | 0        | Principal                |                      |                    |
      | 2026-01-01      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Capitalized Income              | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      |                    |
      | 2026-01-01      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Capitalized Income Amortization |                  |            | 0        | Fees                     |                      |                    |
      | 2026-01-01      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Capitalized Income Amortization |                  |            | 0        | Interest                 |                      |                    |
      | 2026-01-01      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Capitalized Income Amortization |                  |            | 0        | Penalty                  |                      |                    |
      | 2026-01-01      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Capitalized Income Amortization |                  |            | 0        | Principal                |                      |                    |
      | 2026-01-01      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Capitalized Income Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      |                    |
      | 2026-01-01      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Disbursement                    | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-01      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Disbursement                    | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-01      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Disbursement                    | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-01      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Disbursement                    | AUTOPAY          |            | 0        | Principal                |                      | 100.0              |
      | 2026-01-01      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Disbursement                    | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    Then Transaction Summary Report for date "03 January 2026" has the following data:
      | TransactionDate | Product                                                                                           | TransactionType_Name            | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-03      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Apply Charges                   |                  |            | 0        | Interest                 |                      | 0.06               |
      | 2026-01-03      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Capitalized Income Amortization |                  |            | 0        | Fees                     |                      |                    |
      | 2026-01-03      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Capitalized Income Amortization |                  |            | 0        | Interest                 |                      |                    |
      | 2026-01-03      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Capitalized Income Amortization |                  |            | 0        | Penalty                  |                      |                    |
      | 2026-01-03      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Capitalized Income Amortization |                  |            | 0        | Principal                |                      |                    |
      | 2026-01-03      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Capitalized Income Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      |                    |
      | 2026-01-03      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Payout Refund                   | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-03      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Payout Refund                   | AUTOPAY          |            | 0        | Interest                 |                      | -0.06              |
      | 2026-01-03      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Payout Refund                   | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-03      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Payout Refund                   | AUTOPAY          |            | 0        | Principal                |                      | -150.0             |
      | 2026-01-03      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Payout Refund                   | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-03      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Payout Refund                   | AUTOPAY          |            | 1        | Fees                     |                      | 0.0                |
      | 2026-01-03      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Payout Refund                   | AUTOPAY          |            | 1        | Interest                 |                      | 0.06               |
      | 2026-01-03      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Payout Refund                   | AUTOPAY          |            | 1        | Penalty                  |                      | 0.0                |
      | 2026-01-03      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Payout Refund                   | AUTOPAY          |            | 1        | Principal                |                      | 150.0              |
      | 2026-01-03      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | Payout Refund                   | AUTOPAY          |            | 1        | Unallocated Credit (UNC) |                      | 0.0                |
    When Loan Pay-off is made on "05 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
