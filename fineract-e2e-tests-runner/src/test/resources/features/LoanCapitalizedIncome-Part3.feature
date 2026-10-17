@CapitalizedIncomeFeature @CapitalizedIncomeAmortizationStrategyReset
Feature: Capitalized Income - Part3

  @TestRailId:C111033
  Scenario: Verify capitalized income stays deferred on an unsold loan when amortization strategy IMMEDIATE and is recognized immediately once the loan is sold - UC1
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 July 2026      | 1100           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "1100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "1000" EUR transaction amount
    # --- Loan is not sold yet: IMMEDIATE does not apply, capitalized income is deferred ---
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "01 July 2026" with "80" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement       | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Capitalized Income | 80.0   | 80.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 80.0   | 0.0              | 80.0                | 0.0             | 0.0                |
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 80.0   | 0.87             | 79.13               | 0.0             | 0.0                |
    # --- Sale: remaining deferred amount is recognized on the settlement date ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-02     | 1                  |
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2026-07-02     | 1                  | PENDING | 2026-07-02    | 2026-07-02  | SALE             |
      | 2026-07-02     | 1                  | ACTIVE  | 2026-07-03    | 9999-12-31  | SALE             |
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                    | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Capitalized Income              | 80.0   | 80.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 01 July 2026     | Capitalized Income Amortization | 0.87   | 0.0       | 0.87     | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Capitalized Income Amortization | 0.87   | 0.0       | 0.87     | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Capitalized Income Amortization | 78.26  | 0.0       | 78.26    | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 80.0   | 80.0             | 0.0                 | 0.0             | 0.0                |
    # --- Loan is sold: new capitalized income is recognized immediately ---
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "03 July 2026" with "20" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                    | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Capitalized Income              | 80.0   | 80.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 01 July 2026     | Capitalized Income Amortization | 0.87   | 0.0       | 0.87     | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Capitalized Income Amortization | 0.87   | 0.0       | 0.87     | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Capitalized Income Amortization | 78.26  | 0.0       | 78.26    | 0.0  | 0.0       | 0.0          | false    |
      | 03 July 2026     | Capitalized Income              | 20.0   | 20.0      | 0.0      | 0.0  | 0.0       | 1100.0       | false    |
      | 03 July 2026     | Capitalized Income Amortization | 20.0   | 0.0       | 20.0     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 80.0   | 80.0             | 0.0                 | 0.0             | 0.0                |
      | 20.0   | 20.0             | 0.0                 | 0.0             | 0.0                |
    When Loan Pay-off is made on "03 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C111034
  Scenario: Verify capitalized income when amortization strategy IMMEDIATE - backdated capitalized income case - UC2
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 July 2026      | 1100           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "1100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "1000" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-01     | 1                  |
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    # --- Backdated capitalized income on sold loan is recognized immediately on its transaction date ---
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "02 July 2026" with "80" EUR transaction amount
    And Admin adds capitalized income adjustment with "AUTOPAY" payment type to the loan on "03 July 2026" with "50" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                           | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                               | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 02 July 2026     | Capitalized Income                         | 80.0   | 80.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 02 July 2026     | Capitalized Income Amortization            | 80.0   | 0.0       | 80.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 July 2026     | Capitalized Income Adjustment              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1030.0       | false    |
      | 03 July 2026     | Capitalized Income Amortization Adjustment | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 80.0   | 30.0             | 0.0                 | 50.0            | 0.0                |
    When Admin sets the business date to "04 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                           | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                               | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 02 July 2026     | Capitalized Income                         | 80.0   | 80.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 02 July 2026     | Capitalized Income Amortization            | 80.0   | 0.0       | 80.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 July 2026     | Capitalized Income Adjustment              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1030.0       | false    |
      | 03 July 2026     | Capitalized Income Amortization Adjustment | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 80.0   | 30.0             | 0.0                 | 50.0            | 0.0                |
    When Loan Pay-off is made on "04 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C111035
  Scenario: Verify immediate capitalized income amortization posts journal entries and is not clawed back by COB - UC3
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 July 2026      | 1100           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "1100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "1000" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-01     | 1                  |
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "02 July 2026" with "80" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                    | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 02 July 2026     | Capitalized Income              | 80.0   | 80.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 02 July 2026     | Capitalized Income Amortization | 80.0   | 0.0       | 80.0     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 80.0   | 80.0             | 0.0                 | 0.0             | 0.0                |
    And Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "02 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 404000       | Interest Income             |       | 80.0   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 80.0  |        |
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "04 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                    | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 02 July 2026     | Capitalized Income              | 80.0   | 80.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 02 July 2026     | Capitalized Income Amortization | 80.0   | 0.0       | 80.0     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 80.0   | 80.0             | 0.0                 | 0.0             | 0.0                |
    # --- Pay-off triggers closure branch; guard that it does not re-recognize ---
    When Loan Pay-off is made on "04 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                    | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 02 July 2026     | Capitalized Income              | 80.0    | 80.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 02 July 2026     | Capitalized Income Amortization | 80.0    | 0.0       | 80.0     | 0.0  | 0.0       | 0.0          | false    |
      | 04 July 2026     | Repayment                       | 1080.59 | 1080.0    | 0.59     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 80.0   | 80.0             | 0.0                 | 0.0             | 0.0                |
    And Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "02 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 404000       | Interest Income             |       | 80.0   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 80.0  |        |

  @TestRailId:C111036
  Scenario: Verify existing deferred capitalized income stays deferred when amortization strategy is changed to IMMEDIATE - UC4
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 July 2026      | 1100           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "1100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "1000" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-01     | 1                  |
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    # --- Capitalized income on sold loan while DEFERRED: amortized daily ---
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "02 July 2026" with "50" EUR transaction amount
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "04 July 2026"
    And Admin runs inline COB job for Loan
    Then Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 1.1              | 48.9                | 0.0             | 0.0                |
    # --- Switch to IMMEDIATE: only the new capitalized income is recognized immediately ---
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "04 July 2026" with "30" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                    | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 02 July 2026     | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1050.0       | false    |
      | 02 July 2026     | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 03 July 2026     | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 04 July 2026     | Capitalized Income              | 30.0   | 30.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 04 July 2026     | Capitalized Income Amortization | 30.0   | 0.0       | 30.0     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 1.1              | 48.9                | 0.0             | 0.0                |
      | 30.0   | 30.0             | 0.0                 | 0.0             | 0.0                |
    # --- COB confirms mixed deferred/immediate state: the older balance keeps amortizing daily, the IMMEDIATE one is untouched ---
    When Admin sets the business date to "05 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                    | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 02 July 2026     | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1050.0       | false    |
      | 02 July 2026     | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 03 July 2026     | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 04 July 2026     | Capitalized Income              | 30.0   | 30.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 04 July 2026     | Capitalized Income Amortization | 30.0   | 0.0       | 30.0     | 0.0  | 0.0       | 0.0          | false    |
      | 04 July 2026     | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 1.65             | 48.35               | 0.0             | 0.0                |
      | 30.0   | 30.0             | 0.0                 | 0.0             | 0.0                |
    When Loan Pay-off is made on "05 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C115193
  Scenario: Verify Immediate Capitalized Income Amortization: Reversing capitalized income reverses its immediate amortization on the business date of the reversal - UC5
    # --- Product attribute setup ---
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    # --- Loan creation, disbursement and sale ---
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2026   | 110            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "110" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "100" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-01-02     | 1                  |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Loan
    # --- Capitalized income posting and immediate recognition ---
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "03 January 2026" with "5" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Capitalized Income              | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 105.0        | false    |
      | 03 January 2026  | Capitalized Income Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Loan
    # --- Reversal of capitalized income on a later day ---
    When Customer undo "1"th "Capitalized Income" transaction made on "03 January 2026"
    # --- Reversal adjustment is posted on the business date of the undo (same behaviour as buy down fee, PR #6489) ---
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                           | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement                               | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 03 January 2026  | Capitalized Income                         | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 105.0        | true     | false    |
      | 03 January 2026  | Capitalized Income Amortization            | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2026  | Capitalized Income Amortization Adjustment | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    # --- COB after the reversal changes nothing ---
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                           | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement                               | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 03 January 2026  | Capitalized Income                         | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 105.0        | true     | false    |
      | 03 January 2026  | Capitalized Income Amortization            | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2026  | Capitalized Income Amortization Adjustment | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    # --- Pay-off and cleanup ---
    When Loan Pay-off is made on "05 January 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C115194
  Scenario: Verify Immediate Capitalized Income Amortization: DEFERRED product defers capitalized income and amortizes daily - UC6
    # --- Product attribute setup (DEFERRED) ---
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    # --- Loan creation, disbursement and sale ---
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2026   | 150            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "150" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "100" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-01-02     | 1                  |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Loan
    # --- capitalized income posting stays deferred ---
    When Admin adds capitalized income with "AUTOPAY" payment type to the loan on "03 January 2026" with "50" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Capitalized Income  | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 0.0              | 50.0                | 0.0             | 0.0                |
    # --- Daily amortization on subsequent COB ---
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 03 January 2026  | Capitalized Income Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Capitalized Income Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 1.14             | 48.86               | 0.0             | 0.0                |
    # --- Pay-off and cleanup ---
    When Loan Pay-off is made on "05 January 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C115195
  Scenario: Verify Immediate Capitalized Income Amortization: Multiple capitalized incomes posted after sale are each immediately recognized - UC7
    # --- Product attribute setup ---
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    # --- Loan creation, disbursement and sale ---
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2026   | 120            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "120" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "100" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-01-02     | 1                  |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Loan
    # --- First capitalized income posting and immediate recognition ---
    When Admin adds capitalized income with "AUTOPAY" payment type to the loan on "03 January 2026" with "5" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Capitalized Income              | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 105.0        | false    |
      | 03 January 2026  | Capitalized Income Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 5.0    | 5.0              | 0.0                 | 0.0             | 0.0                |
    # --- Second capitalized income posting and immediate recognition ---
    When Admin sets the business date to "04 January 2026"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "04 January 2026" with "10" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Capitalized Income              | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 105.0        | false    |
      | 03 January 2026  | Capitalized Income Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Capitalized Income              | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 115.0        | false    |
      | 04 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 5.0    | 5.0              | 0.0                 | 0.0             | 0.0                |
      | 10.0   | 10.0             | 0.0                 | 0.0             | 0.0                |
    # --- COB confirms no claw-back for fully recognized balances ---
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Capitalized Income              | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 105.0        | false    |
      | 03 January 2026  | Capitalized Income Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Capitalized Income              | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 115.0        | false    |
      | 04 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 5.0    | 5.0              | 0.0                 | 0.0             | 0.0                |
      | 10.0   | 10.0             | 0.0                 | 0.0             | 0.0                |
    # --- Pay-off and cleanup ---
    When Loan Pay-off is made on "05 January 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C115196
  Scenario: Verify Immediate Capitalized Income Amortization: Reversing capitalized income adjustment on sold IMMEDIATE loan re-recognizes income - UC8
    # --- Product attribute setup ---
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    # --- Loan creation, disbursement and sale ---
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2026   | 120            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "120" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "100" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-01-02     | 1                  |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Loan
    # --- Capitalized income + adjustment with immediate recognition ---
    When Admin adds capitalized income with "AUTOPAY" payment type to the loan on "03 January 2026" with "10" EUR transaction amount
    When Admin adds capitalized income adjustment with "AUTOPAY" payment type to the loan on "03 January 2026" with "3" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                           | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                               | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Capitalized Income                         | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 110.0        | false    |
      | 03 January 2026  | Capitalized Income Amortization            | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Capitalized Income Adjustment              | 3.0    | 3.0       | 0.0      | 0.0  | 0.0       | 107.0        | false    |
      | 03 January 2026  | Capitalized Income Amortization Adjustment | 3.0    | 0.0       | 3.0      | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 10.0   | 7.0              | 0.0                 | 3.0             | 0.0                |
    # --- Reverse adjustment: the 3 is re-recognized on the same day (same behaviour as buy down fee, PR #6489) ---
    When Customer undo "1"th "Capitalized Income Adjustment" transaction made on "03 January 2026"
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                           | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement                               | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 03 January 2026  | Capitalized Income                         | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 110.0        | false    | false    |
      | 03 January 2026  | Capitalized Income Amortization            | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Capitalized Income Adjustment              | 3.0    | 3.0       | 0.0      | 0.0  | 0.0       | 107.0        | true     | false    |
      | 03 January 2026  | Capitalized Income Amortization Adjustment | 3.0    | 0.0       | 3.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Capitalized Income Amortization            | 3.0    | 0.0       | 3.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 10.0   | 10.0             | 0.0                 | 0.0             | 0.0                |
    # --- COB after the undo changes nothing: no claw-back, no daily amortization on this balance ---
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                           | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement                               | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 03 January 2026  | Capitalized Income                         | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 110.0        | false    | false    |
      | 03 January 2026  | Capitalized Income Amortization            | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Capitalized Income Adjustment              | 3.0    | 3.0       | 0.0      | 0.0  | 0.0       | 107.0        | true     | false    |
      | 03 January 2026  | Capitalized Income Amortization Adjustment | 3.0    | 0.0       | 3.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2026  | Capitalized Income Amortization            | 3.0    | 0.0       | 3.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 10.0   | 10.0             | 0.0                 | 0.0             | 0.0                |
    # --- Pay-off and cleanup ---
    When Loan Pay-off is made on "05 January 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C115197
  Scenario: Verify Immediate Capitalized Income Amortization: Capitalized income posted without strategy stays deferred, posted after switching to IMMEDIATE is not reopened by COB, sale to new owner recognizes only the deferred remainder - UC9
    # --- Product attribute setup: no capitalized income amortization strategy configured ---
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DEFAULT_SETTLEMENT" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    When Admin deletes external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME" does not exist
    # --- Loan creation, disbursement and sale to first owner ---
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2026   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "200" amount and expected disbursement date on "01 January 2026"
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
    # --- 1st capitalized income posted on sold loan without strategy: deferred ---
    When Admin adds capitalized income with "AUTOPAY" payment type to the loan on "03 January 2026" with "50" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Capitalized Income | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 0.0              | 50.0                | 0.0             | 0.0                |
    # --- Daily amortization of 1st capitalized income ---
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 03 January 2026  | Capitalized Income Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 0.57             | 49.43               | 0.0             | 0.0                |
    # --- Switch to IMMEDIATE and post 2nd capitalized income on still-sold loan: fully amortized at once ---
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    When Admin adds capitalized income with "AUTOPAY" payment type to the loan on "04 January 2026" with "5" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 03 January 2026  | Capitalized Income Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Capitalized Income              | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 155.0        | false    |
      | 04 January 2026  | Capitalized Income Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "04 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 404000       | Interest Income             |       | 5.0    |
      | LIABILITY | 145024       | Deferred Capitalized Income | 5.0   |        |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 0.57             | 49.43               | 0.0             | 0.0                |
      | 5.0    | 5.0              | 0.0                 | 0.0             | 0.0                |
    And LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent is raised on "04 January 2026"
    # --- Next days COB: 1st capitalized income keeps daily amortization, 2nd capitalized income is not adjusted or reopened ---
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 03 January 2026  | Capitalized Income Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Capitalized Income              | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 155.0        | false    |
      | 04 January 2026  | Capitalized Income Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Capitalized Income Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 1.14             | 48.86               | 0.0             | 0.0                |
      | 5.0    | 5.0              | 0.0                 | 0.0             | 0.0                |
    When Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 03 January 2026  | Capitalized Income Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Capitalized Income              | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 155.0        | false    |
      | 04 January 2026  | Capitalized Income Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Capitalized Income Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Capitalized Income Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 1.7              | 48.3                | 0.0             | 0.0                |
      | 5.0    | 5.0              | 0.0                 | 0.0             | 0.0                |
    # --- Sale to a new owner: remaining deferred amount of 1st capitalized income is fully recognized, 2nd capitalized income untouched ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-01-06     | 1                  |
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 03 January 2026  | Capitalized Income Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Capitalized Income              | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 155.0        | false    |
      | 04 January 2026  | Capitalized Income Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Capitalized Income Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Capitalized Income Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Capitalized Income Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Capitalized Income Amortization | 47.73  | 0.0       | 47.73    | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has 2 a "CAPITALIZED_INCOME_AMORTIZATION" transactions with date "06 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 404000       | Interest Income             |       | 0.57   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 0.57  |        |
      | INCOME    | 404000       | Interest Income             |       | 47.73  |
      | LIABILITY | 145024       | Deferred Capitalized Income | 47.73 |        |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 50.0             | 0.0                 | 0.0             | 0.0                |
      | 5.0    | 5.0              | 0.0                 | 0.0             | 0.0                |
    # Immediate and sale-time recognition both stay on the previous owner
    And The previous asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName               | entryType | amount |
      | ASSET         | 112601        | Loans Receivable            | DEBIT     | 50.00  |
      | LIABILITY     | 145024        | Deferred Capitalized Income | CREDIT    | 50.00  |
      | ASSET         | 112601        | Loans Receivable            | DEBIT     | 5.00   |
      | LIABILITY     | 145024        | Deferred Capitalized Income | CREDIT    | 5.00   |
      | INCOME        | 404000        | Interest Income             | CREDIT    | 5.00   |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 5.00   |
      | INCOME        | 404000        | Interest Income             | CREDIT    | 0.57   |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 0.57   |
      | INCOME        | 404000        | Interest Income             | CREDIT    | 0.56   |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 0.56   |
      | INCOME        | 404000        | Interest Income             | CREDIT    | 47.73  |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 47.73  |
    # --- COB after sale: no further amortization or adjustment on either capitalized income ---
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 03 January 2026  | Capitalized Income Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Capitalized Income              | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 155.0        | false    |
      | 04 January 2026  | Capitalized Income Amortization | 5.0    | 0.0       | 5.0      | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Capitalized Income Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Capitalized Income Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Capitalized Income Amortization | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Capitalized Income Amortization | 47.73  | 0.0       | 47.73    | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 50.0             | 0.0                 | 0.0             | 0.0                |
      | 5.0    | 5.0              | 0.0                 | 0.0             | 0.0                |
    # --- Pay-off and cleanup ---
    When Loan Pay-off is made on "08 January 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C115272
  Scenario: Verify Immediate Capitalized Income Amortization: Adjustment and its reversal on the second of two capitalized incomes are recognized immediately for that balance - UC10
    # --- Product attribute setup ---
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    # --- Loan creation, disbursement and sale ---
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2026   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "200" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "100" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-01-02     | 1                  |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Loan
    # --- Two capitalized incomes, both immediately recognized ---
    When Admin adds capitalized income with "AUTOPAY" payment type to the loan on "03 January 2026" with "50" EUR transaction amount
    When Admin sets the business date to "04 January 2026"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "04 January 2026" with "30" EUR transaction amount
    Then Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 50.0             | 0.0                 | 0.0             | 0.0                |
      | 30.0   | 30.0             | 0.0                 | 0.0             | 0.0                |
    # --- Adjustment on the SECOND capitalized income is recognized immediately against that balance ---
    When Admin adds capitalized income adjustment of capitalized income transaction made on "04 January 2026" with "AUTOPAY" payment type to the loan on "04 January 2026" with "10" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                           | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                               | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 January 2026  | Capitalized Income                         | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 03 January 2026  | Capitalized Income Amortization            | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Capitalized Income                         | 30.0   | 30.0      | 0.0      | 0.0  | 0.0       | 180.0        | false    |
      | 04 January 2026  | Capitalized Income Amortization            | 30.0   | 0.0       | 30.0     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Capitalized Income Adjustment              | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 170.0        | false    |
      | 04 January 2026  | Capitalized Income Amortization Adjustment | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 50.0             | 0.0                 | 0.0             | 0.0                |
      | 30.0   | 20.0             | 0.0                 | 10.0            | 0.0                |
    # --- Reversal of that adjustment re-recognizes the 10 on the same day, on the same balance ---
    When Customer undo "1"th "Capitalized Income Adjustment" transaction made on "04 January 2026"
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                           | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement                               | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 03 January 2026  | Capitalized Income                         | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 03 January 2026  | Capitalized Income Amortization            | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2026  | Capitalized Income                         | 30.0   | 30.0      | 0.0      | 0.0  | 0.0       | 180.0        | false    | false    |
      | 04 January 2026  | Capitalized Income Amortization            | 30.0   | 0.0       | 30.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2026  | Capitalized Income Adjustment              | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 170.0        | true     | false    |
      | 04 January 2026  | Capitalized Income Amortization Adjustment | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2026  | Capitalized Income Amortization            | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 50.0             | 0.0                 | 0.0             | 0.0                |
      | 30.0   | 30.0             | 0.0                 | 0.0             | 0.0                |
    # --- COB after the undo changes nothing: no claw-back, no daily amortization ---
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                           | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement                               | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 03 January 2026  | Capitalized Income                         | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 03 January 2026  | Capitalized Income Amortization            | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2026  | Capitalized Income                         | 30.0   | 30.0      | 0.0      | 0.0  | 0.0       | 180.0        | false    | false    |
      | 04 January 2026  | Capitalized Income Amortization            | 30.0   | 0.0       | 30.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2026  | Capitalized Income Adjustment              | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 170.0        | true     | false    |
      | 04 January 2026  | Capitalized Income Amortization Adjustment | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2026  | Capitalized Income Amortization            | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 50.0             | 0.0                 | 0.0             | 0.0                |
      | 30.0   | 30.0             | 0.0                 | 0.0             | 0.0                |
    # --- Pay-off and cleanup ---
    When Loan Pay-off is made on "06 January 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C115273
  Scenario: Verify Immediate Capitalized Income Amortization: Adjustment of an IMMEDIATE capitalized income does not recognize an older deferred balance - UC11
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 July 2026      | 1100           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "1100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "1000" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-01     | 1                  |
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    # --- Old capitalized income on sold loan under DEFERRED, amortized daily ---
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "02 July 2026" with "50" EUR transaction amount
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "04 July 2026"
    And Admin runs inline COB job for Loan
    Then Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 1.1              | 48.9                | 0.0             | 0.0                |
    # --- New capitalized income under IMMEDIATE, then an adjustment on it ---
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "04 July 2026" with "30" EUR transaction amount
    And Admin adds capitalized income adjustment of capitalized income transaction made on "04 July 2026" with "AUTOPAY" payment type to the loan on "04 July 2026" with "10" EUR transaction amount
    # --- Only the IMMEDIATE balance is adjusted; the old deferred balance stays deferred ---
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                           | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                               | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 02 July 2026     | Capitalized Income                         | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1050.0       | false    |
      | 02 July 2026     | Capitalized Income Amortization            | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 03 July 2026     | Capitalized Income Amortization            | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 04 July 2026     | Capitalized Income                         | 30.0   | 30.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 04 July 2026     | Capitalized Income Amortization            | 30.0   | 0.0       | 30.0     | 0.0  | 0.0       | 0.0          | false    |
      | 04 July 2026     | Capitalized Income Adjustment              | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 1070.0       | false    |
      | 04 July 2026     | Capitalized Income Amortization Adjustment | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 1.1              | 48.9                | 0.0             | 0.0                |
      | 30.0   | 20.0             | 0.0                 | 10.0            | 0.0                |
    # --- Next COB: the old balance keeps amortizing daily, the IMMEDIATE one is untouched ---
    When Admin sets the business date to "05 July 2026"
    And Admin runs inline COB job for Loan
    Then Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 1.65             | 48.35               | 0.0             | 0.0                |
      | 30.0   | 20.0             | 0.0                 | 10.0            | 0.0                |
    When Loan Pay-off is made on "05 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  Scenario: Verify Immediate Capitalized Income Amortization: Strategy attribute on a product without income capitalization is ignored - UC12
    # --- Product attribute setup on a product without income capitalization ---
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE"
    # --- Loan creation and disbursement ---
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2026   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "100" EUR transaction amount
    # --- COB has no capitalized income side effects ---
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
    # --- Cleanup ---
    When Loan Pay-off is made on "02 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE"
