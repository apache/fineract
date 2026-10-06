@CapitalizedIncomeFeature
Feature: Capitalized Income - Part3

  @TestRailId:C111033
  Scenario: Verify capitalized income when amortization strategy IMMEDIATE
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data

    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"

    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 July 2026      | 1100           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "1100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "1000" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "01 July 2026" with "80" EUR transaction amount
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    And Admin adds capitalized income adjustment with "AUTOPAY" payment type to the loan on "02 July 2026" with "50" EUR transaction amount
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-03     | 1                  |
    When Admin sets the business date to "04 July 2026"
    And Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2026-07-03     | 1                  | PENDING | 2026-07-03    | 2026-07-03  | SALE             |
      | 2026-07-03     | 1                  | ACTIVE  | 2026-07-04    | 9999-12-31  | SALE             |
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                           | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                               | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Capitalized Income                         | 80.0   | 80.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 01 July 2026     | Capitalized Income Amortization            | 80.0   | 0.0       | 80.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Capitalized Income Adjustment              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1030.0       | false    |
      | 02 July 2026     | Capitalized Income Amortization Adjustment | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 80.0   | 30.0             | 0.0                 | 50.0            | 0.0                |
    When Admin sets the business date to "05 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                           | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                               | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Capitalized Income                         | 80.0   | 80.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 01 July 2026     | Capitalized Income Amortization            | 80.0   | 0.0       | 80.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Capitalized Income Adjustment              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1030.0       | false    |
      | 02 July 2026     | Capitalized Income Amortization Adjustment | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
    When Loan Pay-off is made on "05 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                           | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                               | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Capitalized Income                         | 80.0    | 80.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 01 July 2026     | Capitalized Income Amortization            | 80.0    | 0.0       | 80.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Capitalized Income Adjustment              | 50.0    | 50.0      | 0.0      | 0.0  | 0.0       | 1030.0       | false    |
      | 02 July 2026     | Capitalized Income Amortization Adjustment | 50.0    | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 05 July 2026     | Repayment                                  | 1030.78 | 1030.0    | 0.78     | 0.0  | 0.0       | 0.0          | false    |

    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"

  @TestRailId:C111034
  Scenario: Verify capitalized income when amortization strategy IMMEDIATE - backdated capitalized income case
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data

    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"

    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 July 2026      | 1100           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "1100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "01 July 2026" with "80" EUR transaction amount
    And Admin adds capitalized income adjustment with "AUTOPAY" payment type to the loan on "02 July 2026" with "50" EUR transaction amount
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-03     | 1                  |
    When Admin sets the business date to "04 July 2026"
    And Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2026-07-03     | 1                  | PENDING | 2026-07-03    | 2026-07-03  | SALE             |
      | 2026-07-03     | 1                  | ACTIVE  | 2026-07-04    | 9999-12-31  | SALE             |
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                           | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                               | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Capitalized Income                         | 80.0   | 80.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 01 July 2026     | Capitalized Income Amortization            | 80.0   | 0.0       | 80.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Capitalized Income Adjustment              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1030.0       | false    |
      | 02 July 2026     | Capitalized Income Amortization Adjustment | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 80.0   | 30.0             | 0.0                 | 50.0            | 0.0                |
    When Admin sets the business date to "05 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                           | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                               | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Capitalized Income                         | 80.0   | 80.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 01 July 2026     | Capitalized Income Amortization            | 80.0   | 0.0       | 80.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Capitalized Income Adjustment              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1030.0       | false    |
      | 02 July 2026     | Capitalized Income Amortization Adjustment | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
    When Loan Pay-off is made on "05 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                           | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                               | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Capitalized Income                         | 80.0    | 80.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 01 July 2026     | Capitalized Income Amortization            | 80.0    | 0.0       | 80.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Capitalized Income Adjustment              | 50.0    | 50.0      | 0.0      | 0.0  | 0.0       | 1030.0       | false    |
      | 02 July 2026     | Capitalized Income Amortization Adjustment | 50.0    | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 05 July 2026     | Repayment                                  | 1030.78 | 1030.0    | 0.78     | 0.0  | 0.0       | 0.0          | false    |

    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"

  @TestRailId:C111035
  Scenario: Verify immediate capitalized income amortization posts journal entries and is not clawed back by COB
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 July 2026      | 1100           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "1100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "1000" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "01 July 2026" with "80" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                    | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Capitalized Income              | 80.0   | 80.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 01 July 2026     | Capitalized Income Amortization | 80.0   | 0.0       | 80.0     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 80.0   | 80.0             | 0.0                 | 0.0             | 0.0                |
    And Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "01 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 404000       | Interest Income             |       | 80.0   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 80.0  |        |
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                    | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Capitalized Income              | 80.0   | 80.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 01 July 2026     | Capitalized Income Amortization | 80.0   | 0.0       | 80.0     | 0.0  | 0.0       | 0.0          | false    |
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"

  @TestRailId:C111036
  Scenario: Verify existing deferred capitalized income stays deferred when amortization strategy is changed to IMMEDIATE
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 July 2026      | 1100           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "1100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "1000" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "01 July 2026" with "50" EUR transaction amount
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    Then Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 1.09             | 48.91               | 0.0             | 0.0                |
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "IMMEDIATE" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "03 July 2026" with "30" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                    | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1050.0       | false    |
      | 01 July 2026     | Capitalized Income Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 03 July 2026     | Capitalized Income              | 30.0   | 30.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 03 July 2026     | Capitalized Income Amortization | 30.0   | 0.0       | 30.0     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 1.09             | 48.91               | 0.0             | 0.0                |
      | 30.0   | 30.0             | 0.0                 | 0.0             | 0.0                |
    When Admin set external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
