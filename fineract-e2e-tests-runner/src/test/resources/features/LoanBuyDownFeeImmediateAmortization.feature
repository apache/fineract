@BuyDownFeeFeature
@AssetExternalizationFeature
@AssetExternalizationImmediateBuyDownFeeAmortization
Feature: Immediate Buy Down Fee Amortization

  @TestRailId:C111060
  Scenario: Verify Immediate Buy Down Fee Amortization - UC1: Fee posted after sale is fully recognized with no COB claw-back
    # --- Product attribute setup ---
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    # --- Loan creation and disbursement ---
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 January 2026   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "100" EUR transaction amount
    # --- External asset owner sale ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-01-02     | 1                  |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2026-01-02     | 1                  | PENDING | 2026-01-01    | 2026-01-02  | SALE             |
      | 2026-01-02     | 1                  | ACTIVE  | 2026-01-03    | 9999-12-31  | SALE             |
    # --- Buy down fee posting ---
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 January 2026" with "5" EUR transaction amount
    # --- Immediate recognition verification ---
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
    # --- COB processing to confirm no claw-back ---
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
    # --- Pay-off and cleanup ---
    When Loan Pay-off is made on "10 January 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"

  @TestRailId:C111061
  Scenario: Verify Immediate Buy Down Fee Amortization - UC2: Fee posted before sale stays deferred on IMMEDIATE product
    # --- Product attribute setup ---
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    # --- Loan creation and disbursement ---
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 January 2026   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "100" EUR transaction amount
    # --- Buy down fee posting (loan not sold, so stays deferred) ---
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "01 January 2026" with "5" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2026  | Buy Down Fee     | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 01 January 2026 | 5.0        | 0.0              | 5.0                      | 0.0             | 0.0                |
    # --- Pay-off and cleanup ---
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"

  @TestRailId:C111062
  Scenario: Verify Immediate Buy Down Fee Amortization - UC3: Positive adjustment after sale immediately adjusts recognized income
    # --- Product attribute setup ---
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    # --- Loan creation, disbursement and sale ---
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
    # --- Buy down fee posting and immediate recognition ---
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 January 2026" with "10" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee              | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
    # --- Buy down fee adjustment ---
    When Admin adds buy down fee adjustment with "AUTOPAY" payment type to the loan on "03 January 2026" with "3" EUR transaction amount
    # --- Immediate adjustment recognition verification ---
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
    # --- Pay-off and cleanup ---
    When Loan Pay-off is made on "03 January 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"

  @TestRailId:C111063
  Scenario: Verify Immediate Buy Down Fee Amortization - UC4: No double recognition when fee is posted on already closed loan
    # --- Product attribute setup ---
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    # --- Loan creation, disbursement and sale ---
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
    # --- Buy down fee posting and immediate recognition ---
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 January 2026" with "5" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee              | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
    # --- Pay-off triggers closure branch; guard that it does not re-recognize ---
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
    And Loan Transactions tab has a "BUY_DOWN_FEE_AMORTIZATION" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 450281       | Income From Buy Down        |       | 5.0    |
      | LIABILITY | 145024       | Deferred Capitalized Income | 5.0   |        |
    # --- Cleanup ---
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"

  @TestRailId:C111064
  Scenario: Verify Immediate Buy Down Fee Amortization - UC5: Switching product from DEFERRED to IMMEDIATE after sale does not touch already deferred balances
    # --- Product attribute setup (DEFERRED) ---
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    # --- Loan creation, disbursement and sale ---
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
    # --- Buy down fee posted while product is DEFERRED ---
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 January 2026" with "50" EUR transaction amount
    Then Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 January 2026 | 50.0       | 0.0              | 50.0                     | 0.0             | 0.0                |
    # --- Daily amortization under DEFERRED ---
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
    # --- Switch to IMMEDIATE and post new fee ---
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
    # --- COB confirms mixed deferred/immediate state ---
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
    # --- Pay-off and cleanup ---
    When Loan Pay-off is made on "05 January 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"

  @TestRailId:C111065
  Scenario: Verify Immediate Buy Down Fee Amortization - UC6: Reversing buy down fee reverses immediate amortization
    # --- Product attribute setup ---
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    # --- Loan creation, disbursement and sale ---
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
    # --- Buy down fee posting and immediate recognition ---
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 January 2026" with "5" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee              | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
    # --- Reversal of buy down fee ---
    When Customer undo "1"th "Buy Down Fee" transaction made on "03 January 2026"
    # --- Reversal adjustments verification ---
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                     | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement                         | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 03 January 2026  | Buy Down Fee                         | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 03 January 2026  | Buy Down Fee Amortization            | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Buy Down Fee Amortization Adjustment | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    # --- Pay-off and cleanup ---
    When Loan Pay-off is made on "03 January 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"

  @TestRailId:C111066
  Scenario: Verify Immediate Buy Down Fee Amortization - UC7: DEFERRED product defers fee and amortizes daily
    # --- Product attribute setup (DEFERRED) ---
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    # --- Loan creation, disbursement and sale ---
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
    # --- Buy down fee posting stays deferred ---
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 January 2026" with "50" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee     | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 January 2026 | 50.0       | 0.0              | 50.0                     | 0.0             | 0.0                |
    # --- Daily amortization on subsequent COB ---
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 January 2026 | 50.0       | 1.14             | 48.86                    | 0.0             | 0.0                |
    # --- Pay-off and cleanup ---
    When Loan Pay-off is made on "05 January 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"

  @TestRailId:C111067
  Scenario: Verify Immediate Buy Down Fee Amortization - UC8: Multiple buy down fees posted after sale are each immediately recognized
    # --- Product attribute setup ---
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    # --- Loan creation, disbursement and sale ---
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
    # --- First buy down fee posting and immediate recognition ---
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 January 2026" with "5" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee              | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 January 2026 | 5.0        | 5.0              | 0.0                      | 0.0             | 0.0                |
    # --- Second buy down fee posting and immediate recognition ---
    When Admin sets the business date to "04 January 2026"
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "04 January 2026" with "10" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee              | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee              | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 January 2026 | 5.0        | 5.0              | 0.0                      | 0.0             | 0.0                |
      | 04 January 2026 | 10.0       | 10.0             | 0.0                      | 0.0             | 0.0                |
    # --- COB confirms no claw-back for fully recognized balances ---
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee              | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee              | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 January 2026 | 5.0        | 5.0              | 0.0                      | 0.0             | 0.0                |
      | 04 January 2026 | 10.0       | 10.0             | 0.0                      | 0.0             | 0.0                |
    # --- Pay-off and cleanup ---
    When Loan Pay-off is made on "05 January 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"

  @TestRailId:C111068
  Scenario: Verify Immediate Buy Down Fee Amortization - UC9: Buy down fee disabled attribute is ignored
    # --- Product attribute setup on a non-buydown-fee product ---
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE"
    # --- Loan creation and disbursement ---
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2026   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "100" EUR transaction amount
    # --- COB has no buy down fee side effects ---
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
    # --- Cleanup ---
    When Loan Pay-off is made on "02 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE"

  @TestRailId:C111069
  Scenario: Verify Immediate Buy Down Fee Amortization - UC10: Reversing buy down fee adjustment on sold IMMEDIATE loan re-recognizes income
    # --- Product attribute setup ---
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    # --- Loan creation, disbursement and sale ---
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
    # --- Fee + adjustment with immediate recognition ---
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 January 2026" with "10" EUR transaction amount
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
    # --- Reverse adjustment: re-recognize the previously adjusted income ---
    When Customer undo "1"th "Buy Down Fee Adjustment" transaction made on "03 January 2026"
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                     | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement                         | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 03 January 2026  | Buy Down Fee                         | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Buy Down Fee Amortization            | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Buy Down Fee Adjustment              | 3.0    | 0.0       | 3.0      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 03 January 2026  | Buy Down Fee Amortization Adjustment | 3.0    | 0.0       | 3.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Buy Down Fee Amortization            | 3.0    | 0.0       | 3.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 January 2026 | 10.0       | 10.0             | 0.0                      | 0.0             | 0.0                |
    # --- Pay-off and cleanup ---
    When Loan Pay-off is made on "03 January 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"

  @TestRailId:C111081
  Scenario: Verify Immediate Buy Down Fee Amortization - UC11: Fee posted without strategy stays deferred, fee posted after switching to IMMEDIATE is not reopened by COB, sale to new owner recognizes only the deferred remainder
    # --- Product attribute setup: no buy down fee amortization strategy configured ---
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DEFAULT_SETTLEMENT" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    When Admin deletes external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES" does not exist
    # --- Loan creation, disbursement and sale to first owner ---
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
    Then LoanOwnershipTransferBusinessEvent is created
    And Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2026-01-02     | 1                  | PENDING | 2026-01-01    | 2026-01-02  | SALE             |
      | 2026-01-02     | 1                  | ACTIVE  | 2026-01-03    | 9999-12-31  | SALE             |
    # --- 1st buy down fee posted on sold loan without strategy: deferred ---
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 January 2026" with "50" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee     | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 January 2026 | 50.0       | 0.0              | 50.0                     | 0.0             | 0.0                |
    # --- Daily amortization of 1st buy down fee ---
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
    # --- Switch to IMMEDIATE and post 2nd buy down fee on still-sold loan: fully amortized at once ---
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "04 January 2026" with "5" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee              | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "BUY_DOWN_FEE_AMORTIZATION" transaction with date "04 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 450281       | Income From Buy Down        |       | 5.0    |
      | LIABILITY | 145024       | Deferred Capitalized Income | 5.0   |        |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 January 2026 | 50.0       | 0.57             | 49.43                    | 0.0             | 0.0                |
      | 04 January 2026 | 5.0        | 5.0              | 0.0                      | 0.0             | 0.0                |
    And LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent is created on "04 January 2026"
    # --- Next days COB: 1st fee keeps daily amortization, 2nd fee is not adjusted or reopened ---
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
    When Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee              | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Buy Down Fee Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 January 2026 | 50.0       | 1.7              | 48.3                     | 0.0             | 0.0                |
      | 04 January 2026 | 5.0        | 5.0              | 0.0                      | 0.0             | 0.0                |
    # --- Sale to a new owner: remaining deferred amount of 1st fee is fully recognized, 2nd fee untouched ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-01-06     | 1                  |
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee              | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Buy Down Fee Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Buy Down Fee Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Buy Down Fee Amortization | 47.73  | 0.0       | 47.73    | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has 2 a "BUY_DOWN_FEE_AMORTIZATION" transactions with date "06 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 450281       | Income From Buy Down        |       | 0.57   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 0.57  |        |
      | INCOME    | 450281       | Income From Buy Down        |       | 47.73  |
      | LIABILITY | 145024       | Deferred Capitalized Income | 47.73 |        |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 January 2026 | 50.0       | 50.0             | 0.0                      | 0.0             | 0.0                |
      | 04 January 2026 | 5.0        | 5.0              | 0.0                      | 0.0             | 0.0                |
    # Immediate and sale-time recognition both stay on the previous owner
    And The previous asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName               | entryType | amount |
      | EXPENSE       | 450280        | Buy Down Expense            | DEBIT     | 50.00  |
      | LIABILITY     | 145024        | Deferred Capitalized Income | CREDIT    | 50.00  |
      | EXPENSE       | 450280        | Buy Down Expense            | DEBIT     | 5.00   |
      | LIABILITY     | 145024        | Deferred Capitalized Income | CREDIT    | 5.00   |
      | INCOME        | 450281        | Income From Buy Down        | CREDIT    | 5.00   |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 5.00   |
      | INCOME        | 450281        | Income From Buy Down        | CREDIT    | 0.57   |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 0.57   |
      | INCOME        | 450281        | Income From Buy Down        | CREDIT    | 0.56   |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 0.56   |
      | INCOME        | 450281        | Income From Buy Down        | CREDIT    | 47.73  |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 47.73  |
    # --- COB after sale: no further amortization or adjustment on either fee ---
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee              | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Buy Down Fee Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Buy Down Fee Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Buy Down Fee Amortization | 47.73  | 0.0       | 47.73    | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 January 2026 | 50.0       | 50.0             | 0.0                      | 0.0             | 0.0                |
      | 04 January 2026 | 5.0        | 5.0              | 0.0                      | 0.0             | 0.0                |
    # --- Pay-off and cleanup ---
    When Loan Pay-off is made on "08 January 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
