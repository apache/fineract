@AssetExternalizationFeature
@ExternalAssetOwnerLoanAttributes
Feature: External Asset Owner Loan Attributes

  @TestRailId:C119657
  Scenario: Verify external asset owner loan-level attribute copy - UC1: first sale creates a copy of the product attributes configured when the sale is executed
    Given Loan product "LP2_EAO_LOAN_ATTR_UC1" exists dedicated to this feature with buy down fees
    When Admin creates external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_EAO_LOAN_ATTR_UC1" with values:
      | BUY_DOWN_FEE              |
      | BUY_DOWN_FEE_AMORTIZATION |
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_EAO_LOAN_ATTR_UC1 | 01 July 2026      | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    And External asset owner loan attributes of the loan have no loan-level copy
    # --- Asset externalization (sale) ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-01     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    And External asset owner loan attributes of the loan have no loan-level copy
    # --- Product attribute added while the sale is pending ---
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_EAO_LOAN_ATTR_UC1"
    # --- Sale executed by COB: loan-level copy is created ---
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    Then External asset owner loan attributes of the loan have a loan-level copy taken from the first sale with:
      | attributeKey                       | attributeValue                         |
      | EXCLUDED_TRANSACTION_TYPES         | BUY_DOWN_FEE,BUY_DOWN_FEE_AMORTIZATION |
      | BUY_DOWN_FEE_AMORTIZATION_STRATEGY | DEFERRED                               |
    And External asset owner loan attributes of the loan retrieved by loan external id are the same as retrieved by loan id
    # --- Closing the loan ---
    When Loan Pay-off is made on "02 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    # --- Cleanup ---
    When Admin deletes external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_EAO_LOAN_ATTR_UC1"
    And Admin deletes external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" for loan product "LP2_EAO_LOAN_ATTR_UC1"

  @TestRailId:C119658
  Scenario: Verify external asset owner loan-level attribute copy - UC2: deleting EXCLUDED_TRANSACTION_TYPES on the product after the sale keeps buy down fee transactions untagged
    Given Loan product "LP2_EAO_LOAN_ATTR_UC2" exists dedicated to this feature with buy down fees
    When Admin creates external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_EAO_LOAN_ATTR_UC2" with values:
      | BUY_DOWN_FEE                         |
      | BUY_DOWN_FEE_ADJUSTMENT              |
      | BUY_DOWN_FEE_AMORTIZATION            |
      | BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT |
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_EAO_LOAN_ATTR_UC2 | 01 July 2026      | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    # --- Asset externalization (sale) ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-01     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    # --- Buy down fee ---
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "02 July 2026" with "50" EUR transaction amount
    Then Loan transaction of type "BUY_DOWN_FEE" on "02 July 2026" has journal entries with external asset owner "none"
    And LoanBuyDownFeeTransactionCreatedBusinessEvent is created on "02 July 2026" without external owner
    # --- Product attribute deleted after the sale ---
    When Admin deletes external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_EAO_LOAN_ATTR_UC2"
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_EAO_LOAN_ATTR_UC2" does not exist
    # --- Buy down fee amortization ---
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has a "BUY_DOWN_FEE_AMORTIZATION" transaction with date "02 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit | External asset owner |
      | INCOME    | 450281       | Income From Buy Down        |       | 0.55   | none                 |
      | LIABILITY | 145024       | Deferred Capitalized Income | 0.55  |        | none                 |
    And Loan transaction of type "BUY_DOWN_FEE_AMORTIZATION" on "02 July 2026" has journal entries with external asset owner "none"
    And LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent is created on "02 July 2026" without external owner
    # --- Buy down fee adjustment ---
    When Admin adds buy down fee adjustment of buy down fee transaction made on "02 July 2026" with "AUTOPAY" payment type to the loan on "03 July 2026" with "10" EUR transaction amount
    Then Loan Transactions tab has a "BUY_DOWN_FEE_ADJUSTMENT" transaction with date "03 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit | External asset owner |
      | LIABILITY | 145024       | Deferred Capitalized Income | 10.0  |        | none                 |
      | EXPENSE   | 450280       | Buy Down Expense            |       | 10.0   | none                 |
    And Loan transaction of type "BUY_DOWN_FEE_ADJUSTMENT" on "03 July 2026" has journal entries with external asset owner "none"
    And LoanBuyDownFeeAdjustmentTransactionCreatedBusinessEvent is created on "03 July 2026" without external owner
    # --- Repayment ---
    When Customer makes "AUTOPAY" repayment on "03 July 2026" with 10 EUR transaction amount and check external owner
    Then Loan transaction of type "REPAYMENT" on "03 July 2026" has journal entries with external asset owner "active owner"
    And External asset owner loan attributes of the loan have a loan-level copy taken from the first sale with:
      | attributeKey               | attributeValue                                                                                     |
      | EXCLUDED_TRANSACTION_TYPES | BUY_DOWN_FEE,BUY_DOWN_FEE_ADJUSTMENT,BUY_DOWN_FEE_AMORTIZATION,BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT |
    # --- Closing the loan ---
    When Loan Pay-off is made on "03 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119659
  Scenario: Verify external asset owner loan-level attribute copy - UC3: changing BUY_DOWN_FEE_AMORTIZATION_STRATEGY on the product after the sale keeps the loan DEFERRED
    Given Loan product "LP2_EAO_LOAN_ATTR_UC3" exists dedicated to this feature with buy down fees
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_EAO_LOAN_ATTR_UC3"
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_EAO_LOAN_ATTR_UC3 | 01 July 2026      | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    # --- Asset externalization (sale) ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-01     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    # --- Product attribute switched to IMMEDIATE after the sale ---
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_EAO_LOAN_ATTR_UC3"
    # --- Buy down fee ---
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "02 July 2026" with "50" EUR transaction amount
    Then Buy down fee contains the following data:
      | Date         | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 02 July 2026 | 50.0       | 0.0              | 50.0                     | 0.0             | 0.0                |
    # --- Buy down fee amortization ---
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 02 July 2026     | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date         | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 02 July 2026 | 50.0       | 0.55             | 49.45                    | 0.0             | 0.0                |
    And External asset owner loan attributes of the loan have a loan-level copy taken from the first sale with:
      | attributeKey                       | attributeValue |
      | BUY_DOWN_FEE_AMORTIZATION_STRATEGY | DEFERRED       |
    # --- Closing the loan ---
    When Loan Pay-off is made on "03 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    # --- Cleanup ---
    When Admin deletes external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" for loan product "LP2_EAO_LOAN_ATTR_UC3"

  @TestRailId:C119660
  Scenario: Verify external asset owner loan-level attribute copy - UC4: changing CAPITALIZED_INCOME_AMORTIZATION_STRATEGY on the product after the sale keeps the loan DEFERRED
    Given Loan product "LP2_EAO_LOAN_ATTR_UC4" exists dedicated to this feature with capitalized income
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_EAO_LOAN_ATTR_UC4"
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_EAO_LOAN_ATTR_UC4 | 01 July 2026      | 1100           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "1100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    # --- Asset externalization (sale) ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-01     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    # --- Product attribute switched to IMMEDIATE after the sale ---
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_EAO_LOAN_ATTR_UC4"
    # --- Capitalized income ---
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "02 July 2026" with "50" EUR transaction amount
    Then Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 0.0              | 50.0                | 0.0             | 0.0                |
    # --- Capitalized income amortization ---
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                    | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 02 July 2026     | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1050.0       | false    |
      | 02 July 2026     | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 0.55             | 49.45               | 0.0             | 0.0                |
    And External asset owner loan attributes of the loan have a loan-level copy taken from the first sale with:
      | attributeKey                             | attributeValue |
      | CAPITALIZED_INCOME_AMORTIZATION_STRATEGY | DEFERRED       |
    # --- Closing the loan ---
    When Loan Pay-off is made on "03 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    # --- Cleanup ---
    When Admin deletes external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" for loan product "LP2_EAO_LOAN_ATTR_UC4"

  @TestRailId:C119661
  Scenario: Verify external asset owner loan-level attribute copy - UC5: resale to another investor keeps the original copy
    Given Loan product "LP2_EAO_LOAN_ATTR_UC5" exists dedicated to this feature with buy down fees
    When Admin creates external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_EAO_LOAN_ATTR_UC5" with values:
      | BUY_DOWN_FEE              |
      | BUY_DOWN_FEE_AMORTIZATION |
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_EAO_LOAN_ATTR_UC5 | 01 July 2026      | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    # --- Asset externalization (sale) ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-01     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    Then External asset owner loan attributes of the loan have a loan-level copy taken from the first sale with:
      | attributeKey               | attributeValue                         |
      | EXCLUDED_TRANSACTION_TYPES | BUY_DOWN_FEE,BUY_DOWN_FEE_AMORTIZATION |
    # --- Product attribute deleted after the sale ---
    When Admin deletes external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_EAO_LOAN_ATTR_UC5"
    # --- Resale to another investor ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-02     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    Then External asset owner loan attributes of the loan have a loan-level copy taken from the first sale with:
      | attributeKey               | attributeValue                         |
      | EXCLUDED_TRANSACTION_TYPES | BUY_DOWN_FEE,BUY_DOWN_FEE_AMORTIZATION |
    # --- Buy down fee ---
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 July 2026" with "50" EUR transaction amount
    Then Loan transaction of type "BUY_DOWN_FEE" on "03 July 2026" has journal entries with external asset owner "none"
    And LoanBuyDownFeeTransactionCreatedBusinessEvent is created on "03 July 2026" without external owner
    # --- Repayment ---
    When Customer makes "AUTOPAY" repayment on "03 July 2026" with 10 EUR transaction amount and check external owner
    Then Loan transaction of type "REPAYMENT" on "03 July 2026" has journal entries with external asset owner "active owner"
    # --- Closing the loan ---
    When Loan Pay-off is made on "03 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119662
  Scenario: Verify external asset owner loan-level attribute copy - UC6: sale, buyback and a new sale reuse the original copy
    Given Loan product "LP2_EAO_LOAN_ATTR_UC6" exists dedicated to this feature with buy down fees
    When Admin creates external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_EAO_LOAN_ATTR_UC6" with values:
      | BUY_DOWN_FEE              |
      | BUY_DOWN_FEE_AMORTIZATION |
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_EAO_LOAN_ATTR_UC6 | 01 July 2026      | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    # --- Asset externalization (sale) ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-01     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    Then External asset owner loan attributes of the loan have a loan-level copy taken from the first sale with:
      | attributeKey               | attributeValue                         |
      | EXCLUDED_TRANSACTION_TYPES | BUY_DOWN_FEE,BUY_DOWN_FEE_AMORTIZATION |
    # --- Buyback ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2026-07-02     |                    |
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    Then External asset owner loan attributes of the loan have a loan-level copy taken from the first sale with:
      | attributeKey               | attributeValue                         |
      | EXCLUDED_TRANSACTION_TYPES | BUY_DOWN_FEE,BUY_DOWN_FEE_AMORTIZATION |
    # --- Product attribute changed while the loan is not owned ---
    When Admin set external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" value "REPAYMENT" for loan product "LP2_EAO_LOAN_ATTR_UC6"
    # --- New sale ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-03     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "04 July 2026"
    And Admin runs inline COB job for Loan
    Then External asset owner loan attributes of the loan have a loan-level copy taken from the first sale with:
      | attributeKey               | attributeValue                         |
      | EXCLUDED_TRANSACTION_TYPES | BUY_DOWN_FEE,BUY_DOWN_FEE_AMORTIZATION |
    # --- Buy down fee ---
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "04 July 2026" with "50" EUR transaction amount
    Then Loan transaction of type "BUY_DOWN_FEE" on "04 July 2026" has journal entries with external asset owner "none"
    And LoanBuyDownFeeTransactionCreatedBusinessEvent is created on "04 July 2026" without external owner
    # --- Repayment ---
    When Customer makes "AUTOPAY" repayment on "04 July 2026" with 10 EUR transaction amount and check external owner
    Then Loan transaction of type "REPAYMENT" on "04 July 2026" has journal entries with external asset owner "active owner"
    # --- Closing the loan ---
    When Loan Pay-off is made on "04 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    # --- Cleanup ---
    When Admin deletes external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_EAO_LOAN_ATTR_UC6"

  @TestRailId:C119663
  Scenario: Verify external asset owner loan-level attribute copy - UC7: BUY_DOWN_FEE_AMORTIZATION_STRATEGY correction through the API affects only that loan and only later activity
    Given Loan product "LP2_EAO_LOAN_ATTR_UC7" exists dedicated to this feature with buy down fees
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_EAO_LOAN_ATTR_UC7"
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_EAO_LOAN_ATTR_UC7 | 01 July 2026      | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    # --- Asset externalization (sale) ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-01     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    # --- Buy down fee under DEFERRED ---
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "02 July 2026" with "50" EUR transaction amount
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    Then Buy down fee contains the following data:
      | Date         | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 02 July 2026 | 50.0       | 0.55             | 49.45                    | 0.0             | 0.0                |
    # --- Loan-level correction ---
    When Admin sets external asset owner loan attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for the loan
    Then External asset owner loan attributes of the loan have a loan-level copy taken from the first sale with:
      | attributeKey                       | attributeValue |
      | BUY_DOWN_FEE_AMORTIZATION_STRATEGY | IMMEDIATE      |
    And External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" for loan product "LP2_EAO_LOAN_ATTR_UC7" has values:
      | DEFERRED |
    # --- Buy down fee under IMMEDIATE ---
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 July 2026" with "5" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 02 July 2026     | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 03 July 2026     | Buy Down Fee              | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 03 July 2026     | Buy Down Fee Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date         | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 02 July 2026 | 50.0       | 0.55             | 49.45                    | 0.0             | 0.0                |
      | 03 July 2026 | 5.0        | 5.0              | 0.0                      | 0.0             | 0.0                |
    When Loan Pay-off is made on "03 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    # --- Second loan of the same product keeps the product value ---
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_EAO_LOAN_ATTR_UC7 | 03 July 2026      | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "03 July 2026" with "100" amount and expected disbursement date on "03 July 2026"
    And Admin successfully disburse the loan on "03 July 2026" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-03     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "04 July 2026"
    And Admin runs inline COB job for Loan
    Then External asset owner loan attributes of the loan have a loan-level copy taken from the first sale with:
      | attributeKey                       | attributeValue |
      | BUY_DOWN_FEE_AMORTIZATION_STRATEGY | DEFERRED       |
    # --- Buy down fee on the second loan stays DEFERRED ---
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "04 July 2026" with "50" EUR transaction amount
    Then Buy down fee contains the following data:
      | Date         | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 04 July 2026 | 50.0       | 0.0              | 50.0                     | 0.0             | 0.0                |
    # --- Closing the loan ---
    When Loan Pay-off is made on "04 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    # --- Cleanup ---
    When Admin deletes external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" for loan product "LP2_EAO_LOAN_ATTR_UC7"

  @TestRailId:C119664
  Scenario: Verify external asset owner loan-level attribute copy - UC8: CAPITALIZED_INCOME_AMORTIZATION_STRATEGY correction through the API affects only that loan and only later activity
    Given Loan product "LP2_EAO_LOAN_ATTR_UC8" exists dedicated to this feature with capitalized income
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_EAO_LOAN_ATTR_UC8"
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_EAO_LOAN_ATTR_UC8 | 01 July 2026      | 1100           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "1100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    # --- Asset externalization (sale) ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-01     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    # --- Capitalized income under DEFERRED ---
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "02 July 2026" with "50" EUR transaction amount
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    Then Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 0.55             | 49.45               | 0.0             | 0.0                |
    # --- Loan-level correction ---
    When Admin sets external asset owner loan attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "IMMEDIATE" for the loan
    Then External asset owner loan attributes of the loan have a loan-level copy taken from the first sale with:
      | attributeKey                             | attributeValue |
      | CAPITALIZED_INCOME_AMORTIZATION_STRATEGY | IMMEDIATE      |
    And External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" for loan product "LP2_EAO_LOAN_ATTR_UC8" has values:
      | DEFERRED |
    # --- Capitalized income under IMMEDIATE ---
    When Admin adds capitalized income with "AUTOPAY" payment type to the loan on "03 July 2026" with "30" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                    | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 02 July 2026     | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1050.0       | false    |
      | 02 July 2026     | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 03 July 2026     | Capitalized Income              | 30.0   | 30.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 03 July 2026     | Capitalized Income Amortization | 30.0   | 0.0       | 30.0     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 0.55             | 49.45               | 0.0             | 0.0                |
      | 30.0   | 30.0             | 0.0                 | 0.0             | 0.0                |
    When Loan Pay-off is made on "03 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    # --- Second loan of the same product keeps the product value ---
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_EAO_LOAN_ATTR_UC8 | 03 July 2026      | 1100           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "03 July 2026" with "1100" amount and expected disbursement date on "03 July 2026"
    And Admin successfully disburse the loan on "03 July 2026" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-03     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "04 July 2026"
    And Admin runs inline COB job for Loan
    Then External asset owner loan attributes of the loan have a loan-level copy taken from the first sale with:
      | attributeKey                             | attributeValue |
      | CAPITALIZED_INCOME_AMORTIZATION_STRATEGY | DEFERRED       |
    # --- Capitalized income on the second loan stays DEFERRED ---
    When Admin adds capitalized income with "AUTOPAY" payment type to the loan on "04 July 2026" with "50" EUR transaction amount
    Then Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 0.0              | 50.0                | 0.0             | 0.0                |
    # --- Closing the loan ---
    When Loan Pay-off is made on "04 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    # --- Cleanup ---
    When Admin deletes external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" for loan product "LP2_EAO_LOAN_ATTR_UC8"

  @TestRailId:C119665
  Scenario: Verify external asset owner loan-level attribute copy - UC9: correction through the API needs a loan-level copy and is validated like the product attribute (Negative)
    Given Loan product "LP2_EAO_LOAN_ATTR_UC9" exists dedicated to this feature with buy down fees
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_EAO_LOAN_ATTR_UC9"
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_EAO_LOAN_ATTR_UC9 | 01 July 2026      | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    # --- Correction on a loan without a loan-level copy ---
    Then Setting external asset owner loan attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for the loan results a 403 error and "LOAN_ATTRIBUTE_NO_LOAN_LEVEL_COPY" error message
    And External asset owner loan attributes of the loan have no loan-level copy
    # --- Asset externalization (sale) ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-01     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    # --- Invalid corrections ---
    Then Setting external asset owner loan attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "SOMETIMES" for the loan results a 403 error and "LOAN_PRODUCT_ATTRIBUTE_VALUE_INVALID" error message
    And Setting external asset owner loan attribute "EXCLUDED_TRANSACTION_TYPES" value "DEFERRED" for the loan results a 403 error and "LOAN_PRODUCT_ATTRIBUTE_VALUE_INVALID" error message
    And Setting external asset owner loan attribute "NOT_AN_ATTRIBUTE" value "IMMEDIATE" for the loan results a 403 error and "LOAN_PRODUCT_ATTRIBUTE_INVALID" error message
    And Setting external asset owner loan attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "" for the loan results a 400 error and "LOAN_PRODUCT_ATTRIBUTE_VALUE_MANDATORY" error message
    And External asset owner loan attributes of the loan have a loan-level copy taken from the first sale with:
      | attributeKey                       | attributeValue |
      | BUY_DOWN_FEE_AMORTIZATION_STRATEGY | DEFERRED       |
    # --- Valid correction is normalised like the product attribute ---
    When Admin sets external asset owner loan attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "immediate" for the loan
    Then External asset owner loan attributes of the loan have a loan-level copy taken from the first sale with:
      | attributeKey                       | attributeValue |
      | BUY_DOWN_FEE_AMORTIZATION_STRATEGY | IMMEDIATE      |
    # --- Closing the loan ---
    When Loan Pay-off is made on "02 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    # --- Cleanup ---
    When Admin deletes external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" for loan product "LP2_EAO_LOAN_ATTR_UC9"

  @TestRailId:C119666
  Scenario: Verify external asset owner loan-level attribute copy - UC10: correction through the API needs the dedicated permission
    Then Permission "UPDATE_EXTERNAL_ASSET_OWNER_LOAN_ATTRIBUTE" is returned with grouping "investor", entity "EXTERNAL_ASSET_OWNER_LOAN_ATTRIBUTE" and action "UPDATE"
    Given Loan product "LP2_EAO_LOAN_ATTR_UC10" exists dedicated to this feature with buy down fees
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_EAO_LOAN_ATTR_UC10"
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_EAO_LOAN_ATTR_UC10 | 01 July 2026      | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    # --- Asset externalization (sale) ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-01     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    # --- User without the loan attribute permission ---
    When Admin creates new user with "EAO_LOAN_ATTR_NO_PERMISSION" username, "EAO_LOAN_ATTR_NO_PERMISSION_ROLE" role name and given permissions:
      | READ_LOAN |
    Then Created user setting external asset owner loan attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for the loan results a 403 error and "USER_HAS_NO_UPDATE_LOAN_ATTRIBUTE_AUTHORITY" error message
    # --- User with the loan product attribute permission only ---
    When Admin creates new user with "EAO_LOAN_ATTR_PRODUCT_PERMISSION" username, "EAO_LOAN_ATTR_PRODUCT_PERMISSION_ROLE" role name and given permissions:
      | READ_LOAN                                          |
      | UPDATE_EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE |
    Then Created user setting external asset owner loan attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for the loan results a 403 error and "USER_HAS_NO_UPDATE_LOAN_ATTRIBUTE_AUTHORITY" error message
    And External asset owner loan attributes of the loan have a loan-level copy taken from the first sale with:
      | attributeKey                       | attributeValue |
      | BUY_DOWN_FEE_AMORTIZATION_STRATEGY | DEFERRED       |
    # --- User with the dedicated loan attribute permission ---
    When Admin creates new user with "EAO_LOAN_ATTR_PERMISSION" username, "EAO_LOAN_ATTR_PERMISSION_ROLE" role name and given permissions:
      | READ_LOAN                                  |
      | UPDATE_EXTERNAL_ASSET_OWNER_LOAN_ATTRIBUTE |
    When Created user sets external asset owner loan attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for the loan
    Then External asset owner loan attributes of the loan have a loan-level copy taken from the first sale with:
      | attributeKey                       | attributeValue |
      | BUY_DOWN_FEE_AMORTIZATION_STRATEGY | IMMEDIATE      |
    # --- Closing the loan ---
    When Loan Pay-off is made on "02 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    # --- Cleanup ---
    When Admin deletes external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" for loan product "LP2_EAO_LOAN_ATTR_UC10"

  @TestRailId:C123827
  Scenario: Verify external asset owner loan-level attribute copy - UC11: a loan sold before the loan-level copy was introduced follows the product until its next sale, which takes the copy
    Given Loan product "LP2_EAO_LOAN_ATTR_UC11" exists dedicated to this feature with buy down fees
    When Admin creates external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_EAO_LOAN_ATTR_UC11" with values:
      | BUY_DOWN_FEE |
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_EAO_LOAN_ATTR_UC11 | 01 July 2026      | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    # --- Asset externalization (sale) ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-01     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    # --- Loan sold before the upgrade: owned, but without a loan-level copy (no backfill) ---
    When Admin removes the loan-level copy of the external asset owner attributes of the loan, as for a loan sold before the loan-level copy was introduced
    Then External asset owner loan attributes of the loan have no loan-level copy
    And Setting external asset owner loan attribute "EXCLUDED_TRANSACTION_TYPES" value "REPAYMENT" for the loan results a 403 error and "LOAN_ATTRIBUTE_NO_LOAN_LEVEL_COPY" error message
    # --- Without a copy the loan follows the product: deleting the exclusion tags the buy down fee to the owner ---
    When Admin deletes external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_EAO_LOAN_ATTR_UC11"
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "02 July 2026" with "50" EUR transaction amount
    Then Loan transaction of type "BUY_DOWN_FEE" on "02 July 2026" has journal entries with external asset owner "active owner"
    And LoanBuyDownFeeTransactionCreatedBusinessEvent is created on "02 July 2026" with the active external owner
    And External asset owner loan attributes of the loan have no loan-level copy
    # --- Resale to another investor takes the copy from the product attributes at that moment ---
    When Admin creates external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_EAO_LOAN_ATTR_UC11" with values:
      | BUY_DOWN_FEE |
    And Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-02     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    Then External asset owner loan attributes of the loan have a loan-level copy taken from the latest sale with:
      | attributeKey               | attributeValue |
      | EXCLUDED_TRANSACTION_TYPES | BUY_DOWN_FEE   |
    # --- From now on the copy decides: deleting the product exclusion no longer tags the buy down fee to the owner ---
    When Admin deletes external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_EAO_LOAN_ATTR_UC11"
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 July 2026" with "20" EUR transaction amount
    Then Loan transaction of type "BUY_DOWN_FEE" on "03 July 2026" has journal entries with external asset owner "none"
    And LoanBuyDownFeeTransactionCreatedBusinessEvent is created on "03 July 2026" without external owner
    And External asset owner loan attributes of the loan have a loan-level copy taken from the latest sale with:
      | attributeKey               | attributeValue |
      | EXCLUDED_TRANSACTION_TYPES | BUY_DOWN_FEE   |
    # --- Closing the loan ---
    When Loan Pay-off is made on "03 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C123828
  Scenario: Verify external asset owner loan-level attribute copy - UC12: switching the product to DEFAULT_SETTLEMENT while an intermediary sale is pending does not block the investor sale
    Given Loan product "LP2_EAO_LOAN_ATTR_UC12" exists dedicated to this feature with buy down fees
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DELAYED_SETTLEMENT" for loan product "LP2_EAO_LOAN_ATTR_UC12"
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_EAO_LOAN_ATTR_UC12 | 01 July 2026      | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    # --- Intermediary sale initiated under DELAYED_SETTLEMENT ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | intermediarySale | 2026-07-01     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    # --- Product switched to DEFAULT_SETTLEMENT before COB executes the intermediary sale ---
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DEFAULT_SETTLEMENT" for loan product "LP2_EAO_LOAN_ATTR_UC12"
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent with transfer type: "INTERMEDIARYSALE" and transfer asset owner is created
    And External asset owner loan attributes of the loan have a loan-level copy taken from the first sale with:
      | attributeKey     | attributeValue     |
      | SETTLEMENT_MODEL | DELAYED_SETTLEMENT |
    # --- Investor sale still follows the delayed settlement the loan was sold under ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-02     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent with transfer type: "SALE" and transfer asset owner based on intermediarySale is created
    And External asset owner loan attributes of the loan have a loan-level copy taken from the first sale with:
      | attributeKey     | attributeValue     |
      | SETTLEMENT_MODEL | DELAYED_SETTLEMENT |
    # --- Closing the loan ---
    When Loan Pay-off is made on "03 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    # --- Cleanup ---
    When Admin deletes external asset owner loan product attribute "SETTLEMENT_MODEL" for loan product "LP2_EAO_LOAN_ATTR_UC12"
