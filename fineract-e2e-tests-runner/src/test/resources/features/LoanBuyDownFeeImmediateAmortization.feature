@BuyDownFeeFeature
@AssetExternalizationFeature
Feature: Immediate Buy Down Fee Amortization

  Scenario: Verify buy down fee posted after sale on IMMEDIATE product is fully recognized without COB claw-back
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 January 2026   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "100" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-01-02     | 1                  |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2026-01-02     | 1                  | PENDING | 2026-01-01    | 2026-01-02  | SALE             |
      | 2026-01-02     | 1                  | ACTIVE  | 2026-01-03    | 9999-12-31  | SALE             |
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 January 2026" with "5" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee              | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "BUY_DOWN_FEE_AMORTIZATION" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 450281       | Income From Buy Down        |       | 5.0    |
      | LIABILITY | 145024       | Deferred Capitalized Income | 5.0   |        |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 January 2026 | 5.0        | 5.0              | 0.0                      | 0.0             | 0.0                |
    And LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent is created on "03 January 2026"
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee              | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 January 2026 | 5.0        | 5.0              | 0.0                      | 0.0             | 0.0                |
    When Loan Pay-off is made on "10 January 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"

  Scenario: Verify IMMEDIATE product does not recognize buy down fee before sale
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 January 2026   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "100" EUR transaction amount
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "01 January 2026" with "5" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2026  | Buy Down Fee     | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 01 January 2026 | 5.0        | 0.0              | 5.0                      | 0.0             | 0.0                |
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"

  Scenario: Verify buy down fee adjustment after sale on IMMEDIATE product immediately adjusts recognized income
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 January 2026   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "100" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-01-02     | 1                  |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Loan
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 January 2026" with "10" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee              | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
    When Admin adds buy down fee adjustment with "AUTOPAY" payment type to the loan on "03 January 2026" with "3" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                     | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                         | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee                         | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization            | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Adjustment              | 3.0    | 0.0       | 3.0      | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization Adjustment | 3.0    | 0.0       | 3.0      | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 January 2026 | 10.0       | 7.0              | 0.0                      | 3.0             | 0.0                |
    When Loan Pay-off is made on "03 January 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"

  Scenario: Verify IMMEDIATE recognition after sale and loan closure do not double-recognize buy down fee
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 January 2026   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "100" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-01-02     | 1                  |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Loan
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 January 2026" with "5" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee              | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
    When Loan Pay-off is made on "03 January 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee              | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Repayment                 | 100.04 | 100.0     | 0.04     | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 January 2026 | 5.0        | 5.0              | 0.0                      | 0.0             | 0.0                |
    Then Loan Transactions tab has a "BUY_DOWN_FEE_AMORTIZATION" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 450281       | Income From Buy Down        |       | 5.0    |
      | LIABILITY | 145024       | Deferred Capitalized Income | 5.0   |        |
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"

  Scenario: Verify switching product from DEFERRED to IMMEDIATE after sale does not touch already deferred balances
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 January 2026   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "100" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-01-02     | 1                  |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Loan
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 January 2026" with "50" EUR transaction amount
    Then Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 January 2026 | 50.0       | 0.0              | 50.0                     | 0.0             | 0.0                |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 January 2026 | 50.0       | 0.57             | 49.43                    | 0.0             | 0.0                |
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "04 January 2026" with "5" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee              | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 January 2026 | 50.0       | 0.57             | 49.43                    | 0.0             | 0.0                |
      | 04 January 2026 | 5.0        | 5.0              | 0.0                      | 0.0             | 0.0                |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee              | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 January 2026 | 50.0       | 1.14             | 48.86                    | 0.0             | 0.0                |
      | 04 January 2026 | 5.0        | 5.0              | 0.0                      | 0.0             | 0.0                |
    When Loan Pay-off is made on "05 January 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"

  Scenario: Verify reversing buy down fee after sale on IMMEDIATE product reverses immediate amortization
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 January 2026   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "100" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-01-02     | 1                  |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Loan
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 January 2026" with "5" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee              | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
    When Customer undo "1"th "Buy Down Fee" transaction made on "03 January 2026"
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                     | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement                         | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 03 January 2026  | Buy Down Fee                         | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 03 January 2026  | Buy Down Fee Amortization            | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Buy Down Fee Amortization Adjustment | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "03 January 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
