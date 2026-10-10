@SerialChargeAccrualConfig
@WorkingCapital
@WorkingCapitalTransactionSummaryReportFeature
Feature: Working Capital Transaction Summary Reports

  Scenario: Verify Transaction Summary Reports for Working Capital loans show every transaction type with its allocation, payment type and sign
    Given Global config "charge-accrual-date" value set to "due-date"
    And A code value "REFUND_TO_CUSTOMER" exists for code name "working_capital_loan_credit_balance_refund_classification"
    When Admin sets the business date to "01 January 2026"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "AUTOPAY" payment type
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "01 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Principal                |                      | 9000.0             |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Principal                |                      | 1000.0             |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "01 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Principal                |                      | 9000.0             |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Principal                |                      | 1000.0             |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "05 January 2026" with 50 transaction amount on Working Capital loan with the following payment details:
      | paymentType |
      | AUTOPAY     |
    And Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "05 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Fees                     |                      | 9.61               |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |            | 0        | Principal                |                      | -50.0              |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "05 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Fees                     |                      | 9.61               |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |            | 0        | Principal                |                      | -50.0              |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    When Admin adds Discount fee adjustment with "500" amount on transaction date "08 January 2026" on Working Capital loan account for last discount
    And Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "08 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name                 | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-08      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment              |                  |            | 0        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-01-08      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment              |                  |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-08      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment              |                  |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-08      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment              |                  |            | 0        | Principal                |                      | -500.0             |                |                     |                         |
      | 2026-01-08      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment              |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
      | 2026-01-08      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization Adjustment |                  |            | 0        | Fees                     |                      | -4.47              |                |                     |                         |
      | 2026-01-08      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization Adjustment |                  |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-08      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization Adjustment |                  |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-08      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization Adjustment |                  |            | 0        | Principal                |                      | 0.0                |                |                     |                         |
      | 2026-01-08      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization Adjustment |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "08 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name                 | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-08      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment              |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-08      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment              |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-08      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment              |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-08      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment              |                  |            | 0        | Principal                |                      | -500.0             |
      | 2026-01-08      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment              |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-08      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization Adjustment |                  |            | 0        | Fees                     |                      | -4.47              |
      | 2026-01-08      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization Adjustment |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-08      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization Adjustment |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-08      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization Adjustment |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-08      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization Adjustment |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    When Admin sets the business date to "15 January 2026"
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 15 January 2026 | 15 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "15 January 2026" with "9000" amount and expected disbursement date on "15 January 2026"
    And Admin successfully disburse the Working Capital loan on "15 January 2026" with "9000" EUR transaction amount and "AUTOPAY" payment type
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "16 January 2026" due date and 45.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "16 January 2026" due date and 25.0 transaction amount
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "15 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-15      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-01-15      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-15      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-15      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Principal                |                      | 9000.0             |                |                     |                         |
      | 2026-01-15      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "15 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-15      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-15      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-15      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-15      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Principal                |                      | 9000.0             |
      | 2026-01-15      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    When Admin sets the business date to "17 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "16 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype                   | Reversed | Allocation_Type | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-16      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  |                              | 0        | Interest        |                      | 0.0                |                |                     |                         |
      | 2026-01-16      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  | Working Capital Loan Fee     | 0        | Fees            |                      | 45.0               |                |                     |                         |
      | 2026-01-16      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  | Working Capital Loan Penalty | 0        | Penalty         |                      | 25.0               |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "16 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype                   | Reversed | Allocation_Type | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-16      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  |                              | 0        | Interest        |                      | 0.0                |
      | 2026-01-16      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  | Working Capital Loan Fee     | 0        | Fees            |                      | 45.0               |
      | 2026-01-16      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  | Working Capital Loan Penalty | 0        | Penalty         |                      | 25.0               |
    When Customer makes repayment on "17 January 2026" with 40 transaction amount on Working Capital loan with the following payment details:
      | paymentType |
      | AUTOPAY     |
    And Admin makes a charge adjustment for the last added fee charge with 20.0 amount on working capital loan
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "17 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-17      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 0        | Fees                     |                      | -20.0              |                |                     |                         |
      | 2026-01-17      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-17      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-17      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 0        | Principal                |                      | 0.0                |                |                     |                         |
      | 2026-01-17      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
      | 2026-01-17      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Fees                     |                      | -15.0              |                |                     |                         |
      | 2026-01-17      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-17      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Penalty                  |                      | -25.0              |                |                     |                         |
      | 2026-01-17      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Principal                |                      | 0.0                |                |                     |                         |
      | 2026-01-17      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "17 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype               | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-17      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |                          | 0        | Interest                 |                      | 0.0                |
      | 2026-01-17      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |                          | 0        | Principal                |                      | 0.0                |
      | 2026-01-17      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |                          | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-17      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  | Working Capital Loan Fee | 0        | Fees                     |                      | -20.0              |
      | 2026-01-17      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |                          | 0        | Fees                     |                      | -15.0              |
      | 2026-01-17      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |                          | 0        | Interest                 |                      | 0.0                |
      | 2026-01-17      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |                          | 0        | Penalty                  |                      | -25.0              |
      | 2026-01-17      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |                          | 0        | Principal                |                      | 0.0                |
      | 2026-01-17      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |                          | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    When Admin sets the business date to "18 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "18 January 2026" with 110 transaction amount on Working Capital loan with the following payment details:
      | paymentType |
      | AUTOPAY     |
    And Customer makes "PAYOUT_REFUND" transaction on "18 January 2026" with 200 transaction amount on Working Capital loan with the following payment details:
      | paymentType |
      | AUTOPAY     |
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "18 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-18      | WCLP_ACC_DEF_REV_AM | Goodwill Credit      | AUTOPAY          |            | 0        | Fees                     |                      | -10.0              |                |                     |                         |
      | 2026-01-18      | WCLP_ACC_DEF_REV_AM | Goodwill Credit      | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-18      | WCLP_ACC_DEF_REV_AM | Goodwill Credit      | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-18      | WCLP_ACC_DEF_REV_AM | Goodwill Credit      | AUTOPAY          |            | 0        | Principal                |                      | -100.0             |                |                     |                         |
      | 2026-01-18      | WCLP_ACC_DEF_REV_AM | Goodwill Credit      | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
      | 2026-01-18      | WCLP_ACC_DEF_REV_AM | Payout Refund        | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-01-18      | WCLP_ACC_DEF_REV_AM | Payout Refund        | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-18      | WCLP_ACC_DEF_REV_AM | Payout Refund        | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-18      | WCLP_ACC_DEF_REV_AM | Payout Refund        | AUTOPAY          |            | 0        | Principal                |                      | -200.0             |                |                     |                         |
      | 2026-01-18      | WCLP_ACC_DEF_REV_AM | Payout Refund        | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "18 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-18      | WCLP_ACC_DEF_REV_AM | Goodwill Credit      | AUTOPAY          |            | 0        | Fees                     |                      | -10.0              |
      | 2026-01-18      | WCLP_ACC_DEF_REV_AM | Goodwill Credit      | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-18      | WCLP_ACC_DEF_REV_AM | Goodwill Credit      | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-18      | WCLP_ACC_DEF_REV_AM | Goodwill Credit      | AUTOPAY          |            | 0        | Principal                |                      | -100.0             |
      | 2026-01-18      | WCLP_ACC_DEF_REV_AM | Goodwill Credit      | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-18      | WCLP_ACC_DEF_REV_AM | Payout Refund        | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-18      | WCLP_ACC_DEF_REV_AM | Payout Refund        | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-18      | WCLP_ACC_DEF_REV_AM | Payout Refund        | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-18      | WCLP_ACC_DEF_REV_AM | Payout Refund        | AUTOPAY          |            | 0        | Principal                |                      | -200.0             |
      | 2026-01-18      | WCLP_ACC_DEF_REV_AM | Payout Refund        | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    When Admin sets the business date to "19 January 2026"
    And Customer makes repayment on "19 January 2026" with 8750 transaction amount on Working Capital loan with the following payment details:
      | paymentType |
      | AUTOPAY     |
    Then Working Capital loan status will be "OVERPAID"
    And Transaction Summary Report with Asset Owner for Working Capital Loans for date "19 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-19      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-01-19      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-19      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-19      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Principal                |                      | -8700.0            |                |                     |                         |
      | 2026-01-19      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | -50.0              |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "19 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-19      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-19      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-19      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-19      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Principal                |                      | -8700.0            |
      | 2026-01-19      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | -50.0              |
    When Admin sets the business date to "20 January 2026"
    And Customer makes credit balance refund on "20 January 2026" with 30 transaction amount on Working Capital loan with valid classification "REFUND_TO_CUSTOMER"
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "20 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name  | PaymentType_Name   | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-20      | WCLP_ACC_DEF_REV_AM | Credit Balance Refund | REFUND_TO_CUSTOMER |            | 0        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-01-20      | WCLP_ACC_DEF_REV_AM | Credit Balance Refund | REFUND_TO_CUSTOMER |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-20      | WCLP_ACC_DEF_REV_AM | Credit Balance Refund | REFUND_TO_CUSTOMER |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-20      | WCLP_ACC_DEF_REV_AM | Credit Balance Refund | REFUND_TO_CUSTOMER |            | 0        | Principal                |                      | 0.0                |                |                     |                         |
      | 2026-01-20      | WCLP_ACC_DEF_REV_AM | Credit Balance Refund | REFUND_TO_CUSTOMER |            | 0        | Unallocated Credit (UNC) |                      | 30.0               |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "20 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name  | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-20      | WCLP_ACC_DEF_REV_AM | Credit Balance Refund |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-20      | WCLP_ACC_DEF_REV_AM | Credit Balance Refund |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-20      | WCLP_ACC_DEF_REV_AM | Credit Balance Refund |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-20      | WCLP_ACC_DEF_REV_AM | Credit Balance Refund |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-20      | WCLP_ACC_DEF_REV_AM | Credit Balance Refund |                  |            | 0        | Unallocated Credit (UNC) |                      | 30.0               |

  Scenario: Verify Transaction Summary Reports for Working Capital loans show reversals on the reversal date with the opposite sign and a charge waiver with the full waived amount
    Given Global config "charge-accrual-date" value set to "due-date"
    When Admin sets the business date to "01 January 2026"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "AUTOPAY" payment type
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "01 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Principal                |                      | 9000.0             |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Principal                |                      | 1000.0             |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "01 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Principal                |                      | 9000.0             |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Principal                |                      | 1000.0             |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "05 January 2026" with 50 transaction amount on Working Capital loan with the following payment details:
      | paymentType |
      | AUTOPAY     |
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "06 January 2026" due date and 45.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "06 January 2026" due date and 25.0 transaction amount
    And Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "05 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Fees                     |                      | 9.61               |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |            | 0        | Principal                |                      | -50.0              |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "05 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Fees                     |                      | 9.61               |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |            | 0        | Principal                |                      | -50.0              |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    And Transaction Summary Report with Asset Owner for Working Capital Loans for date "06 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype                   | Reversed | Allocation_Type | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  |                              | 0        | Interest        |                      | 0.0                |                |                     |                         |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  | Working Capital Loan Fee     | 0        | Fees            |                      | 45.0               |                |                     |                         |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  | Working Capital Loan Penalty | 0        | Penalty         |                      | 25.0               |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "06 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype                   | Reversed | Allocation_Type | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  |                              | 0        | Interest        |                      | 0.0                |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  | Working Capital Loan Fee     | 0        | Fees            |                      | 45.0               |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  | Working Capital Loan Penalty | 0        | Penalty         |                      | 25.0               |
    When Customer undo "1"th "REPAYMENT" transaction made on "05 January 2026" on Working Capital loan
    And Admin adds Discount fee adjustment with "500" amount on transaction date "07 January 2026" on Working Capital loan account for last discount and "working_capital_loan_discount_fee_classification_value" classification
    And Admin successfully undo Working Capital disbursal
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "07 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name      | PaymentType_Name                                       | chargetype                   | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Apply Charges             |                                                        |                              | 1        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Apply Charges             |                                                        | Working Capital Loan Fee     | 1        | Fees                     |                      | -45.0              |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Apply Charges             |                                                        | Working Capital Loan Penalty | 1        | Penalty                  |                      | -25.0              |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Disbursement              | AUTOPAY                                                |                              | 1        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Disbursement              | AUTOPAY                                                |                              | 1        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Disbursement              | AUTOPAY                                                |                              | 1        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Disbursement              | AUTOPAY                                                |                              | 1        | Principal                |                      | -9000.0            |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Disbursement              | AUTOPAY                                                |                              | 1        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee              |                                                        |                              | 1        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee              |                                                        |                              | 1        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee              |                                                        |                              | 1        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee              |                                                        |                              | 1        | Principal                |                      | -1000.0            |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee              |                                                        |                              | 1        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment   | working_capital_loan_discount_fee_classification_value |                              | 0        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment   | working_capital_loan_discount_fee_classification_value |                              | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment   | working_capital_loan_discount_fee_classification_value |                              | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment   | working_capital_loan_discount_fee_classification_value |                              | 0        | Principal                |                      | -500.0             |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment   | working_capital_loan_discount_fee_classification_value |                              | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment   | working_capital_loan_discount_fee_classification_value |                              | 1        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment   | working_capital_loan_discount_fee_classification_value |                              | 1        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment   | working_capital_loan_discount_fee_classification_value |                              | 1        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment   | working_capital_loan_discount_fee_classification_value |                              | 1        | Principal                |                      | 500.0              |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment   | working_capital_loan_discount_fee_classification_value |                              | 1        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                                                        |                              | 1        | Fees                     |                      | -9.61              |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                                                        |                              | 1        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                                                        |                              | 1        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                                                        |                              | 1        | Principal                |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                                                        |                              | 1        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY                                                |                              | 1        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY                                                |                              | 1        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY                                                |                              | 1        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY                                                |                              | 1        | Principal                |                      | 50.0               |                |                     |                         |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY                                                |                              | 1        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "07 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name      | PaymentType_Name | chargetype                   | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Apply Charges             |                  |                              | 1        | Interest                 |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Apply Charges             |                  | Working Capital Loan Fee     | 1        | Fees                     |                      | -45.0              |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Apply Charges             |                  | Working Capital Loan Penalty | 1        | Penalty                  |                      | -25.0              |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Disbursement              | AUTOPAY          |                              | 1        | Fees                     |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Disbursement              | AUTOPAY          |                              | 1        | Interest                 |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Disbursement              | AUTOPAY          |                              | 1        | Penalty                  |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Disbursement              | AUTOPAY          |                              | 1        | Principal                |                      | -9000.0            |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Disbursement              | AUTOPAY          |                              | 1        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee              |                  |                              | 1        | Fees                     |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee              |                  |                              | 1        | Interest                 |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee              |                  |                              | 1        | Penalty                  |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee              |                  |                              | 1        | Principal                |                      | -1000.0            |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee              |                  |                              | 1        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment   |                  |                              | 0        | Fees                     |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment   |                  |                              | 0        | Interest                 |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment   |                  |                              | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment   |                  |                              | 0        | Principal                |                      | -500.0             |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment   |                  |                              | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment   |                  |                              | 1        | Fees                     |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment   |                  |                              | 1        | Interest                 |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment   |                  |                              | 1        | Penalty                  |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment   |                  |                              | 1        | Principal                |                      | 500.0              |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Adjustment   |                  |                              | 1        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |                              | 1        | Fees                     |                      | -9.61              |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |                              | 1        | Interest                 |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |                              | 1        | Penalty                  |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |                              | 1        | Principal                |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |                              | 1        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |                              | 1        | Fees                     |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |                              | 1        | Interest                 |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |                              | 1        | Penalty                  |                      | 0.0                |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |                              | 1        | Principal                |                      | 50.0               |
      | 2026-01-07      | WCLP_ACC_DEF_REV_AM | Repayment                 | AUTOPAY          |                              | 1        | Unallocated Credit (UNC) |                      | 0.0                |
    And Transaction Summary Report with Asset Owner for Working Capital Loans for date "06 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype                   | Reversed | Allocation_Type | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  |                              | 0        | Interest        |                      | 0.0                |                |                     |                         |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  | Working Capital Loan Fee     | 0        | Fees            |                      | 45.0               |                |                     |                         |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  | Working Capital Loan Penalty | 0        | Penalty         |                      | 25.0               |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "06 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype                   | Reversed | Allocation_Type | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  |                              | 0        | Interest        |                      | 0.0                |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  | Working Capital Loan Fee     | 0        | Fees            |                      | 45.0               |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  | Working Capital Loan Penalty | 0        | Penalty         |                      | 25.0               |
    When Admin sets the business date to "01 February 2026"
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 February 2026 | 01 February 2026         | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 February 2026" with "9000" amount and expected disbursement date on "01 February 2026"
    And Admin successfully disburse the Working Capital loan on "01 February 2026" with "9000" EUR transaction amount and "AUTOPAY" payment type
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "01 February 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-02-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-02-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-02-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-02-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Principal                |                      | 9000.0             |                |                     |                         |
      | 2026-02-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "01 February 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-02-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-02-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-02-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-02-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Principal                |                      | 9000.0             |
      | 2026-02-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    When Admin sets the business date to "02 February 2026"
    And Customer makes repayment on "02 February 2026" with 100 transaction amount on Working Capital loan with the following payment details:
      | paymentType |
      | AUTOPAY     |
    And Customer undo "1"th "REPAYMENT" transaction made on "02 February 2026" on Working Capital loan
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "02 February 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-02-02      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-02-02      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-02-02      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-02-02      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Principal                |                      | -100.0             |                |                     |                         |
      | 2026-02-02      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
      | 2026-02-02      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 1        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-02-02      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 1        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-02-02      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 1        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-02-02      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 1        | Principal                |                      | 100.0              |                |                     |                         |
      | 2026-02-02      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 1        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "02 February 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-02-02      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-02-02      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-02-02      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-02-02      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Principal                |                      | -100.0             |
      | 2026-02-02      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-02-02      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 1        | Fees                     |                      | 0.0                |
      | 2026-02-02      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 1        | Interest                 |                      | 0.0                |
      | 2026-02-02      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 1        | Penalty                  |                      | 0.0                |
      | 2026-02-02      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 1        | Principal                |                      | 100.0              |
      | 2026-02-02      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 1        | Unallocated Credit (UNC) |                      | 0.0                |
    When Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "03 February 2026" due date and 30.0 transaction amount
    And Admin sets the business date to "03 February 2026"
    And Admin makes a charge adjustment for the last added fee charge with 30.0 amount on working capital loan
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "03 February 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-02-03      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 0        | Fees                     |                      | -30.0              |                |                     |                         |
      | 2026-02-03      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-02-03      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-02-03      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 0        | Principal                |                      | 0.0                |                |                     |                         |
      | 2026-02-03      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "03 February 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype               | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-02-03      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |                          | 0        | Interest                 |                      | 0.0                |
      | 2026-02-03      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |                          | 0        | Principal                |                      | 0.0                |
      | 2026-02-03      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |                          | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-02-03      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  | Working Capital Loan Fee | 0        | Fees                     |                      | -30.0              |
    When Admin sets the business date to "04 February 2026"
    And Admin reverts the last charge adjustment on working capital loan
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "04 February 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-02-04      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 1        | Fees                     |                      | 30.0               |                |                     |                         |
      | 2026-02-04      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 1        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-02-04      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 1        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-02-04      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 1        | Principal                |                      | 0.0                |                |                     |                         |
      | 2026-02-04      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 1        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
    When Admin sets the business date to "05 February 2026"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 February 2026" due date and 40.0 transaction amount
    And Admin waives the last added charge on working capital loan
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "05 February 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-02-05      | WCLP_ACC_DEF_REV_AM | Waive Charges        |                  |            | 0        | Fees                     |                      | -40.0              |                |                     |                         |
      | 2026-02-05      | WCLP_ACC_DEF_REV_AM | Waive Charges        |                  |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-02-05      | WCLP_ACC_DEF_REV_AM | Waive Charges        |                  |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-02-05      | WCLP_ACC_DEF_REV_AM | Waive Charges        |                  |            | 0        | Principal                |                      | 0.0                |                |                     |                         |
      | 2026-02-05      | WCLP_ACC_DEF_REV_AM | Waive Charges        |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "05 February 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-02-05      | WCLP_ACC_DEF_REV_AM | Waive Charges        |                  |            | 0        | Fees                     |                      | -40.0              |
      | 2026-02-05      | WCLP_ACC_DEF_REV_AM | Waive Charges        |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2026-02-05      | WCLP_ACC_DEF_REV_AM | Waive Charges        |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-02-05      | WCLP_ACC_DEF_REV_AM | Waive Charges        |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-02-05      | WCLP_ACC_DEF_REV_AM | Waive Charges        |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |

  Scenario: Verify Transaction Summary Reports for Working Capital loans show the charge-off reason from the charge-off date on
    Given Global config "charge-accrual-date" value set to "due-date"
    And A code value "Fraud" exists for code name "ChargeOffReasons"
    When Admin sets the business date to "01 January 2026"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "AUTOPAY" payment type
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "01 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Principal                |                      | 9000.0             |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Principal                |                      | 1000.0             |                |                     |                         |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "01 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Principal                |                      | 9000.0             |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Principal                |                      | 1000.0             |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Discount Fee         |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 50 transaction amount on Working Capital loan with the following payment details:
      | paymentType |
      | AUTOPAY     |
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "05 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Principal                |                      | -50.0              |                |                     |                         |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "05 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Principal                |                      | -50.0              |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    When Admin sets the business date to "10 January 2026"
    And Admin charges off the Working Capital loan on "10 January 2026" with charge-off reason "Fraud"
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "10 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-10      | WCLP_ACC_DEF_REV_AM | Charge-off                |                  |            | 0        | Fees                     | Fraud                | 0.0                |                |                     |                         |
      | 2026-01-10      | WCLP_ACC_DEF_REV_AM | Charge-off                |                  |            | 0        | Interest                 | Fraud                | 0.0                |                |                     |                         |
      | 2026-01-10      | WCLP_ACC_DEF_REV_AM | Charge-off                |                  |            | 0        | Penalty                  | Fraud                | 0.0                |                |                     |                         |
      | 2026-01-10      | WCLP_ACC_DEF_REV_AM | Charge-off                |                  |            | 0        | Principal                | Fraud                | -9950.0            |                |                     |                         |
      | 2026-01-10      | WCLP_ACC_DEF_REV_AM | Charge-off                |                  |            | 0        | Unallocated Credit (UNC) | Fraud                | 0.0                |                |                     |                         |
      | 2026-01-10      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Fees                     | Fraud                | 1000.0             |                |                     |                         |
      | 2026-01-10      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Interest                 | Fraud                | 0.0                |                |                     |                         |
      | 2026-01-10      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Penalty                  | Fraud                | 0.0                |                |                     |                         |
      | 2026-01-10      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Principal                | Fraud                | 0.0                |                |                     |                         |
      | 2026-01-10      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) | Fraud                | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "10 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name      | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-10      | WCLP_ACC_DEF_REV_AM | Charge-off                |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-10      | WCLP_ACC_DEF_REV_AM | Charge-off                |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-10      | WCLP_ACC_DEF_REV_AM | Charge-off                |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-10      | WCLP_ACC_DEF_REV_AM | Charge-off                |                  |            | 0        | Principal                |                      | -9950.0            |
      | 2026-01-10      | WCLP_ACC_DEF_REV_AM | Charge-off                |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-01-10      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Fees                     |                      | 1000.0             |
      | 2026-01-10      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-10      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-10      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-10      | WCLP_ACC_DEF_REV_AM | Discount Fee Amortization |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    When Admin sets the business date to "11 January 2026"
    And Customer makes repayment on "11 January 2026" with 100 transaction amount on Working Capital loan with the following payment details:
      | paymentType |
      | AUTOPAY     |
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "11 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-11      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Fees                     | Fraud                | 0.0                |                |                     |                         |
      | 2026-01-11      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Interest                 | Fraud                | 0.0                |                |                     |                         |
      | 2026-01-11      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Penalty                  | Fraud                | 0.0                |                |                     |                         |
      | 2026-01-11      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Principal                | Fraud                | -100.0             |                |                     |                         |
      | 2026-01-11      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Unallocated Credit (UNC) | Fraud                | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "11 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-11      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-11      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-11      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-11      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Principal                |                      | -100.0             |
      | 2026-01-11      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    When Admin sets the business date to "12 January 2026"
    And Customer undo "1"th "REPAYMENT" transaction made on "11 January 2026" on Working Capital loan
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "12 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-12      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 1        | Fees                     | Fraud                | 0.0                |                |                     |                         |
      | 2026-01-12      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 1        | Interest                 | Fraud                | 0.0                |                |                     |                         |
      | 2026-01-12      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 1        | Penalty                  | Fraud                | 0.0                |                |                     |                         |
      | 2026-01-12      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 1        | Principal                | Fraud                | 100.0              |                |                     |                         |
      | 2026-01-12      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 1        | Unallocated Credit (UNC) | Fraud                | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "12 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-12      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 1        | Fees                     |                      | 0.0                |
      | 2026-01-12      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 1        | Interest                 |                      | 0.0                |
      | 2026-01-12      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 1        | Penalty                  |                      | 0.0                |
      | 2026-01-12      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 1        | Principal                |                      | 100.0              |
      | 2026-01-12      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 1        | Unallocated Credit (UNC) |                      | 0.0                |
    When Admin sets the business date to "01 February 2026"
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 February 2026 | 01 February 2026         | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 February 2026" with "9000" amount and expected disbursement date on "01 February 2026"
    And Admin successfully disburse the Working Capital loan on "01 February 2026" with "9000" EUR transaction amount and "AUTOPAY" payment type
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 February 2026" due date and 45.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "05 February 2026" due date and 25.0 transaction amount
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "01 February 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-02-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-02-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-02-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-02-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Principal                |                      | 9000.0             |                |                     |                         |
      | 2026-02-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "01 February 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-02-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-02-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-02-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-02-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Principal                |                      | 9000.0             |
      | 2026-02-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    When Admin sets the business date to "03 February 2026"
    And Admin charges off the Working Capital loan on "03 February 2026" with charge-off reason "Fraud"
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "03 February 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-02-03      | WCLP_ACC_DEF_REV_AM | Charge-off           |                  |            | 0        | Fees                     | Fraud                | -45.0              |                |                     |                         |
      | 2026-02-03      | WCLP_ACC_DEF_REV_AM | Charge-off           |                  |            | 0        | Interest                 | Fraud                | 0.0                |                |                     |                         |
      | 2026-02-03      | WCLP_ACC_DEF_REV_AM | Charge-off           |                  |            | 0        | Penalty                  | Fraud                | -25.0              |                |                     |                         |
      | 2026-02-03      | WCLP_ACC_DEF_REV_AM | Charge-off           |                  |            | 0        | Principal                | Fraud                | -9000.0            |                |                     |                         |
      | 2026-02-03      | WCLP_ACC_DEF_REV_AM | Charge-off           |                  |            | 0        | Unallocated Credit (UNC) | Fraud                | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "03 February 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-02-03      | WCLP_ACC_DEF_REV_AM | Charge-off           |                  |            | 0        | Fees                     |                      | -45.0              |
      | 2026-02-03      | WCLP_ACC_DEF_REV_AM | Charge-off           |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2026-02-03      | WCLP_ACC_DEF_REV_AM | Charge-off           |                  |            | 0        | Penalty                  |                      | -25.0              |
      | 2026-02-03      | WCLP_ACC_DEF_REV_AM | Charge-off           |                  |            | 0        | Principal                |                      | -9000.0            |
      | 2026-02-03      | WCLP_ACC_DEF_REV_AM | Charge-off           |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    When Admin sets the business date to "06 February 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "05 February 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype                   | Reversed | Allocation_Type | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-02-05      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  |                              | 0        | Interest        | Fraud                | 0.0                |                |                     |                         |
      | 2026-02-05      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  | Working Capital Loan Fee     | 0        | Fees            | Fraud                | 45.0               |                |                     |                         |
      | 2026-02-05      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  | Working Capital Loan Penalty | 0        | Penalty         | Fraud                | 25.0               |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "05 February 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype                   | Reversed | Allocation_Type | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-02-05      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  |                              | 0        | Interest        |                      | 0.0                |
      | 2026-02-05      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  | Working Capital Loan Fee     | 0        | Fees            |                      | 45.0               |
      | 2026-02-05      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  | Working Capital Loan Penalty | 0        | Penalty         |                      | 25.0               |
    When Admin makes a charge adjustment for the last added penalty charge with 25.0 amount on working capital loan
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "06 February 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-02-06      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 0        | Fees                     | Fraud                | 0.0                |                |                     |                         |
      | 2026-02-06      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 0        | Interest                 | Fraud                | 0.0                |                |                     |                         |
      | 2026-02-06      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 0        | Penalty                  | Fraud                | -25.0              |                |                     |                         |
      | 2026-02-06      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 0        | Principal                | Fraud                | 0.0                |                |                     |                         |
      | 2026-02-06      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 0        | Unallocated Credit (UNC) | Fraud                | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "06 February 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype                   | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-02-06      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |                              | 0        | Interest                 |                      | 0.0                |
      | 2026-02-06      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |                              | 0        | Principal                |                      | 0.0                |
      | 2026-02-06      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |                              | 0        | Unallocated Credit (UNC) |                      | 0.0                |
      | 2026-02-06      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  | Working Capital Loan Penalty | 0        | Penalty                  |                      | -25.0              |
    When Admin sets the business date to "07 February 2026"
    And Admin reverts the last charge adjustment on working capital loan
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "07 February 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-02-07      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 1        | Fees                     | Fraud                | 0.0                |                |                     |                         |
      | 2026-02-07      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 1        | Interest                 | Fraud                | 0.0                |                |                     |                         |
      | 2026-02-07      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 1        | Penalty                  | Fraud                | 25.0               |                |                     |                         |
      | 2026-02-07      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 1        | Principal                | Fraud                | 0.0                |                |                     |                         |
      | 2026-02-07      | WCLP_ACC_DEF_REV_AM | Charge Adjustment    |                  |            | 1        | Unallocated Credit (UNC) | Fraud                | 0.0                |                |                     |                         |

  Scenario: Verify Transaction Summary Report with Asset Owner for Working Capital loans gives the same rows as for a regular loan and keeps both loan types and offices apart
    Given Global config "charge-accrual-date" value set to "due-date"
    When Admin sets the business date to "01 January 2026"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a new loan originator with external ID and name "WC Transaction Summary Report Originator"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | 01 January 2026   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin attaches the originator to the loan
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    And Admin adds "LOAN_SNOOZE_FEE" due date charge with "02 January 2026" due date and 10 EUR transaction amount
    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "03 January 2026" with 100 EUR transaction amount
    And Admin sets the business date to "04 January 2026"
    And Admin does write-off the loan on "04 January 2026"
    And Admin sets the business date to "05 January 2026"
    And Admin makes a recovery payment of 50 on the loan on "05 January 2026"
    When Admin sets the business date to "01 January 2026"
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 1000            | 10000        | 18                | 0        |
    And Admin attaches the originator to the working capital loan
    And Admin successfully approves the working capital loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "1000" EUR transaction amount and "AUTOPAY" payment type
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "02 January 2026" due date and 10.0 transaction amount
    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "03 January 2026" with 100 transaction amount on Working Capital loan with the following payment details:
      | paymentType |
      | AUTOPAY     |
    And Admin sets the business date to "04 January 2026"
    And Admin writes off the Working Capital loan on "04 January 2026"
    And Admin sets the business date to "05 January 2026"
    And Admin makes a recovery payment of "50" on the Working Capital loan on "05 January 2026"
    Then Transaction Summary Report with Asset Owner for date "01 January 2026" has originatorId, asset owner externalId and the following data:
      | TransactionDate | Product                                          | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-01      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Disbursement         | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-01      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Disbursement         | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-01      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Disbursement         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-01      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Disbursement         | AUTOPAY          |            | 0        | Principal                |                      | 1000.0             |                |                     | originator_external_id  |
      | 2026-01-01      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Disbursement         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     | originator_external_id  |
    And Transaction Summary Report with Asset Owner for Working Capital Loans for date "01 January 2026" has originatorId, asset owner externalId and the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Principal                |                      | 1000.0             |                |                     | originator_external_id  |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     | originator_external_id  |
    And Transaction Summary Report for Working Capital Loans for date "01 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Principal                |                      | 1000.0             |
      | 2026-01-01      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    And Transaction Summary Report with Asset Owner for date "02 January 2026" has originatorId, asset owner externalId and the following data:
      | TransactionDate | Product                                          | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-02      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Apply Charges        |                  |            | 0        | Interest        |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-02      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Apply Charges        |                  | Snooze fee | 0        | Fees            |                      | 10.0               |                |                     | originator_external_id  |
    And Transaction Summary Report with Asset Owner for Working Capital Loans for date "02 January 2026" has originatorId, asset owner externalId and the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype               | Reversed | Allocation_Type | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-02      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  |                          | 0        | Interest        |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-02      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  | Working Capital Loan Fee | 0        | Fees            |                      | 10.0               |                |                     | originator_external_id  |
    And Transaction Summary Report for Working Capital Loans for date "02 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype               | Reversed | Allocation_Type | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-02      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  |                          | 0        | Interest        |                      | 0.0                |
      | 2026-01-02      | WCLP_ACC_DEF_REV_AM | Apply Charges        |                  | Working Capital Loan Fee | 0        | Fees            |                      | 10.0               |
    And Transaction Summary Report with Asset Owner for date "03 January 2026" has originatorId, asset owner externalId and the following data:
      | TransactionDate | Product                                          | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-03      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Repayment            | AUTOPAY          |            | 0        | Fees                     |                      | -10.0              |                |                     | originator_external_id  |
      | 2026-01-03      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Repayment            | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-03      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Repayment            | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-03      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Repayment            | AUTOPAY          |            | 0        | Principal                |                      | -90.0              |                |                     | originator_external_id  |
      | 2026-01-03      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Repayment            | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     | originator_external_id  |
    And Transaction Summary Report with Asset Owner for Working Capital Loans for date "03 January 2026" has originatorId, asset owner externalId and the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-03      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Fees                     |                      | -10.0              |                |                     | originator_external_id  |
      | 2026-01-03      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-03      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-03      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Principal                |                      | -90.0              |                |                     | originator_external_id  |
      | 2026-01-03      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     | originator_external_id  |
    And Transaction Summary Report for Working Capital Loans for date "03 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-03      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Fees                     |                      | -10.0              |
      | 2026-01-03      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-03      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-03      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Principal                |                      | -90.0              |
      | 2026-01-03      | WCLP_ACC_DEF_REV_AM | Repayment            | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    And Transaction Summary Report with Asset Owner for date "04 January 2026" has originatorId, asset owner externalId and the following data:
      | TransactionDate | Product                                          | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-04      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Write-Off            |                  |            | 0        | Fees                     |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-04      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Write-Off            |                  |            | 0        | Interest                 |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-04      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Write-Off            |                  |            | 0        | Penalty                  |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-04      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Write-Off            |                  |            | 0        | Principal                |                      | -910.0             |                |                     | originator_external_id  |
      | 2026-01-04      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Write-Off            |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     | originator_external_id  |
    And Transaction Summary Report with Asset Owner for Working Capital Loans for date "04 January 2026" has originatorId, asset owner externalId and the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-04      | WCLP_ACC_DEF_REV_AM | Write-Off            |                  |            | 0        | Fees                     |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-04      | WCLP_ACC_DEF_REV_AM | Write-Off            |                  |            | 0        | Interest                 |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-04      | WCLP_ACC_DEF_REV_AM | Write-Off            |                  |            | 0        | Penalty                  |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-04      | WCLP_ACC_DEF_REV_AM | Write-Off            |                  |            | 0        | Principal                |                      | -910.0             |                |                     | originator_external_id  |
      | 2026-01-04      | WCLP_ACC_DEF_REV_AM | Write-Off            |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     | originator_external_id  |
    And Transaction Summary Report for Working Capital Loans for date "04 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-04      | WCLP_ACC_DEF_REV_AM | Write-Off            |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-04      | WCLP_ACC_DEF_REV_AM | Write-Off            |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-04      | WCLP_ACC_DEF_REV_AM | Write-Off            |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-04      | WCLP_ACC_DEF_REV_AM | Write-Off            |                  |            | 0        | Principal                |                      | -910.0             |
      | 2026-01-04      | WCLP_ACC_DEF_REV_AM | Write-Off            |                  |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
    And Transaction Summary Report with Asset Owner for date "05 January 2026" has originatorId, asset owner externalId and the following data:
      | TransactionDate | Product                                          | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-05      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Recovery Repayment   |                  |            | 0        | Fees                     |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-05      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Recovery Repayment   |                  |            | 0        | Interest                 |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-05      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Recovery Repayment   |                  |            | 0        | Penalty                  |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-05      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Recovery Repayment   |                  |            | 0        | Principal                |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-05      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | Recovery Repayment   |                  |            | 0        | Unallocated Credit (UNC) |                      | -50.0              |                |                     | originator_external_id  |
    And Transaction Summary Report with Asset Owner for Working Capital Loans for date "05 January 2026" has originatorId, asset owner externalId and the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Recovery Repayment   |                  |            | 0        | Fees                     |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Recovery Repayment   |                  |            | 0        | Interest                 |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Recovery Repayment   |                  |            | 0        | Penalty                  |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Recovery Repayment   |                  |            | 0        | Principal                |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Recovery Repayment   |                  |            | 0        | Unallocated Credit (UNC) |                      | -50.0              |                |                     | originator_external_id  |
    And Transaction Summary Report for Working Capital Loans for date "05 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Recovery Repayment   |                  |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Recovery Repayment   |                  |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Recovery Repayment   |                  |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Recovery Repayment   |                  |            | 0        | Principal                |                      | 0.0                |
      | 2026-01-05      | WCLP_ACC_DEF_REV_AM | Recovery Repayment   |                  |            | 0        | Unallocated Credit (UNC) |                      | -50.0              |
    When Admin sets the business date to "06 January 2026"
    And Admin undoes the last recovery payment on the Working Capital loan
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "06 January 2026" has originatorId, asset owner externalId and the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Recovery Repayment   |                  |            | 1        | Fees                     |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Recovery Repayment   |                  |            | 1        | Interest                 |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Recovery Repayment   |                  |            | 1        | Penalty                  |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Recovery Repayment   |                  |            | 1        | Principal                |                      | 0.0                |                |                     | originator_external_id  |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Recovery Repayment   |                  |            | 1        | Unallocated Credit (UNC) |                      | 50.0               |                |                     | originator_external_id  |
    And Transaction Summary Report for Working Capital Loans for date "06 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Recovery Repayment   |                  |            | 1        | Fees                     |                      | 0.0                |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Recovery Repayment   |                  |            | 1        | Interest                 |                      | 0.0                |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Recovery Repayment   |                  |            | 1        | Penalty                  |                      | 0.0                |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Recovery Repayment   |                  |            | 1        | Principal                |                      | 0.0                |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Recovery Repayment   |                  |            | 1        | Unallocated Credit (UNC) |                      | 50.0               |
    When Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 06 January 2026 | 06 January 2026          | 1000            | 10000        | 18                | 0        |
    And Admin successfully approves the working capital loan on "06 January 2026" with "1000" amount and expected disbursement date on "06 January 2026"
    And Admin successfully disburse the Working Capital loan on "06 January 2026" with "1000" EUR transaction amount and "AUTOPAY" payment type
    Then Transaction Summary Report with Asset Owner for Working Capital Loans for date "06 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount | Asset_owner_id | From_asset_owner_id | Originator_External_Ids |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |                |                     |                         |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |                |                     |                         |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |                |                     |                         |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Principal                |                      | 1000.0             |                |                     |                         |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |                |                     |                         |
    And Transaction Summary Report for Working Capital Loans for date "06 January 2026" has the following data:
      | TransactionDate | Product             | TransactionType_Name | PaymentType_Name | chargetype | Reversed | Allocation_Type          | Chargeoff_ReasonCode | Transaction_Amount |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Fees                     |                      | 0.0                |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Interest                 |                      | 0.0                |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Penalty                  |                      | 0.0                |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Principal                |                      | 1000.0             |
      | 2026-01-06      | WCLP_ACC_DEF_REV_AM | Disbursement         | AUTOPAY          |            | 0        | Unallocated Credit (UNC) |                      | 0.0                |
