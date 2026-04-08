@LoanChargesInstallmentFeeChargesFeature
Feature: LoanChargesInstallmentFeeCharges

  @TestRailId:C3784
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: flat charge type, interestRecalculation = true
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_DAILY_INSTALLMENT_FEE_FLAT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 0.0  | 0.0        | 0.0  | 162.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 27.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0   | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 27.01 | 0.0        | 0.0  | 135.04      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.01  | 16.43     | 0.58     | 10.0 | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 10.0 | 0.0    | 50.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.58  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.01 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 0.0  | 0.0        | 0.0  | 162.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.01  | 16.43     | 0.58     | 10.0 | 0.0       | 83.57        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.58  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.01 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.43 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 10.58 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 27.01  |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3811
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: flat charge type, interestRecalculation = true, early repayment
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_DAILY_INSTALLMENT_FEE_FLAT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 0.0  | 0.0        | 0.0  | 162.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    When Admin sets the business date to "15 January 2024"
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 54.02 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 83.25           | 16.75         | 0.26     | 10.0 | 0.0       | 27.01 | 27.01 | 27.01      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 January 2024 | 66.24           | 17.01         | 0.0      | 10.0 | 0.0       | 27.01 | 27.01 | 27.01      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                 | 50.23           | 16.01         | 1.0      | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |                 | 33.51           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |                 | 16.7            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 16.7          | 0.1      | 10.0 | 0.0       | 26.8  | 0.0   | 0.0        | 0.0  | 26.8        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.85     | 60.0 | 0.0       | 161.85 | 54.02 | 54.02      | 0.0  | 107.83      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment        | 54.02  | 33.76     | 0.26     | 20.0 | 0.0       | 66.24        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 20.0 | 0.0    | 40.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "15 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 33.76  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 20.26  |
      | LIABILITY | 145023       | Suspense/Clearing account | 54.02 |        |
    When Customer makes a repayment undo on "15 January 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 0.0  | 0.0        | 0.0  | 162.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment        | 54.02  | 33.76     | 0.26     | 20.0 | 0.0       | 66.24        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "15 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 33.76  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 20.26  |
      | LIABILITY | 145023       | Suspense/Clearing account | 54.02 |        |
      | ASSET     | 112601       | Loans Receivable          | 33.76 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 20.26 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 54.02  |
    When Loan Pay-off is made on "15 January 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3785
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: percentage amount charge type, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_PERCENT_AMOUNT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.16 | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.17 | 0.0       | 17.21 | 0.0  | 0.0        | 0.0  | 17.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 1.01 | 0.0       | 103.05 | 0.0  | 0.0        | 0.0  | 103.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                              | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount fee | false     | Installment Fee |           | % Amount         | 1.01  | 0.0  | 0.0    | 1.01        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.16 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 0.16 | 0.0       | 17.16 | 17.16 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.54         | 0.46     | 0.17 | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
      | 3  | 31   | 01 April 2024    |                  | 50.45           | 16.6          | 0.4      | 0.17 | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.71         | 0.29     | 0.17 | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.8          | 0.2      | 0.17 | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.1      | 0.17 | 0.0       | 17.21 | 0.0   | 0.0        | 0.0  | 17.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 1.01 | 0.0       | 103.05 | 17.16 | 0.0        | 0.0  | 85.89       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.16  | 16.41     | 0.59     | 0.16 | 0.0       | 83.59        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                              | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount fee | false     | Installment Fee |           | % Amount         | 1.01  | 0.16 | 0.0    | 0.85        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.75   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.16 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.16 | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.17 | 0.0       | 17.21 | 0.0  | 0.0        | 0.0  | 17.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 1.01 | 0.0       | 103.05 | 0.0  | 0.0        | 0.0  | 103.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.16  | 16.41     | 0.59     | 0.16 | 0.0       | 83.59        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                              | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount fee | false     | Installment Fee |           | % Amount         | 1.01  | 0.0  | 0.0    | 1.01        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.75   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.16 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.41 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 0.75  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 17.16  |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3786
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: percentage interest charge type, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_PERCENT_INTEREST_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.03 | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.0  | 0.0       | 17.04 | 0.0  | 0.0        | 0.0  | 17.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 0.09 | 0.0       | 102.13 | 0.0  | 0.0        | 0.0  | 102.13      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.0  | 0.0    | 0.09        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.03 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 0.03 | 0.0       | 17.03 | 17.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.54         | 0.46     | 0.02 | 0.0       | 17.02 | 0.0   | 0.0        | 0.0  | 17.02       |
      | 3  | 31   | 01 April 2024    |                  | 50.45           | 16.6          | 0.4      | 0.02 | 0.0       | 17.02 | 0.0   | 0.0        | 0.0  | 17.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.71         | 0.29     | 0.01 | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.8          | 0.2      | 0.01 | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.1      | 0.0  | 0.0       | 17.04 | 0.0   | 0.0        | 0.0  | 17.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 0.09 | 0.0       | 102.13 | 17.03 | 0.0        | 0.0  | 85.1        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.03  | 16.41     | 0.59     | 0.03 | 0.0       | 83.59        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.03 | 0.0    | 0.06        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.62   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.03 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.03 | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.0  | 0.0       | 17.04 | 0.0  | 0.0        | 0.0  | 17.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 0.09 | 0.0       | 102.13 | 0.0  | 0.0        | 0.0  | 102.13      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.03  | 16.41     | 0.59     | 0.03 | 0.0       | 83.59        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.0  | 0.0    | 0.09        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.62   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.03 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.41 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 0.62  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 17.03  |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3812
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: percentage interest charge type, interestRecalculation = false, early repayment
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_PERCENT_INTEREST_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.03 | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.0  | 0.0       | 17.04 | 0.0  | 0.0        | 0.0  | 17.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 0.09 | 0.0       | 102.13 | 0.0  | 0.0        | 0.0  | 102.13      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.0  | 0.0    | 0.09        |
    When Admin sets the business date to "15 January 2024"
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 34.05 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 83.59           | 16.41         | 0.59     | 0.03 | 0.0       | 17.03 | 17.03 | 17.03      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 January 2024 | 67.05           | 16.54         | 0.46     | 0.02 | 0.0       | 17.02 | 17.02 | 17.02      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                 | 50.45           | 16.6          | 0.4      | 0.02 | 0.0       | 17.02 | 0.0   | 0.0        | 0.0  | 17.02       |
      | 4  | 30   | 01 May 2024      |                 | 33.74           | 16.71         | 0.29     | 0.01 | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                 | 16.94           | 16.8          | 0.2      | 0.01 | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 16.94         | 0.1      | 0.0  | 0.0       | 17.04 | 0.0   | 0.0        | 0.0  | 17.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 0.09 | 0.0       | 102.13 | 34.05 | 34.05      | 0.0  | 68.08       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment        | 34.05  | 32.95     | 1.05     | 0.05 | 0.0       | 67.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.05 | 0.0    | 0.04        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "15 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 32.95  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 1.1    |
      | LIABILITY | 145023       | Suspense/Clearing account | 34.05 |        |
    When Customer makes a repayment undo on "15 January 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.03 | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.0  | 0.0       | 17.04 | 0.0  | 0.0        | 0.0  | 17.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 0.09 | 0.0       | 102.13 | 0.0  | 0.0        | 0.0  | 102.13      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment        | 34.05  | 32.95     | 1.05     | 0.05 | 0.0       | 67.05        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.0  | 0.0    | 0.09        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "15 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 32.95  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 1.1    |
      | LIABILITY | 145023       | Suspense/Clearing account | 34.05 |        |
      | ASSET     | 112601       | Loans Receivable          | 32.95 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 1.1   |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 34.05  |
    When Loan Pay-off is made on "15 January 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3787
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: percentage amount + interest charge type, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_PERCENT_AMOUNT_INTEREST_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.17 | 0.0       | 17.21 | 0.0  | 0.0        | 0.0  | 17.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 1.02 | 0.0       | 103.06 | 0.0  | 0.0        | 0.0  | 103.06      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.0  | 0.0    | 1.02        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.17 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 0.17 | 0.0       | 17.17 | 17.17 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.54         | 0.46     | 0.17 | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
      | 3  | 31   | 01 April 2024    |                  | 50.45           | 16.6          | 0.4      | 0.17 | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.71         | 0.29     | 0.17 | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.8          | 0.2      | 0.17 | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.1      | 0.17 | 0.0       | 17.21 | 0.0   | 0.0        | 0.0  | 17.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 1.02 | 0.0       | 103.06 | 17.17 | 0.0        | 0.0  | 85.89       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.17  | 16.41     | 0.59     | 0.17 | 0.0       | 83.59        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.17 | 0.0    | 0.85        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.76   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.17 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.17 | 0.0       | 17.21 | 0.0  | 0.0        | 0.0  | 17.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 1.02 | 0.0       | 103.06 | 0.0  | 0.0        | 0.0  | 103.06      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.17  | 16.41     | 0.59     | 0.17 | 0.0       | 83.59        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.0  | 0.0    | 1.02        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.76   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.17 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.41 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 0.76  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 17.17  |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3788
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: all charge types, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_ALL_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 10.35 | 0.0       | 27.35 | 0.0  | 0.0        | 0.0  | 27.35       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 10.35 | 0.0       | 27.35 | 0.0  | 0.0        | 0.0  | 27.35       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 10.34 | 0.0       | 27.38 | 0.0  | 0.0        | 0.0  | 27.38       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 62.12 | 0.0       | 164.16 | 0.0  | 0.0        | 0.0  | 164.16      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 0.0  | 0.0    | 60.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01  | 0.0  | 0.0    | 1.01        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.0  | 0.0    | 0.09        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.0  | 0.0    | 1.02        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.36 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.36 | 0.0       | 27.36 | 27.36 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.54         | 0.46     | 10.36 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 3  | 31   | 01 April 2024    |                  | 50.45           | 16.6          | 0.4      | 10.36 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.71         | 0.29     | 10.35 | 0.0       | 27.35 | 0.0   | 0.0        | 0.0  | 27.35       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.8          | 0.2      | 10.35 | 0.0       | 27.35 | 0.0   | 0.0        | 0.0  | 27.35       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.1      | 10.34 | 0.0       | 27.38 | 0.0   | 0.0        | 0.0  | 27.38       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 62.12 | 0.0       | 164.16 | 27.36 | 0.0        | 0.0  | 136.8       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.36  | 16.41     | 0.59     | 10.36 | 0.0       | 83.59        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 10.0 | 0.0    | 50.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01  | 0.16 | 0.0    | 0.85        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.03 | 0.0    | 0.06        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.17 | 0.0    | 0.85        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.95  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.36 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 10.35 | 0.0       | 27.35 | 0.0  | 0.0        | 0.0  | 27.35       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 10.35 | 0.0       | 27.35 | 0.0  | 0.0        | 0.0  | 27.35       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 10.34 | 0.0       | 27.38 | 0.0  | 0.0        | 0.0  | 27.38       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 62.12 | 0.0       | 164.16 | 0.0  | 0.0        | 0.0  | 164.16      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.36  | 16.41     | 0.59     | 10.36 | 0.0       | 83.59        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 0.0  | 0.0    | 60.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01  | 0.0  | 0.0    | 1.01        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.0  | 0.0    | 0.09        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.0  | 0.0    | 1.02        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.95  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.36 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.41 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 10.95 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 27.36  |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3789
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: flat + % interest charge types, tranche loan, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_FLAT_INTEREST_CHARGES_TRANCHE | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 10.03 | 0.0       | 27.03 | 0.0  | 0.0        | 0.0  | 27.03       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 10.02 | 0.0       | 27.02 | 0.0  | 0.0        | 0.0  | 27.02       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 10.02 | 0.0       | 27.02 | 0.0  | 0.0        | 0.0  | 27.02       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 10.01 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 10.01 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 10.0  | 0.0       | 27.04 | 0.0  | 0.0        | 0.0  | 27.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 60.09 | 0.0       | 162.13 | 0.0  | 0.0        | 0.0  | 162.13      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.0  | 0.0    | 0.09        |
      | Installment flat fee                | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.03 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 27.02 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.03 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.54         | 0.46     | 10.02 | 0.0       | 27.02 | 27.02 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.45           | 16.6          | 0.4      | 10.02 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.71         | 0.29     | 10.01 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.8          | 0.2      | 10.01 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.1      | 10.0  | 0.0       | 27.04 | 0.0   | 0.0        | 0.0  | 27.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 60.09 | 0.0       | 162.13 | 54.05 | 0.0        | 0.0  | 108.08      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.03  | 16.41     | 0.59     | 10.03 | 0.0       | 83.59        | false    | false    |
      | 01 March 2024    | Repayment        | 27.02  | 16.54     | 0.46     | 10.02 | 0.0       | 67.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 20.0 | 0.0    | 40.0        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.05 | 0.0    | 0.04        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.62  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.03 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.54  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.48  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.02 |        |
    When Admin sets the business date to "03 March 2024"
    When Admin successfully disburse the loan on "03 March 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.03 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.54         | 0.46     | 10.02 | 0.0       | 27.02 | 27.02 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 126.0           | 41.05         | 0.95     | 10.05 | 0.0       | 52.05 | 0.0   | 0.0        | 0.0  | 52.05       |
      | 4  | 30   | 01 May 2024      |                  | 84.72           | 41.28         | 0.72     | 10.04 | 0.0       | 52.04 | 0.0   | 0.0        | 0.0  | 52.04       |
      | 5  | 31   | 01 June 2024     |                  | 43.22           | 41.5          | 0.5      | 10.02 | 0.0       | 52.02 | 0.0   | 0.0        | 0.0  | 52.02       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 43.22         | 0.25     | 10.01 | 0.0       | 53.48 | 0.0   | 0.0        | 0.0  | 53.48       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.47     | 60.17 | 0.0       | 263.64 | 54.05 | 0.0        | 0.0  | 209.59      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.03  | 16.41     | 0.59     | 10.03 | 0.0       | 83.59        | false    | false    |
      | 01 March 2024    | Repayment        | 27.02  | 16.54     | 0.46     | 10.02 | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 167.05       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 20.0 | 0.0    | 40.0        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.17  | 0.05 | 0.0    | 0.12        |
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.03 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.54         | 0.46     | 10.02 | 0.0       | 27.02 | 27.02 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.45           | 16.6          | 0.4      | 10.02 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.71         | 0.29     | 10.01 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.8          | 0.2      | 10.01 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.1      | 10.0  | 0.0       | 27.04 | 0.0   | 0.0        | 0.0  | 27.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 60.09 | 0.0       | 162.13 | 54.05 | 0.0        | 0.0  | 108.08      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.03  | 16.41     | 0.59     | 10.03 | 0.0       | 83.59        | false    | false    |
      | 01 March 2024    | Repayment        | 27.02  | 16.54     | 0.46     | 10.02 | 0.0       | 67.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 20.0 | 0.0    | 40.0        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.05 | 0.0    | 0.04        |
    Then Admin can successfully undone the loan disbursal
    Then Admin can successfully undone the loan approval
    Then Loan status will be "SUBMITTED_AND_PENDING_APPROVAL"

  @TestRailId:C3813
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: flat + % interest charge types, tranche loan, interestRecalculation = false, early repayment
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_FLAT_INTEREST_CHARGES_TRANCHE | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 10.03 | 0.0       | 27.03 | 0.0  | 0.0        | 0.0  | 27.03       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 10.02 | 0.0       | 27.02 | 0.0  | 0.0        | 0.0  | 27.02       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 10.02 | 0.0       | 27.02 | 0.0  | 0.0        | 0.0  | 27.02       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 10.01 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 10.01 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 10.0  | 0.0       | 27.04 | 0.0  | 0.0        | 0.0  | 27.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 60.09 | 0.0       | 162.13 | 0.0  | 0.0        | 0.0  | 162.13      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.0  | 0.0    | 0.09        |
      | Installment flat fee                | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.03 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 27.02 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.03 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.54         | 0.46     | 10.02 | 0.0       | 27.02 | 27.02 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.45           | 16.6          | 0.4      | 10.02 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.71         | 0.29     | 10.01 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.8          | 0.2      | 10.01 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.1      | 10.0  | 0.0       | 27.04 | 0.0   | 0.0        | 0.0  | 27.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 60.09 | 0.0       | 162.13 | 54.05 | 0.0        | 0.0  | 108.08      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.03  | 16.41     | 0.59     | 10.03 | 0.0       | 83.59        | false    | false    |
      | 01 March 2024    | Repayment        | 27.02  | 16.54     | 0.46     | 10.02 | 0.0       | 67.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 20.0 | 0.0    | 40.0        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.05 | 0.0    | 0.04        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.62  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.03 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.54  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.48  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.02 |        |
    When Admin sets the business date to "03 March 2024"
    When Admin successfully disburse the loan on "03 March 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.03 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.54         | 0.46     | 10.02 | 0.0       | 27.02 | 27.02 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 126.0           | 41.05         | 0.95     | 10.05 | 0.0       | 52.05 | 0.0   | 0.0        | 0.0  | 52.05       |
      | 4  | 30   | 01 May 2024      |                  | 84.72           | 41.28         | 0.72     | 10.04 | 0.0       | 52.04 | 0.0   | 0.0        | 0.0  | 52.04       |
      | 5  | 31   | 01 June 2024     |                  | 43.22           | 41.5          | 0.5      | 10.02 | 0.0       | 52.02 | 0.0   | 0.0        | 0.0  | 52.02       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 43.22         | 0.25     | 10.01 | 0.0       | 53.48 | 0.0   | 0.0        | 0.0  | 53.48       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.47     | 60.17 | 0.0       | 263.64 | 54.05 | 0.0        | 0.0  | 209.59      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.03  | 16.41     | 0.59     | 10.03 | 0.0       | 83.59        | false    | false    |
      | 01 March 2024    | Repayment        | 27.02  | 16.54     | 0.46     | 10.02 | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 167.05       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 20.0 | 0.0    | 40.0        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.17  | 0.05 | 0.0    | 0.12        |
    And Customer makes "AUTOPAY" repayment on "03 March 2024" with 104.09 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.03 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.54         | 0.46     | 10.02 | 0.0       | 27.02 | 27.02 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 3  | 31   | 01 April 2024    | 03 March 2024    | 126.0           | 41.05         | 0.95     | 10.05 | 0.0       | 52.05 | 52.05 | 52.05      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 03 March 2024    | 84.72           | 41.28         | 0.72     | 10.04 | 0.0       | 52.04 | 52.04 | 52.04      | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     |                  | 43.22           | 41.5          | 0.5      | 10.02 | 0.0       | 52.02 | 0.0   | 0.0        | 0.0  | 52.02       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 43.22         | 0.25     | 10.01 | 0.0       | 53.48 | 0.0   | 0.0        | 0.0  | 53.48       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 200.0         | 3.47     | 60.17 | 0.0       | 263.64 | 158.14 | 104.09     | 0.0  | 105.5       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.03  | 16.41     | 0.59     | 10.03 | 0.0       | 83.59        | false    | false    |
      | 01 March 2024    | Repayment        | 27.02  | 16.54     | 0.46     | 10.02 | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Repayment        | 104.09 | 82.33     | 1.67     | 20.09 | 0.0       | 84.72        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 40.0 | 0.0    | 20.0        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.17  | 0.14 | 0.0    | 0.03        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 82.33  |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 21.76  |
      | LIABILITY | 145023       | Suspense/Clearing account | 104.09 |        |
    When Loan Pay-off is made on "03 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3814
  Scenario: Progressive loan - Verify add installment fee charge: flat + % interest charge types, tranche loan, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_MULTIDISBURSE | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST" installment charge with 5 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.03 | 0.0       | 27.04 | 0.0  | 0.0        | 0.0  | 27.04       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.02 | 0.0       | 27.03 | 0.0  | 0.0        | 0.0  | 27.03       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.02 | 0.0       | 27.03 | 0.0  | 0.0        | 0.0  | 27.03       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.01 | 0.0       | 27.02 | 0.0  | 0.0        | 0.0  | 27.02       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.01 | 0.0       | 27.02 | 0.0  | 0.0        | 0.0  | 27.02       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0  | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.09 | 0.0       | 162.14 | 0.0  | 0.0        | 0.0  | 162.14      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 0.0  | 0.0    | 60.0        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.0  | 0.0    | 0.09        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.04 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 27.03 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.03 | 0.0       | 27.04 | 27.04 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 10.02 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 10.02 | 0.0       | 27.03 | 0.0   | 0.0        | 0.0  | 27.03       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 10.01 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 10.01 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 10.0  | 0.0       | 27.0  | 0.0   | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.09 | 0.0       | 162.14 | 54.07 | 0.0        | 0.0  | 108.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.04  | 16.43     | 0.58     | 10.03 | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 27.03  | 16.52     | 0.49     | 10.02 | 0.0       | 67.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 20.0 | 0.0    | 40.0        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.05 | 0.0    | 0.04        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.61  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.04 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.52  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.51  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.03 |        |
    When Admin sets the business date to "03 March 2024"
    When Admin successfully disburse the loan on "03 March 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.03 | 0.0       | 27.04 | 27.04 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 10.02 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 125.62          | 41.43         | 0.94     | 10.05 | 0.0       | 52.42 | 0.0   | 0.0        | 0.0  | 52.42       |
      | 4  | 30   | 01 May 2024      |                  | 83.98           | 41.64         | 0.73     | 10.04 | 0.0       | 52.41 | 0.0   | 0.0        | 0.0  | 52.41       |
      | 5  | 31   | 01 June 2024     |                  | 42.1            | 41.88         | 0.49     | 10.02 | 0.0       | 52.39 | 0.0   | 0.0        | 0.0  | 52.39       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 42.1          | 0.25     | 10.01 | 0.0       | 52.36 | 0.0   | 0.0        | 0.0  | 52.36       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.48     | 60.17 | 0.0       | 263.65 | 54.07 | 0.0        | 0.0  | 209.58      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.04  | 16.43     | 0.58     | 10.03 | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 27.03  | 16.52     | 0.49     | 10.02 | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 167.05       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 20.0 | 0.0    | 40.0        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.17  | 0.05 | 0.0    | 0.12        |
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.03 | 0.0       | 27.04 | 27.04 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 10.02 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 10.02 | 0.0       | 27.03 | 0.0   | 0.0        | 0.0  | 27.03       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 10.01 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 10.01 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 10.0  | 0.0       | 27.0  | 0.0   | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.09 | 0.0       | 162.14 | 54.07 | 0.0        | 0.0  | 108.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.04  | 16.43     | 0.58     | 10.03 | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 27.03  | 16.52     | 0.49     | 10.02 | 0.0       | 67.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 20.0 | 0.0    | 40.0        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.05 | 0.0    | 0.04        |
    When Loan Pay-off is made on "03 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3820
  Scenario: Progressive loan - Verify add installment fee charge: flat charge type, tranche loan, interestRecalculation = true, early repayment
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_INTEREST_RECALCULATION_MULTIDISB | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.58           | 16.42         | 0.59     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.03           | 16.55         | 0.46     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.42           | 16.61         | 0.4      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.7            | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.89           | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.89         | 0.1      | 10.0 | 0.0       | 26.99 | 0.0  | 0.0        | 0.0  | 26.99       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 60.0 | 0.0       | 162.04 | 0.0  | 0.0        | 0.0  | 162.04      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 0.0  | 0.0    | 60.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 27.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.59     | 10.0 | 0.0       | 27.01 | 27.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.03           | 16.55         | 0.46     | 10.0 | 0.0       | 27.01 | 27.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.42           | 16.61         | 0.4      | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.7            | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.89           | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.89         | 0.1      | 10.0 | 0.0       | 26.99 | 0.0   | 0.0        | 0.0  | 26.99       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 60.0 | 0.0       | 162.04 | 54.02 | 0.0        | 0.0  | 108.02      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.01  | 16.42     | 0.59     | 10.0 | 0.0       | 83.58        | false    | false    |
      | 01 March 2024    | Repayment        | 27.01  | 16.55     | 0.46     | 10.0 | 0.0       | 67.03        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 20.0 | 0.0    | 40.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.42  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.59  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.01 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.55  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.46  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.01 |        |
    When Admin sets the business date to "03 March 2024"
    When Admin successfully disburse the loan on "03 March 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.59     | 10.0 | 0.0       | 27.01 | 27.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.03           | 16.55         | 0.46     | 10.0 | 0.0       | 27.01 | 27.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 125.62          | 41.41         | 0.95     | 10.0 | 0.0       | 52.36 | 0.0   | 0.0        | 0.0  | 52.36       |
      | 4  | 30   | 01 May 2024      |                  | 83.98           | 41.64         | 0.72     | 10.0 | 0.0       | 52.36 | 0.0   | 0.0        | 0.0  | 52.36       |
      | 5  | 31   | 01 June 2024     |                  | 42.12           | 41.86         | 0.5      | 10.0 | 0.0       | 52.36 | 0.0   | 0.0        | 0.0  | 52.36       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 42.12         | 0.24     | 10.0 | 0.0       | 52.36 | 0.0   | 0.0        | 0.0  | 52.36       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.46     | 60.0 | 0.0       | 263.46 | 54.02 | 0.0        | 0.0  | 209.44      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.01  | 16.42     | 0.59     | 10.0 | 0.0       | 83.58        | false    | false    |
      | 01 March 2024    | Repayment        | 27.01  | 16.55     | 0.46     | 10.0 | 0.0       | 67.03        | false    | false    |
      | 03 March 2024    | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.03       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 20.0 | 0.0    | 40.0        |
    And Customer makes "AUTOPAY" repayment on "03 March 2024" with 104.72 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.59     | 10.0 | 0.0       | 27.01 | 27.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.03           | 16.55         | 0.46     | 10.0 | 0.0       | 27.01 | 27.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 3  | 31   | 01 April 2024    | 03 March 2024    | 124.7           | 42.33         | 0.03     | 10.0 | 0.0       | 52.36 | 52.36 | 52.36      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 03 March 2024    | 82.34           | 42.36         | 0.0      | 10.0 | 0.0       | 52.36 | 52.36 | 52.36      | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     |                  | 41.39           | 40.95         | 1.41     | 10.0 | 0.0       | 52.36 | 0.0   | 0.0        | 0.0  | 52.36       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 41.39         | 0.24     | 10.0 | 0.0       | 51.63 | 0.0   | 0.0        | 0.0  | 51.63       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 200.0         | 2.73     | 60.0 | 0.0       | 262.73 | 158.74 | 104.72     | 0.0  | 103.99      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.01  | 16.42     | 0.59     | 10.0 | 0.0       | 83.58        | false    | false    |
      | 01 March 2024    | Repayment        | 27.01  | 16.55     | 0.46     | 10.0 | 0.0       | 67.03        | false    | false    |
      | 03 March 2024    | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.03       | false    | false    |
      | 03 March 2024    | Repayment        | 104.72 | 84.69     | 0.03     | 20.0 | 0.0       | 82.34        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 40.0 | 0.0    | 20.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 84.69  |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 20.03  |
      | LIABILITY | 145023       | Suspense/Clearing account | 104.72 |        |
    When Loan Pay-off is made on "03 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3790
  Scenario: Progressive loan - Verify add installment fee charge: flat charge type, interestRecalculation = true
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 0.0  | 0.0        | 0.0  | 162.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 27.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0   | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 27.01 | 0.0        | 0.0  | 135.04      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.01  | 16.43     | 0.58     | 10.0 | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 10.0 | 0.0    | 50.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.58  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.01 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 0.0  | 0.0        | 0.0  | 162.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.01  | 16.43     | 0.58     | 10.0 | 0.0       | 83.57        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.58  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.01 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.43 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 10.58 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 27.01  |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3815
  Scenario: Progressive loan - Verify add installment fee charge: flat charge type, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 0.0  | 0.0        | 0.0  | 162.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 27.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0   | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 27.01 | 0.0        | 0.0  | 135.04      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.01  | 16.43     | 0.58     | 10.0 | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 10.0 | 0.0    | 50.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.58  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.01 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 0.0  | 0.0        | 0.0  | 162.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.01  | 16.43     | 0.58     | 10.0 | 0.0       | 83.57        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.58  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.01 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.43 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 10.58 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 27.01  |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3816
  Scenario: Progressive loan - Verify add installment fee charge: percentage amount charge type is NOT allowed when interestRecalculation = true
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin fails to add "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT" installment charge with 1 amount because of wrong charge calculation type
    When Loan Pay-off is made on "01 January 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3791
  Scenario: Progressive loan - Verify add installment fee charge: percentage amount charge type, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT" installment charge with 1 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.16 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.01 | 0.0       | 103.06 | 0.0  | 0.0        | 0.0  | 103.06      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                              | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount fee | false     | Installment Fee |           | % Amount         | 1.01  | 0.0  | 0.0    | 1.01        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.17 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.16 | 0.0       | 17.17 | 17.17 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.17 | 0.0       | 17.18 | 0.0   | 0.0        | 0.0  | 17.18       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.17 | 0.0       | 17.18 | 0.0   | 0.0        | 0.0  | 17.18       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.17 | 0.0       | 17.18 | 0.0   | 0.0        | 0.0  | 17.18       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.17 | 0.0       | 17.18 | 0.0   | 0.0        | 0.0  | 17.18       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.01 | 0.0       | 103.06 | 17.17 | 0.0        | 0.0  | 85.89       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.17  | 16.43     | 0.58     | 0.16 | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                              | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount fee | false     | Installment Fee |           | % Amount         | 1.01  | 0.16 | 0.0    | 0.85        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.74   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.17 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.16 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.01 | 0.0       | 103.06 | 0.0  | 0.0        | 0.0  | 103.06      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.17  | 16.43     | 0.58     | 0.16 | 0.0       | 83.57        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                              | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount fee | false     | Installment Fee |           | % Amount         | 1.01  | 0.0  | 0.0    | 1.01        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.74   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.17 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.43 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 0.74  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 17.17  |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3792
  Scenario: Progressive loan - Verify add installment fee charge: percentage interest charge type is NOT allowed when interestRecalculation = true
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin fails to add "LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST" installment charge with 5 amount because of wrong charge calculation type
    When Loan Pay-off is made on "01 January 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3817
  Scenario: Progressive loan - Verify add installment fee charge: percentage interest charge type, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST" installment charge with 5 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.03 | 0.0       | 17.04 | 0.0  | 0.0        | 0.0  | 17.04       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.02 | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.02 | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.01 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.01 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.09 | 0.0       | 102.14 | 0.0  | 0.0        | 0.0  | 102.14      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.0  | 0.0    | 0.09        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.04 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.03 | 0.0       | 17.04 | 17.04 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.02 | 0.0       | 17.03 | 0.0   | 0.0        | 0.0  | 17.03       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.02 | 0.0       | 17.03 | 0.0   | 0.0        | 0.0  | 17.03       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.01 | 0.0       | 17.02 | 0.0   | 0.0        | 0.0  | 17.02       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.01 | 0.0       | 17.02 | 0.0   | 0.0        | 0.0  | 17.02       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.09 | 0.0       | 102.14 | 17.04 | 0.0        | 0.0  | 85.1        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.04  | 16.43     | 0.58     | 0.03 | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.03 | 0.0    | 0.06        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.61   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.04 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.03 | 0.0       | 17.04 | 0.0  | 0.0        | 0.0  | 17.04       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.02 | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.02 | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.01 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.01 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.09 | 0.0       | 102.14 | 0.0  | 0.0        | 0.0  | 102.14      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.04  | 16.43     | 0.58     | 0.03 | 0.0       | 83.57        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.0  | 0.0    | 0.09        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.61   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.04 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.43 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 0.61  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 17.04  |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3818
  Scenario: Progressive loan - Verify add installment fee charge: percentage amount + interest charge type is NOT allowed when interestRecalculation = true
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin fails to add "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST" installment charge with 1 amount because of wrong charge calculation type
    When Loan Pay-off is made on "01 January 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3793
  Scenario: Progressive loan - Verify add installment fee charge: percentage amount + interest charge type, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST" installment charge with 1 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.02 | 0.0       | 103.07 | 0.0  | 0.0        | 0.0  | 103.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.0  | 0.0    | 1.02        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.18 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.17 | 0.0       | 17.18 | 17.18 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.17 | 0.0       | 17.18 | 0.0   | 0.0        | 0.0  | 17.18       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.17 | 0.0       | 17.18 | 0.0   | 0.0        | 0.0  | 17.18       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.17 | 0.0       | 17.18 | 0.0   | 0.0        | 0.0  | 17.18       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.17 | 0.0       | 17.18 | 0.0   | 0.0        | 0.0  | 17.18       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.02 | 0.0       | 103.07 | 17.18 | 0.0        | 0.0  | 85.89       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.18  | 16.43     | 0.58     | 0.17 | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.17 | 0.0    | 0.85        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.75   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.18 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.02 | 0.0       | 103.07 | 0.0  | 0.0        | 0.0  | 103.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.18  | 16.43     | 0.58     | 0.17 | 0.0       | 83.57        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.0  | 0.0    | 1.02        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.75   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.18 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.43 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 0.75  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 17.18  |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3794
  Scenario: Progressive loan - Verify add installment fee charge: all charge types, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT" installment charge with 1 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST" installment charge with 5 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST" installment charge with 1 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.35 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.35 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.34 | 0.0       | 27.34 | 0.0  | 0.0        | 0.0  | 27.34       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 62.12 | 0.0       | 164.17 | 0.0  | 0.0        | 0.0  | 164.17      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 0.0  | 0.0    | 60.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01  | 0.0  | 0.0    | 1.01        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.0  | 0.0    | 0.09        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.0  | 0.0    | 1.02        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.37 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.36 | 0.0       | 27.37 | 27.37 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 10.36 | 0.0       | 27.37 | 0.0   | 0.0        | 0.0  | 27.37       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 10.36 | 0.0       | 27.37 | 0.0   | 0.0        | 0.0  | 27.37       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 10.35 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 10.35 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 10.34 | 0.0       | 27.34 | 0.0   | 0.0        | 0.0  | 27.34       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 62.12 | 0.0       | 164.17 | 27.37 | 0.0        | 0.0  | 136.8       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.37  | 16.43     | 0.58     | 10.36 | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 10.0 | 0.0    | 50.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01  | 0.16 | 0.0    | 0.85        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.03 | 0.0    | 0.06        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.17 | 0.0    | 0.85        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.94  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.37 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.35 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.35 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.34 | 0.0       | 27.34 | 0.0  | 0.0        | 0.0  | 27.34       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 62.12 | 0.0       | 164.17 | 0.0  | 0.0        | 0.0  | 164.17      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.37  | 16.43     | 0.58     | 10.36 | 0.0       | 83.57        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 0.0  | 0.0    | 60.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01  | 0.0  | 0.0    | 1.01        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.0  | 0.0    | 0.09        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.0  | 0.0    | 1.02        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.94  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.37 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.43 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 10.94 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 27.37  |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3795
  Scenario: Progressive loan - Verify add installment fee charge, then make zero-interest charge-off: all charge types, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT" installment charge with 1 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST" installment charge with 5 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST" installment charge with 1 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 10.35 | 0.0       | 27.35 | 0.0  | 0.0        | 0.0  | 27.35       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 10.35 | 0.0       | 27.35 | 0.0  | 0.0        | 0.0  | 27.35       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 10.34 | 0.0       | 27.38 | 0.0  | 0.0        | 0.0  | 27.38       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 62.12 | 0.0       | 164.16 | 0.0  | 0.0        | 0.0  | 164.16      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 0.0  | 0.0    | 60.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01  | 0.0  | 0.0    | 1.01        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.0  | 0.0    | 0.09        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.0  | 0.0    | 1.02        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.36 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.36 | 0.0       | 27.36 | 27.36 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.54         | 0.46     | 10.36 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 3  | 31   | 01 April 2024    |                  | 50.45           | 16.6          | 0.4      | 10.36 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.71         | 0.29     | 10.35 | 0.0       | 27.35 | 0.0   | 0.0        | 0.0  | 27.35       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.8          | 0.2      | 10.35 | 0.0       | 27.35 | 0.0   | 0.0        | 0.0  | 27.35       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.1      | 10.34 | 0.0       | 27.38 | 0.0   | 0.0        | 0.0  | 27.38       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 62.12 | 0.0       | 164.16 | 27.36 | 0.0        | 0.0  | 136.8       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.36  | 16.41     | 0.59     | 10.36 | 0.0       | 83.59        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 10.0 | 0.0    | 50.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01  | 0.16 | 0.0    | 0.85        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.03 | 0.0    | 0.06        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.17 | 0.0    | 0.85        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.95  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.36 |        |
    When Admin sets the business date to "1 March 2024"
    And Admin does charge-off the loan on "1 March 2024"
    Then LoanBalanceChangedBusinessEvent is created on "01 March 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.36 | 0.0       | 27.36 | 27.36 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.54         | 0.46     | 10.36 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 3  | 31   | 01 April 2024    |                  | 50.05           | 17.0          | 0.0      | 10.34 | 0.0       | 27.34 | 0.0   | 0.0        | 0.0  | 27.34       |
      | 4  | 30   | 01 May 2024      |                  | 33.05           | 17.0          | 0.0      | 10.34 | 0.0       | 27.34 | 0.0   | 0.0        | 0.0  | 27.34       |
      | 5  | 31   | 01 June 2024     |                  | 16.05           | 17.0          | 0.0      | 10.34 | 0.0       | 27.34 | 0.0   | 0.0        | 0.0  | 27.34       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.05         | 0.0      | 10.32 | 0.0       | 26.37 | 0.0   | 0.0        | 0.0  | 26.37       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 1.05     | 62.06 | 0         | 163.11 | 27.36 | 0          | 0    | 135.75      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.36  | 16.41     | 0.59     | 10.36 | 0.0       | 83.59        | false    | false    |
      | 01 March 2024    | Accrual          | 1.05   | 0.0       | 1.05     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off       | 135.75 | 83.59     | 0.46     | 51.7  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "01 March 2024" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 83.59  |
      | ASSET   | 112603       | Interest/Fee Receivable    |       | 52.16  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 83.59 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 0.46  |        |
      | INCOME  | 404008       | Fee Charge Off             | 51.7  |        |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3796
  Scenario: Progressive loan - Verify add installment fee charge, then make accelerate maturity date charge-off: all charge types, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT" installment charge with 1 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST" installment charge with 5 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST" installment charge with 1 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 10.35 | 0.0       | 27.35 | 0.0  | 0.0        | 0.0  | 27.35       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 10.35 | 0.0       | 27.35 | 0.0  | 0.0        | 0.0  | 27.35       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 10.34 | 0.0       | 27.38 | 0.0  | 0.0        | 0.0  | 27.38       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 62.12 | 0.0       | 164.16 | 0.0  | 0.0        | 0.0  | 164.16      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 0.0  | 0.0    | 60.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01  | 0.0  | 0.0    | 1.01        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.0  | 0.0    | 0.09        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.0  | 0.0    | 1.02        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.36 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.36 | 0.0       | 27.36 | 27.36 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.54         | 0.46     | 10.36 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 3  | 31   | 01 April 2024    |                  | 50.45           | 16.6          | 0.4      | 10.36 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.71         | 0.29     | 10.35 | 0.0       | 27.35 | 0.0   | 0.0        | 0.0  | 27.35       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.8          | 0.2      | 10.35 | 0.0       | 27.35 | 0.0   | 0.0        | 0.0  | 27.35       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.1      | 10.34 | 0.0       | 27.38 | 0.0   | 0.0        | 0.0  | 27.38       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 62.12 | 0.0       | 164.16 | 27.36 | 0.0        | 0.0  | 136.8       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.36  | 16.41     | 0.59     | 10.36 | 0.0       | 83.59        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 10.0 | 0.0    | 50.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01  | 0.16 | 0.0    | 0.85        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.03 | 0.0    | 0.06        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.17 | 0.0    | 0.85        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.95  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.36 |        |
    When Admin sets the business date to "1 March 2024"
    And Admin does charge-off the loan on "1 March 2024"
    Then LoanBalanceChangedBusinessEvent is created on "01 March 2024"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.36 | 0.0       | 27.36 | 27.36 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 0.0             | 83.59         | 0.46     | 11.7  | 0.0       | 95.75 | 0.0   | 0.0        | 0.0  | 95.75       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 1.05     | 22.06 | 0         | 123.11 | 27.36 | 0          | 0    | 95.75       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.36  | 16.41     | 0.59     | 10.36 | 0.0       | 83.59        | false    | false    |
      | 01 March 2024    | Accrual          | 1.05   | 0.0       | 1.05     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off       | 95.75  | 83.59     | 0.46     | 11.7  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "01 March 2024" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 83.59  |
      | ASSET   | 112603       | Interest/Fee Receivable    |       | 12.16  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 83.59 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 0.46  |        |
      | INCOME  | 404008       | Fee Charge Off             | 11.7  |        |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3797
  Scenario: Verify that partially waived installment fee applied correctly in reverse-replay logic, Progressive loan
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Progressive Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST" charge with 10 % of transaction amount
    When Admin sets the business date to "15 January 2023"
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 5 EUR transaction amount
    When Admin sets the business date to "20 January 2023"
    And Admin waives due date charge
    And Customer makes "AUTOPAY" repayment on "18 January 2023" with 15 EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0   |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 100.0 | 0.0       | 1100.0 | 20.0 | 20.0       | 0.0  | 980.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 100.0 | 0         | 1100.0 | 20   | 20         | 0    | 980.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement       | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 January 2023  | Repayment          | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 995.0        |
      | 18 January 2023  | Repayment          | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 980.0        |
      | 20 January 2023  | Waive loan charges | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 980.0        |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 100.0 | 0.0  | 100.0  | 0.0         |
    When Loan Pay-off is made on "20 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3823
  Scenario: Progressive loan - Verify non-tranche loan with all installment fee charge types and repayments
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT" installment charge with 1 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST" installment charge with 5 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST" installment charge with 1 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.35 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.35 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.34 | 0.0       | 27.34 | 0.0  | 0.0        | 0.0  | 27.34       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 62.12 | 0.0       | 164.17 | 0.0  | 0.0        | 0.0  | 164.17      |
    Then Loan Transactions tab has none transaction
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.35 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.35 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.34 | 0.0       | 27.34 | 0.0  | 0.0        | 0.0  | 27.34       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 62.12 | 0.0       | 164.17 | 0.0  | 0.0        | 0.0  | 164.17      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0 | 0.0  | 0.0    | 60.0        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02 | 0.0  | 0.0    | 1.02        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09 | 0.0  | 0.0    | 0.09        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01 | 0.0  | 0.0    | 1.01        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.37 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.36 | 0.0       | 27.37 | 27.37 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 10.36 | 0.0       | 27.37 | 0.0   | 0.0        | 0.0  | 27.37       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 10.36 | 0.0       | 27.37 | 0.0   | 0.0        | 0.0  | 27.37       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 10.35 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 10.35 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 10.34 | 0.0       | 27.34 | 0.0   | 0.0        | 0.0  | 27.34       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 62.12 | 0.0       | 164.17 | 27.37 | 0.0        | 0.0  | 136.8       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.37  | 16.43     | 0.58     | 10.36 | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02 | 0.17 | 0.0    | 0.85        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09 | 0.03 | 0.0    | 0.06        |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0 | 10.0 | 0.0    | 50.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01 | 0.16 | 0.0    | 0.85        |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3824
  Scenario: Progressive loan - Verify tranche loan with installment fee charges, repayments and multiple disbursements
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_MULTIDISBURSE | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST" installment charge with 5 amount
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.03 | 0.0       | 27.04 | 0.0  | 0.0        | 0.0  | 27.04       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.02 | 0.0       | 27.03 | 0.0  | 0.0        | 0.0  | 27.03       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.02 | 0.0       | 27.03 | 0.0  | 0.0        | 0.0  | 27.03       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.01 | 0.0       | 27.02 | 0.0  | 0.0        | 0.0  | 27.02       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.01 | 0.0       | 27.02 | 0.0  | 0.0        | 0.0  | 27.02       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0  | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.09 | 0.0       | 162.14 | 0.0  | 0.0        | 0.0  | 162.14      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09 | 0.0  | 0.0    | 0.09        |
      | Installment flat fee                | false     | Installment Fee |           | Flat             | 60.0 | 0.0  | 0.0    | 60.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.04 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.03 | 0.0       | 27.04 | 27.04 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 10.02 | 0.0       | 27.03 | 0.0   | 0.0        | 0.0  | 27.03       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 10.02 | 0.0       | 27.03 | 0.0   | 0.0        | 0.0  | 27.03       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 10.01 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 10.01 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 10.0  | 0.0       | 27.0  | 0.0   | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.09 | 0.0       | 162.14 | 27.04 | 0.0        | 0.0  | 135.1       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.04  | 16.43     | 0.58     | 10.03 | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09 | 0.03 | 0.0    | 0.06        |
      | Installment flat fee                | false     | Installment Fee |           | Flat             | 60.0 | 10.0 | 0.0    | 50.0        |
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 27.03 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.03 | 0.0       | 27.04 | 27.04 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 10.02 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 10.02 | 0.0       | 27.03 | 0.0   | 0.0        | 0.0  | 27.03       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 10.01 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 10.01 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 10.0  | 0.0       | 27.0  | 0.0   | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.09 | 0.0       | 162.14 | 54.07 | 0.0        | 0.0  | 108.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.04  | 16.43     | 0.58     | 10.03 | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 27.03  | 16.52     | 0.49     | 10.02 | 0.0       | 67.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09 | 0.05 | 0.0    | 0.04        |
      | Installment flat fee                | false     | Installment Fee |           | Flat             | 60.0 | 20.0 | 0.0    | 40.0        |
    When Admin sets the business date to "03 March 2024"
    When Admin successfully disburse the loan on "03 March 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.03 | 0.0       | 27.04 | 27.04 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 10.02 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 125.62          | 41.43         | 0.94     | 10.05 | 0.0       | 52.42 | 0.0   | 0.0        | 0.0  | 52.42       |
      | 4  | 30   | 01 May 2024      |                  | 83.98           | 41.64         | 0.73     | 10.04 | 0.0       | 52.41 | 0.0   | 0.0        | 0.0  | 52.41       |
      | 5  | 31   | 01 June 2024     |                  | 42.1            | 41.88         | 0.49     | 10.02 | 0.0       | 52.39 | 0.0   | 0.0        | 0.0  | 52.39       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 42.1          | 0.25     | 10.01 | 0.0       | 52.36 | 0.0   | 0.0        | 0.0  | 52.36       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.48     | 60.17 | 0.0       | 263.65 | 54.07 | 0.0        | 0.0  | 209.58      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.04  | 16.43     | 0.58     | 10.03 | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 27.03  | 16.52     | 0.49     | 10.02 | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 167.05       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Installment flat fee                | false     | Installment Fee |           | Flat             | 60.0 | 20.0 | 0.0    | 40.0        |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.17 | 0.05 | 0.0    | 0.12        |
    When Loan Pay-off is made on "03 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3890
  Scenario: Cumulative loan - Verify final income accrual with multiple fee charges created successfully
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 October 2023   | 100            | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 April 2024" due date and 50 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 April 2024" due date and 35 EUR transaction amount
    When Admin adds "LOAN_FIXED_RETURNED_PAYMENT_FEE" due date charge with "06 April 2024" due date and 10 EUR transaction amount
    When Admin adds "LOAN_FIXED_RETURNED_PAYMENT_FEE" due date charge with "10 April 2024" due date and 5 EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0   | 0.0       | 25.0  | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 63.0            | 12.0          | 0.0      | 10.0  | 0.0       | 22.0  | 0.0  | 0.0        | 0.0  | 22.0        |
      | 3  | 29   | 01 March 2024    |           | 51.0            | 12.0          | 0.0      | 10.0  | 0.0       | 22.0  | 0.0  | 0.0        | 0.0  | 22.0        |
      | 4  | 31   | 01 April 2024    |           | 39.0            | 12.0          | 0.0      | 10.0  | 0.0       | 22.0  | 0.0  | 0.0        | 0.0  | 22.0        |
      | 5  | 30   | 01 May 2024      |           | 27.0            | 12.0          | 0.0      | 110.0 | 0.0       | 122.0 | 0.0  | 0.0        | 0.0  | 122.0       |
      | 6  | 31   | 01 June 2024     |           | 15.0            | 12.0          | 0.0      | 10.0  | 0.0       | 22.0  | 0.0  | 0.0        | 0.0  | 22.0        |
      | 7  | 30   | 01 July 2024     |           | 0.0             | 15.0          | 0.0      | 10.0  | 0.0       | 25.0  | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 160.0 | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                       | isPenalty | Payment due at     | Due as of     | Calculation type | Due  | Paid | Waived | Outstanding |
      | Installment flat fee       | false     | Installment Fee    |               | Flat             | 60.0 | 0.0  | 0.0    | 60.0        |
      | Snooze fee                 | false     | Specified due date | 06 April 2024 | Flat             | 50.0 | 0.0  | 0.0    | 50.0        |
      | Snooze fee                 | false     | Specified due date | 06 April 2024 | Flat             | 35.0 | 0.0  | 0.0    | 35.0        |
      | Fixed Returned payment fee | false     | Specified due date | 06 April 2024 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
      | Fixed Returned payment fee | false     | Specified due date | 10 April 2024 | Flat             | 5.0  | 0.0  | 0.0    | 5.0         |
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "03 March 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "03 March 2024" with 260.0 EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |               | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2024  | 03 March 2024 | 75.0            | 25.0          | 0.0      | 0.0   | 0.0       | 25.0  | 25.0  | 0.0        | 25.0  | 0.0         |
      | 2  | 31   | 01 February 2024 | 03 March 2024 | 63.0            | 12.0          | 0.0      | 10.0  | 0.0       | 22.0  | 22.0  | 0.0        | 22.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 03 March 2024 | 51.0            | 12.0          | 0.0      | 10.0  | 0.0       | 22.0  | 22.0  | 0.0        | 22.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 03 March 2024 | 39.0            | 12.0          | 0.0      | 10.0  | 0.0       | 22.0  | 22.0  | 22.0       | 0.0   | 0.0         |
      | 5  | 30   | 01 May 2024      | 03 March 2024 | 27.0            | 12.0          | 0.0      | 110.0 | 0.0       | 122.0 | 122.0 | 122.0      | 0.0   | 0.0         |
      | 6  | 31   | 01 June 2024     | 03 March 2024 | 15.0            | 12.0          | 0.0      | 10.0  | 0.0       | 22.0  | 22.0  | 22.0       | 0.0   | 0.0         |
      | 7  | 30   | 01 July 2024     | 03 March 2024 | 0.0             | 15.0          | 0.0      | 10.0  | 0.0       | 25.0  | 25.0  | 25.0       | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 160.0 | 0.0       | 260.0 | 260.0 | 191.0      | 69.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Accrual          | 10.0   | 0.0       | 0.0      | 10.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Accrual          | 10.0   | 0.0       | 0.0      | 10.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Repayment        | 260.0  | 100.0     | 0.0      | 160.0 | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Accrual          | 140.0  | 0.0       | 0.0      | 140.0 | 0.0       | 0.0          | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                       | isPenalty | Payment due at     | Due as of     | Calculation type | Due  | Paid | Waived | Outstanding |
      | Installment flat fee       | false     | Installment Fee    |               | Flat             | 60.0 | 60.0 | 0.0    | 0.0         |
      | Snooze fee                 | false     | Specified due date | 06 April 2024 | Flat             | 50.0 | 50.0 | 0.0    | 0.0         |
      | Snooze fee                 | false     | Specified due date | 06 April 2024 | Flat             | 35.0 | 35.0 | 0.0    | 0.0         |
      | Fixed Returned payment fee | false     | Specified due date | 06 April 2024 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
      | Fixed Returned payment fee | false     | Specified due date | 10 April 2024 | Flat             | 5.0  | 5.0  | 0.0    | 0.0         |
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3891
  Scenario: Progressive loan - Verify final income accrual with multiple fee charges created successfully
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 April 2024" due date and 50 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 April 2024" due date and 35 EUR transaction amount
    When Admin adds "LOAN_FIXED_RETURNED_PAYMENT_FEE" due date charge with "06 April 2024" due date and 10 EUR transaction amount
    When Admin adds "LOAN_FIXED_RETURNED_PAYMENT_FEE" due date charge with "10 April 2024" due date and 5 EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0  | 0.0       | 27.01  | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0  | 0.0       | 27.01  | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0  | 0.0       | 27.01  | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 110.0 | 0.0       | 127.01 | 0.0  | 0.0        | 0.0  | 127.01      |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0  | 0.0       | 27.01  | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0  | 0.0       | 27.0   | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 160.0 | 0.0       | 262.05 | 0.0  | 0.0        | 0.0  | 262.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                       | isPenalty | Payment due at     | Due as of     | Calculation type | Due  | Paid | Waived | Outstanding |
      | Installment flat fee       | false     | Installment Fee    |               | Flat             | 60.0 | 0.0  | 0.0    | 60.0        |
      | Snooze fee                 | false     | Specified due date | 06 April 2024 | Flat             | 50.0 | 0.0  | 0.0    | 50.0        |
      | Snooze fee                 | false     | Specified due date | 06 April 2024 | Flat             | 35.0 | 0.0  | 0.0    | 35.0        |
      | Fixed Returned payment fee | false     | Specified due date | 06 April 2024 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
      | Fixed Returned payment fee | false     | Specified due date | 10 April 2024 | Flat             | 5.0  | 0.0  | 0.0    | 5.0         |
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "03 March 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "03 March 2024" with 262.05 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees  | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2024  |               | 100.0           |               |          | 0.0   |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2024 | 03 March 2024 | 83.57           | 16.43         | 0.58     | 10.0  | 0.0       | 27.01  | 27.01  | 0.0        | 27.01  | 0.0         |
      | 2  | 29   | 01 March 2024    | 03 March 2024 | 67.05           | 16.52         | 0.49     | 10.0  | 0.0       | 27.01  | 27.01  | 0.0        | 27.01  | 0.0         |
      | 3  | 31   | 01 April 2024    | 03 March 2024 | 50.43           | 16.62         | 0.39     | 10.0  | 0.0       | 27.01  | 27.01  | 27.01      | 0.0    | 0.0         |
      | 4  | 30   | 01 May 2024      | 03 March 2024 | 33.71           | 16.72         | 0.29     | 110.0 | 0.0       | 127.01 | 127.01 | 127.01     | 0.0    | 0.0         |
      | 5  | 31   | 01 June 2024     | 03 March 2024 | 16.9            | 16.81         | 0.2      | 10.0  | 0.0       | 27.01  | 27.01  | 27.01      | 0.0    | 0.0         |
      | 6  | 30   | 01 July 2024     | 03 March 2024 | 0.0             | 16.9          | 0.1      | 10.0  | 0.0       | 27.0   | 27.0   | 27.0       | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      | 100.0         | 2.05     | 160.0 | 0.0       | 262.05 | 262.05 | 208.03     | 54.02 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 02 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Accrual          | 10.02  | 0.0       | 0.02     | 10.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 16 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Accrual          | 0.01   | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Accrual          | 10.02  | 0.0       | 0.02     | 10.0  | 0.0       | 0.0          | false    | false    |
      | 02 March 2024    | Accrual          | 0.01   | 0.0       | 0.01     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Repayment        | 262.05 | 100.0     | 2.05     | 160.0 | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Accrual          | 140.97 | 0.0       | 0.97     | 140.0 | 0.0       | 0.0          | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                       | isPenalty | Payment due at     | Due as of     | Calculation type | Due  | Paid | Waived | Outstanding |
      | Installment flat fee       | false     | Installment Fee    |               | Flat             | 60.0 | 60.0 | 0.0    | 0.0         |
      | Snooze fee                 | false     | Specified due date | 06 April 2024 | Flat             | 50.0 | 50.0 | 0.0    | 0.0         |
      | Snooze fee                 | false     | Specified due date | 06 April 2024 | Flat             | 35.0 | 35.0 | 0.0    | 0.0         |
      | Fixed Returned payment fee | false     | Specified due date | 06 April 2024 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
      | Fixed Returned payment fee | false     | Specified due date | 10 April 2024 | Flat             | 5.0  | 5.0  | 0.0    | 0.0         |
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3892
  Scenario: Verify installment fee charge allocation when loan has down payment, additional installment and re-aging
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 15   | 16 January 2024  |           | 500.0           | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
      | 3  | 15   | 31 January 2024  |           | 250.0           | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
      | 4  | 15   | 15 February 2024 |           | 0.0             | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 30.0 | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 30.0 | 0.0  | 0.0    | 30.0        |
    When Admin sets the business date to "01 January 2024"
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2024  |                 | 500.0           | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
      | 3  | 15   | 31 January 2024  |                 | 250.0           | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
      | 4  | 15   | 15 February 2024 |                 | 0.0             | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 January 2024  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 30.0 | 0.0       | 1030.0 | 250.0 | 0.0        | 0.0  | 780.0       |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 30.0 | 0.0  | 0.0    | 30.0        |
    When Admin sets the business date to "20 February 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2024  |                 | 500.0           | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
      | 3  | 15   | 31 January 2024  |                 | 250.0           | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
      | 4  | 15   | 15 February 2024 |                 | 0.0             | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
      | 5  | 5    | 20 February 2024 |                 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 0.0   | 0.0        | 0.0  | 125.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 January 2024  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    |
      | 20 February 2024 | Chargeback       | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 875.0        | false    |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1125.0        | 0.0      | 30.0 | 0.0       | 1155.0 | 250.0 | 0.0        | 0.0  | 905.0       |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 30.0 | 0.0  | 0.0    | 30.0        |
    When Admin sets the business date to "21 February 2024"
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 2               | MONTHS        | 10 March 2024 | 3                    |
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2024  |                  | 750.0           | 0.0           | 0.0      | 10.0 | 0.0       | 10.0   | 0.0   | 0.0        | 0.0  | 10.0        |
      | 3  | 15   | 31 January 2024  |                  | 750.0           | 0.0           | 0.0      | 10.0 | 0.0       | 10.0   | 0.0   | 0.0        | 0.0  | 10.0        |
      | 4  | 15   | 15 February 2024 |                  | 750.0           | 0.0           | 0.0      | 10.0 | 0.0       | 10.0   | 0.0   | 0.0        | 0.0  | 10.0        |
      | 5  | 5    | 20 February 2024 | 21 February 2024 | 875.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0   | 0.0        | 0.0  | 0.0         |
      | 6  | 19   | 10 March 2024    |                  | 583.33          | 291.67        | 0.0      | 0.0  | 0.0       | 291.67 | 0.0   | 0.0        | 0.0  | 291.67      |
      | 7  | 61   | 10 May 2024      |                  | 291.66          | 291.67        | 0.0      | 0.0  | 0.0       | 291.67 | 0.0   | 0.0        | 0.0  | 291.67      |
      | 8  | 61   | 10 July 2024     |                  | 0.0             | 291.66        | 0.0      | 0.0  | 0.0       | 291.66 | 0.0   | 0.0        | 0.0  | 291.66      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 January 2024  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    |
      | 20 February 2024 | Chargeback       | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 875.0        | false    |
      | 21 February 2024 | Re-age           | 875.0  | 875.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1125.0        | 0.0      | 30.0 | 0.0       | 1155.0 | 250.0 | 0.0        | 0.0  | 905.0       |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 30.0 | 0.0  | 0.0    | 30.0        |
    When Loan Pay-off is made on "21 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met


