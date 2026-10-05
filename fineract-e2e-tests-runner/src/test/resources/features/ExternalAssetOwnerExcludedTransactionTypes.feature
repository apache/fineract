@AssetExternalizationFeature
@ExternalAssetOwnerExcludedTransactionTypes
Feature: External Asset Owner Excluded Transaction Types

  @TestRailId:C106830
  Scenario: Verify EXCLUDED_TRANSACTION_TYPES - UC1: excluded buy down fee transactions untagged, non-excluded repayment stays tagged
    Given Loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC1" exists dedicated to this feature with buy down fees
    When Admin creates external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC1" with values:
      | BUY_DOWN_FEE                         |
      | BUY_DOWN_FEE_ADJUSTMENT              |
      | BUY_DOWN_FEE_AMORTIZATION            |
      | BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT |
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC1" has values:
      | BUY_DOWN_FEE                         |
      | BUY_DOWN_FEE_ADJUSTMENT              |
      | BUY_DOWN_FEE_AMORTIZATION            |
      | BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT |
    # --- Loan setup and disbursement ---
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_BUYDOWN_FEES_EXCLUDED_UC1 | 01 July 2026      | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    And Loan Transactions tab has a "BUY_DOWN_FEE" transaction with date "02 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit | External asset owner |
      | EXPENSE   | 450280       | Buy Down Expense            | 50.0  |        | none                 |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   | none                 |
    Then Loan transaction of type "BUY_DOWN_FEE" on "02 July 2026" has journal entries with external asset owner "none"
    And LoanBuyDownFeeTransactionCreatedBusinessEvent is created on "02 July 2026" without external owner
    # --- Repayment ---
    And Customer makes "AUTOPAY" repayment on "02 July 2026" with 10 EUR transaction amount and check external owner
    And Loan Transactions tab has a "REPAYMENT" transaction with date "02 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit | External asset owner |
      | LIABILITY | 145023       | Suspense/Clearing account | 10.0  |        | active owner         |
      | ASSET     | 112601       | Loans Receivable          |       | 10.0   | active owner         |
    Then Loan transaction of type "REPAYMENT" on "02 July 2026" has journal entries with external asset owner "active owner"
    # --- Buy down fee amortization ---
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    And Loan Transactions tab has a "BUY_DOWN_FEE_AMORTIZATION" transaction with date "02 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit | External asset owner |
      | INCOME    | 450281       | Income From Buy Down        |       | 0.55   | none                 |
      | LIABILITY | 145024       | Deferred Capitalized Income | 0.55  |        | none                 |
    Then Loan transaction of type "BUY_DOWN_FEE_AMORTIZATION" on "02 July 2026" has journal entries with external asset owner "none"
    And LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent is created on "02 July 2026" without external owner
    # --- Closing the loan ---
    When Loan Pay-off is made on "03 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    # --- Cleanup ---
    When Admin deletes external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC1"
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC1" does not exist

  @TestRailId:C106831
  Scenario: Verify EXCLUDED_TRANSACTION_TYPES - UC2: buy down fee tagged on both surfaces when attribute is absent
    Given Loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC2" exists dedicated to this feature with buy down fees
    # --- Loan setup and disbursement ---
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_BUYDOWN_FEES_EXCLUDED_UC2 | 01 July 2026      | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    And Loan Transactions tab has a "BUY_DOWN_FEE" transaction with date "02 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit | External asset owner |
      | EXPENSE   | 450280       | Buy Down Expense            | 50.0  |        | active owner         |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   | active owner         |
    Then Loan transaction of type "BUY_DOWN_FEE" on "02 July 2026" has journal entries with external asset owner "active owner"
    And LoanBuyDownFeeTransactionCreatedBusinessEvent is created on "02 July 2026" with the active external owner
    # --- Closing the loan ---
    When Loan Pay-off is made on "02 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C106832
  Scenario: Verify EXCLUDED_TRANSACTION_TYPES - UC3: unowned loan keeps all transactions untagged on both surfaces
    Given Loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC3" exists dedicated to this feature with buy down fees
    When Admin creates external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC3" with values:
      | BUY_DOWN_FEE              |
      | BUY_DOWN_FEE_ADJUSTMENT   |
      | BUY_DOWN_FEE_AMORTIZATION |
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC3" has values:
      | BUY_DOWN_FEE              |
      | BUY_DOWN_FEE_ADJUSTMENT   |
      | BUY_DOWN_FEE_AMORTIZATION |
    # --- Loan setup and disbursement ---
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_BUYDOWN_FEES_EXCLUDED_UC3 | 01 July 2026      | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    # --- Buy down fee ---
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "01 July 2026" with "50" EUR transaction amount
    And Loan Transactions tab has a "BUY_DOWN_FEE" transaction with date "01 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit | External asset owner |
      | EXPENSE   | 450280       | Buy Down Expense            | 50.0  |        | none                 |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   | none                 |
    Then Loan transaction of type "BUY_DOWN_FEE" on "01 July 2026" has journal entries with external asset owner "none"
    And LoanBuyDownFeeTransactionCreatedBusinessEvent is created on "01 July 2026" without external owner
    # --- Repayment ---
    And Customer makes "AUTOPAY" repayment on "01 July 2026" with 10 EUR transaction amount and check external owner
    And Loan Transactions tab has a "REPAYMENT" transaction with date "01 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit | External asset owner |
      | LIABILITY | 145023       | Suspense/Clearing account | 10.0  |        | none                 |
      | ASSET     | 112601       | Loans Receivable          |       | 10.0   | none                 |
    Then Loan transaction of type "REPAYMENT" on "01 July 2026" has journal entries with external asset owner "none"
    # --- Closing the loan ---
    When Loan Pay-off is made on "01 July 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    # --- Cleanup ---
    When Admin deletes external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC3"
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC3" does not exist

  @TestRailId:C106833
  Scenario: Verify EXCLUDED_TRANSACTION_TYPES - UC4: adjustment and reversal of excluded buy down fee transactions stay untagged on both surfaces
    Given Loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC4" exists dedicated to this feature with buy down fees
    When Admin creates external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC4" with values:
      | BUY_DOWN_FEE                         |
      | BUY_DOWN_FEE_ADJUSTMENT              |
      | BUY_DOWN_FEE_AMORTIZATION            |
      | BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT |
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC4" has values:
      | BUY_DOWN_FEE                         |
      | BUY_DOWN_FEE_ADJUSTMENT              |
      | BUY_DOWN_FEE_AMORTIZATION            |
      | BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT |
    # --- Loan setup and disbursement ---
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_BUYDOWN_FEES_EXCLUDED_UC4 | 01 July 2026      | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    And Loan Transactions tab has a "BUY_DOWN_FEE" transaction with date "02 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit | External asset owner |
      | EXPENSE   | 450280       | Buy Down Expense            | 50.0  |        | none                 |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   | none                 |
    Then Loan transaction of type "BUY_DOWN_FEE" on "02 July 2026" has journal entries with external asset owner "none"
    And LoanBuyDownFeeTransactionCreatedBusinessEvent is created on "02 July 2026" without external owner
    # --- Buy down fee adjustment ---
    When Admin sets the business date to "03 July 2026"
    And Admin adds buy down fee adjustment of buy down fee transaction made on "02 July 2026" with "AUTOPAY" payment type to the loan on "03 July 2026" with "50" EUR transaction amount
    And Loan Transactions tab has a "BUY_DOWN_FEE" transaction with date "02 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit | External asset owner |
      | EXPENSE   | 450280       | Buy Down Expense            | 50.0  |        | none                 |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   | none                 |
    Then Loan transaction of type "BUY_DOWN_FEE" on "02 July 2026" has journal entries with external asset owner "none"
    And Loan Transactions tab has a "BUY_DOWN_FEE_ADJUSTMENT" transaction with date "03 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit | External asset owner |
      | LIABILITY | 145024       | Deferred Capitalized Income | 50.0  |        | none                 |
      | EXPENSE   | 450280       | Buy Down Expense            |       | 50.0   | none                 |
    And Loan transaction of type "BUY_DOWN_FEE_ADJUSTMENT" on "03 July 2026" has journal entries with external asset owner "none"
    And LoanBuyDownFeeAdjustmentTransactionCreatedBusinessEvent is created on "03 July 2026" without external owner
    # --- Reversal: the adjustment first, as a buy down fee cannot be reversed while it has an active adjustment ---
    When Customer undo "1"th "Buy Down Fee Adjustment" transaction made on "03 July 2026"
    Then Loan Transactions tab has a "BUY_DOWN_FEE_ADJUSTMENT" transaction with date "03 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit | External asset owner |
      | LIABILITY | 145024       | Deferred Capitalized Income | 50.0  |        | none                 |
      | EXPENSE   | 450280       | Buy Down Expense            |       | 50.0   | none                 |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   | none                 |
      | EXPENSE   | 450280       | Buy Down Expense            | 50.0  |        | none                 |
    And LoanAdjustTransactionBusinessEvent for "Buy Down Fee Adjustment" transaction on "03 July 2026" is created without external owner
    When Customer undo "1"th "Buy Down Fee" transaction made on "02 July 2026"
    Then Loan Transactions tab has a "BUY_DOWN_FEE" transaction with date "02 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit | External asset owner |
      | EXPENSE   | 450280       | Buy Down Expense            | 50.0  |        | none                 |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   | none                 |
      | EXPENSE   | 450280       | Buy Down Expense            |       | 50.0   | none                 |
      | LIABILITY | 145024       | Deferred Capitalized Income | 50.0  |        | none                 |
    And LoanAdjustTransactionBusinessEvent for "Buy Down Fee" transaction on "02 July 2026" is created without external owner
    # --- Closing the loan ---
    When Loan Pay-off is made on "03 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    # --- Cleanup ---
    When Admin deletes external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC4"
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC4" does not exist

  @TestRailId:C106834
  Scenario: Verify EXCLUDED_TRANSACTION_TYPES - UC5: sale-time buy down fee amortization not attributed to outgoing investor
    Given Loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC5" exists dedicated to this feature with buy down fees
    When Admin creates external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC5" with values:
      | BUY_DOWN_FEE                         |
      | BUY_DOWN_FEE_ADJUSTMENT              |
      | BUY_DOWN_FEE_AMORTIZATION            |
      | BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT |
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC5" has values:
      | BUY_DOWN_FEE                         |
      | BUY_DOWN_FEE_ADJUSTMENT              |
      | BUY_DOWN_FEE_AMORTIZATION            |
      | BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT |
    # --- Loan setup and disbursement ---
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_BUYDOWN_FEES_EXCLUDED_UC5 | 01 July 2026      | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    # --- Buy down fee ---
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "01 July 2026" with "50" EUR transaction amount
    # --- Asset externalization (sale to first owner) ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-01     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    # --- Asset externalization (sale to second owner) ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-02     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    # --- Buy down fee amortization (sale-time recognition) ---
    And Loan Transactions tab has a "BUY_DOWN_FEE_AMORTIZATION" transaction with date "01 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit | External asset owner |
      | INCOME    | 450281       | Income From Buy Down        |       | 0.54   | none                 |
      | LIABILITY | 145024       | Deferred Capitalized Income | 0.54  |        | none                 |
      | INCOME    | 450281       | Income From Buy Down        |       | 49.46  | none                 |
      | LIABILITY | 145024       | Deferred Capitalized Income | 49.46 |        | none                 |
    Then Loan transaction of type "BUY_DOWN_FEE_AMORTIZATION" on "01 July 2026" has journal entries with external asset owner "none"
    And LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent is created on "01 July 2026" without external owner
    And The previous asset external owner has owner-tagged journal entries
    # --- Closing the loan ---
    When Loan Pay-off is made on "03 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    # --- Cleanup ---
    When Admin deletes external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC5"
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_BUYDOWN_FEES_EXCLUDED_UC5" does not exist

  @TestRailId:C106835
  Scenario: Verify EXCLUDED_TRANSACTION_TYPES - UC6: excluded charge-off transaction not attributed to external asset owner on both surfaces
    Given Loan product "LP2_BUYDOWN_FEES_CHARGE_OFF_UC6" exists dedicated to this feature with buy down fees and charge off reasons
    When Admin creates external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_BUYDOWN_FEES_CHARGE_OFF_UC6" with values:
      | CHARGE_OFF |
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_BUYDOWN_FEES_CHARGE_OFF_UC6" has values:
      | CHARGE_OFF |
    # --- Loan setup and disbursement ---
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_BUYDOWN_FEES_CHARGE_OFF_UC6 | 01 July 2026      | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    # --- Charge-off ---
    And Admin does charge-off the loan with reason "OTHER" on "02 July 2026"
    And LoanChargeOffPostBusinessEvent is created on "02 July 2026" without external owner
    And Loan Transactions tab has a "CHARGE_OFF" transaction with date "02 July 2026" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit | External asset owner |
      | ASSET   | 112601       | Loans Receivable           |       | 100.0  | none                 |
      | ASSET   | 112603       | Interest/Fee Receivable    |       | 1.17   | none                 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 100.0 |        | none                 |
      | INCOME  | 404001       | Interest Income Charge Off | 1.17  |        | none                 |
    Then Loan transaction of type "CHARGE_OFF" on "02 July 2026" has journal entries with external asset owner "none"
    # --- Cleanup ---
    When Admin deletes external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_BUYDOWN_FEES_CHARGE_OFF_UC6"
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP2_BUYDOWN_FEES_CHARGE_OFF_UC6" does not exist
