@LoanChargesDisbursementChargesFeature
Feature: LoanChargesDisbursementCharges

  @TestRailId:C3546
  Scenario: Verify flat disbursement charge for interest bearing progressive loan - UC1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "FLAT" calculation type and 10.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 10.00           |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 10.0 |           | 10.0  | 10.0 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 0.0       | 112.05 | 10.0 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 10.0  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 10.0 |           | 10.0  | 10.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 0.0       | 112.05 | 27.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 10.0 |           | 10.0  | 10.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 0.0       | 112.05 | 44.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3547
  Scenario: Verify amount disbursement charge for interest bearing progressive loan - UC2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.00            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.0  | 0.0  | 0.0    | 1.0        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 1.0  |           | 1.0   | 1.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 1.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.0  | 1.0  | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.0   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 18.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 35.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3548
  Scenario: Verify amount+interest disbursement charge for interest bearing progressive loan - UC3
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.00            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.02 | 0.0  | 0.0    | 1.02        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 1.02 |           | 1.02  | 1.02 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.02 | 0.0       | 103.07 | 1.02 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.02   | 0.0       | 0.0      | 1.02 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.02 | 1.02 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.02   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.02  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.02 |           | 1.02  | 1.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.02 | 0.0       | 103.07 | 18.03 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.02   | 0.0       | 0.0      | 1.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.02 |           | 1.02  | 1.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.02 | 0.0       | 103.07 | 35.04 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.02   | 0.0       | 0.0      | 1.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3549
  Scenario: Verify interest disbursement charge for interest bearing progressive loan - UC4
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.00            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.0  | 0.0    | 0.02       |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.02 |           | 0.02  | 0.02 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 0.02 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.02 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.02   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.02  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 17.03 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 34.04 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3550
  Scenario: Verify interest disbursement charge with cash based accounting for interest bearing progressive loan - UC5
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CASH_ACCOUNTING_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.00            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.0  | 0.0    | 0.02       |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.02 |           | 0.02  | 0.02 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 0.02 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.02 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.02   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.02  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 17.03 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 34.04 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3551
  Scenario: Verify interest disbursement charge with accrual based accounting for interest bearing progressive loan - UC6.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 0.02            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.0  | 0.0  | 0.0    | 0.0        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.0  | 0.0  | 0.0    | 0.0         |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 34.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3552
  Scenario: Verify amount+interest disbursement charge for interest bearing progressive loan - UC6.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.02            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.04 | 0.0  | 0.0    | 1.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 1.04 |           | 1.04  | 1.04 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.04 | 0.0       | 103.09 | 1.04 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.04   | 0.0       | 0.0      | 1.04 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.04 | 1.04 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.04   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.04  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.04 |           | 1.04  | 1.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.04 | 0.0       | 103.09 | 18.05 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.04   | 0.0       | 0.0      | 1.04 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.04 |           | 1.04  | 1.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.04 | 0.0       | 103.09 | 35.06 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.04   | 0.0       | 0.0      | 1.04 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3553
  Scenario: Verify interest disbursement charge with undo disbursal for interest bearing progressive loan - UC7
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.00            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.0  | 0.0    | 0.02       |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.02 |           | 0.02  | 0.02 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 0.02 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.02 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.02   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.02  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 17.03 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 34.04 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
#    --- Undo Disbursement  ---
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.02 |           | 0.02  |      |            |      | 0.02         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 0.0  | 0.0        | 0.0  | 102.07      |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.0  | 0.0    | 0.02        |
    Then Admin can successfully undone the loan approval
    Then Loan status will be "SUBMITTED_AND_PENDING_APPROVAL"

  @TestRailId:C3578
  Scenario: Verify amount disbursement charge for tranche interest bearing progressive loan that expects one tranche with full disbursement - UC8.1.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | tranche_disb_expected_date | tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024            | 100.0                  |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0         |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.0  |           | 2.0   | 2.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.0  | 0.0       | 104.05 | 2.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.0    | 0.0       | 0.0      | 2.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 2.0  | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 2.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 2.0   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 2.0  |           | 2.0   | 2.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.0  | 0.0       | 104.05 | 19.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.0    | 0.0       | 0.0      | 2.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 2.0  | 0.0    | 0.0         |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3579
  Scenario: Verify amount+interest disbursement charge for tranche interest bearing progressive loan that expects one tranche with full disbursement - UC8.1.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | tranche_disb_expected_date | tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024            | 100.0                  |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type          | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest  | 2.04 | 0.0  | 0.0    | 2.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.04 |           | 2.04  | 2.04 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.04 | 0.0       | 104.09 | 2.04 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.04   | 0.0       | 0.0      | 2.04 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 2.04 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 2.04   |
      | LIABILITY | 145023       | Suspense/Clearing account | 2.04  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 2.04 |           | 2.04  | 2.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.04 | 0.0       | 104.09 | 19.05 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.04   | 0.0       | 0.0      | 2.04 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 2.04 | 0.0    | 0.0         |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3580
  Scenario: Verify interest disbursement charge for tranche interest bearing progressive loan that expects one tranche with full disbursement - UC8.1.3
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | tranche_disb_expected_date | tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024            | 100.0                  |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.0  | 0.0    | 0.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.04 |           | 0.04  | 0.04 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.04 | 0.0       | 102.09 | 0.04 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.04   | 0.0       | 0.0      | 0.04 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.04 | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.04   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.04  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.04 |           | 0.04  | 0.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.04 | 0.0       | 102.09 | 17.05 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.04   | 0.0       | 0.0      | 0.04 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.04 | 0.0    | 0.0         |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3581
  Scenario: Verify amount+interest disbursement charge for tranche interest bearing progressive loan that doesn't expect tranches with full disbursement - UC8.1.4
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0         |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.0  |           | 2.0   | 2.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.0  | 0.0       | 104.05 | 2.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.0    | 0.0       | 0.0      | 2.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 2.0  | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 2.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 2.0   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 2.0  |           | 2.0   | 2.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.0  | 0.0       | 104.05 | 19.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.0    | 0.0       | 0.0      | 2.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 2.0  | 0.0    | 0.0         |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3582
  Scenario: Verify amount+interest disbursement charge for tranche interest bearing progressive loan that doesn't expect tranches with full disbursement - UC8.1.5
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 0.0  | 0.0    | 2.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.04 |           | 2.04  | 2.04 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.04 | 0.0       | 104.09 | 2.04 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.04   | 0.0       | 0.0      | 2.04 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type          | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           |  % Loan Amount + Interest | 2.04 | 2.04 | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 2.04   |
      | LIABILITY | 145023       | Suspense/Clearing account | 2.04  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 2.04 |           | 2.04  | 2.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.04 | 0.0       | 104.09 | 19.05 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.04   | 0.0       | 0.0      | 2.04 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type          | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           |  % Loan Amount + Interest | 2.04 | 2.04 | 0.0    | 0.0         |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3583
  Scenario: Verify interest disbursement charge for tranche interest bearing progressive loan that doesn't expect tranches with full disbursement - UC8.1.6
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.0  | 0.0    | 0.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.04 |           | 0.04  | 0.04 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.04 | 0.0       | 102.09 | 0.04 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.04   | 0.0       | 0.0      | 0.04 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.04 | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.04   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.04  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.04 |           | 0.04  | 0.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.04 | 0.0       | 102.09 | 17.05 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.04   | 0.0       | 0.0      | 0.04 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.04 | 0.0    | 0.0         |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3554
  Scenario: Verify amount disbursement charge for tranche interest bearing progressive loan that expects one tranche - UC8.2.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | tranche_disb_expected_date | tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024            | 100.0                  |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0         |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.4  |           | 1.4   | 1.4  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.4  | 0.0       | 72.84  | 1.4  | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.4  | 1.4  | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.4    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.4   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.4  |           | 1.4   | 1.4   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |  70.0         | 1.44     | 1.4  | 0.0       | 72.84  | 13.31 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3555
  Scenario: Verify amount disbursement charge for tranche interest bearing progressive loan that expects two tranches with undo last disbursement - UC8.2.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursements details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | 1st_tranche_disb_expected_date |1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024                | 70.0                      | 01 February 2024               | 30.0                       |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.4  | 0.0  | 0.0    | 1.4        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.4  |           | 1.4   | 1.4  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      |    |      | 01 February 2024 |           | 30.0            |               |          | 0.0  |           | 0.0   |      |            |      |  0.0        |
      | 2  | 29   | 01 March 2024    |           | 76.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 65.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 53.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 41.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 30.0            | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.4  | 0.0       | 72.84  | 1.4  | 0.0        | 0.0  | 71.44       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type  | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount          | 1.4  | 1.4  | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.4    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.4   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.4  |           | 1.4   | 1.4   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 30.0            |               |          | 0.0  |           | 0.0   |       |            |      | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 76.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 65.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 53.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 41.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 30.0            | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.4  | 0.0       | 72.84  | 13.31 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
#    --- 2nd disbursement - 1 February, 2024  ---
    When Admin successfully disburse the loan on "01 February 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.4  |           | 1.4   | 1.4   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 30.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 71.01           | 17.49         | 0.52     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 3  | 31   | 01 April 2024    |                  | 53.41           | 17.6          | 0.41     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 4  | 30   | 01 May 2024      |                  | 35.71           | 17.7          | 0.31     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.91           | 17.8          | 0.21     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.91         | 0.1      | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.96     | 1.4  | 0.0       | 103.36 | 13.31 | 0.0        | 0.0  | 90.05       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
      | 01 February 2024 | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 88.5         | false    | false    |
# -- undo last disbursement ----
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.4  |           | 1.4   | 1.4   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |  70.0         | 1.44     | 1.4  | 0.0       | 72.84  | 13.31 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3556
  Scenario: Verify amount+interest disbursement charge for tranche interest bearing progressive loan that expects two tranches with undo disbursement - UC8.2.3
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursements details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | 1st_tranche_disb_expected_date |1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024                | 70.0                      | 01 February 2024               | 30.0                       |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.43 | 0.0  | 0.0    | 1.43        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.43 |           | 1.43  | 1.43 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      |    |      | 01 February 2024 |           | 30.0            |               |          | 0.0  |           | 0.0   |      |            |      |   0.0       |
      | 2  | 29   | 01 March 2024    |           | 76.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 65.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 53.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 41.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 30.0            | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.43 | 0.0       | 72.87  | 1.43 | 0.0        | 0.0  | 71.44       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.43 | 1.43 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.43   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.43  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.43 |           | 1.43  | 1.43  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 30.0            |               |          | 0.0  |           | 0.0   |       |            |      |  0.0        |
      | 2  | 29   | 01 March 2024    |                  | 76.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 65.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 53.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 41.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 30.0            | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.43 | 0.0       | 72.87  | 13.34 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
#    --- 2nd disbursement - 1 February, 2024  ---
    When Admin successfully disburse the loan on "01 February 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.43 |           | 1.43  | 1.43  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 30.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 71.01           | 17.49         | 0.52     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 3  | 31   | 01 April 2024    |                  | 53.41           | 17.6          | 0.41     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 4  | 30   | 01 May 2024      |                  | 35.71           | 17.7          | 0.31     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.91           | 17.8          | 0.21     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.91         | 0.1      | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.96     | 1.43 | 0.0       | 103.39 | 13.34 | 0.0        | 0.0  | 90.05       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
      | 01 February 2024 | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 88.5         | false    | false    |
# -- undo disbursement ----
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date  | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |            | 70.0            |               |          | 1.43 |           | 1.43  |       |            |      | 1.43        |
      | 1  | 31   | 01 February 2024 |            | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      |    |      | 01 February 2024 |            | 30.0            |               |          | 0.0  |           | 0.0   |       |            |      |  0.0        |
      | 2  | 29   | 01 March 2024    |            | 71.01           | 17.49         | 0.52     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 3  | 31   | 01 April 2024    |            | 53.41           | 17.6          | 0.41     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 4  | 30   | 01 May 2024      |            | 35.71           | 17.7          | 0.31     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 5  | 31   | 01 June 2024     |            | 17.91           | 17.8          | 0.21     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 6  | 30   | 01 July 2024     |            | 0.0             | 17.91         | 0.1      | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.96     | 1.43 | 0.0       | 103.39 | 0.0   | 0.0        | 0.0  | 103.39      |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.43 | 0.0  | 0.0    | 1.43        |
    Then Admin can successfully undone the loan approval
    Then Loan status will be "SUBMITTED_AND_PENDING_APPROVAL"

  @TestRailId:C3557
  Scenario: Verify amount+interest disbursement charge for tranche interest bearing progressive loan that expects one tranche - UC8.2.4
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | tranche_disb_expected_date | tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024            | 100.0                  |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 0.0  | 0.0    | 2.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.43 |           | 1.43  | 1.43 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44      | 1.43 | 0.0       | 72.87 | 1.43 | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.43 | 1.43 | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.43   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.43  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.43 |           | 1.43  | 1.43  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.43 | 0.0       | 72.87 | 13.34 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3558
  Scenario: Verify interest disbursement charge for tranche interest bearing progressive loan that expects one tranche with undo disbursement - UC8.2.5
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | tranche_disb_expected_date | tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024            | 100.0                  |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.0  | 0.0    | 0.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 0.03 |           | 0.03  | 0.03 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 0.03 | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.03 | 0.03 | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.03   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.03  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 0.03 |           | 0.03  | 0.03  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 11.94 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
# -- undo disbursement ----
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           |  70.0           |               |          | 0.03 |           | 0.03  |      |            |      | 0.03        |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 0.00 | 0.0        | 0.0  | 71.47        |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.03 | 0.0  | 0.0    | 0.03        |
    Then Admin can successfully undone the loan approval
    Then Loan status will be "SUBMITTED_AND_PENDING_APPROVAL"

  @TestRailId:C3560
  Scenario: Verify amount disbursement charge for tranche interest bearing progressive loan that doesn't expect tranches with undo disbursements - UC8.2.6
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0         |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.4  |           | 1.4   | 1.4  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.4  | 0.0       | 72.84  | 1.4  | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.4  | 1.4  | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.4    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.4   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.4  |           | 1.4   | 1.4   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |  70.0         | 1.44     | 1.4  | 0.0       | 72.84  | 13.31 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
#    --- 2nd disbursement - 1 February, 2024  ---
    When Admin successfully disburse the loan on "01 February 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.4  |           | 1.4   | 1.4   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 30.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 71.01           | 17.49         | 0.52     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 3  | 31   | 01 April 2024    |                  | 53.41           | 17.6          | 0.41     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 4  | 30   | 01 May 2024      |                  | 35.71           | 17.7          | 0.31     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.91           | 17.8          | 0.21     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.91         | 0.1      | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.96     | 1.4  | 0.0       | 103.36 | 13.31 | 0.0        | 0.0  | 90.05       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
      | 01 February 2024 | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 88.5         | false    | false    |
# -- undo disbursement ----
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 1.4  |           | 1.4   |      |            |      | 1.4         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.4  | 0.0       | 103.45 | 0.0   | 0.0        | 0.0  | 103.45      |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0         |
    Then Admin can successfully undone the loan approval
    Then Loan status will be "SUBMITTED_AND_PENDING_APPROVAL"

  @TestRailId:C3561
  Scenario: Verify amount+interest disbursement charge for tranche interest bearing progressive loan that doesn't expect tranches with undo disbursement - UC8.2.7
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 0.0  | 0.0    | 2.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.43 |           | 1.43  | 1.43 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44      | 1.43 | 0.0       | 72.87 | 1.43 | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.43 | 1.43 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.43   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.43  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.43 |           | 1.43  | 1.43  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.43 | 0.0       | 72.87 | 13.34 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
# -- undo disbursement ----
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.04 |           |  2.04 |      |            |      |  2.04       |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.04 | 0.0       | 104.09 | 0.0   | 0.0        | 0.0  | 104.09      |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 0.0  | 0.0    | 2.04        |
    Then Admin can successfully undone the loan approval
    Then Loan status will be "SUBMITTED_AND_PENDING_APPROVAL"

  @TestRailId:C3562
  Scenario: Verify interest disbursement charge for tranche interest bearing progressive loan that doesn't expect tranches with undo last disbursement - UC8.2.8
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.0  | 0.0    | 0.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 0.03 |           | 0.03  | 0.03 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 0.03 | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.03 | 0.03 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.03   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.03  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 0.03 |           | 0.03  | 0.03  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 11.94 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
#    --- 2nd disbursement - 1 February, 2024  ---
    When Admin successfully disburse the loan on "01 February 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 0.03 |           | 0.03  | 0.03  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 30.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 71.01           | 17.49         | 0.52     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 3  | 31   | 01 April 2024    |                  | 53.41           | 17.6          | 0.41     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 4  | 30   | 01 May 2024      |                  | 35.71           | 17.7          | 0.31     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.91           | 17.8          | 0.21     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.91         | 0.1      | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.96     | 0.03 | 0.0       | 101.99 | 11.94 | 0.0        | 0.0  | 90.05       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
      | 01 February 2024 | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 88.5         | false    | false    |
# -- undo last disbursement ----
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 0.03 |           | 0.03  | 0.03  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 11.94 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.03 | 0.03 | 0.0    | 0.0         |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3563
  Scenario: Verify amount disbursement charge for interest bearing progressive with partial disbursal and with undo disbursement - UC2.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0         |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.4  |           | 1.4   | 1.4  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.4  | 0.0       | 72.84  | 1.4  | 0.0        | 0.0  | 71.44       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.4  | 1.4  | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.4    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.4   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.4  |           | 1.4   | 1.4   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.4  | 0.0       | 72.84  | 13.31 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
# -- undo disbursement ----
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.0  |           | 2.0   |      |            |      | 2.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.0  | 0.0       | 104.05 | 0.0   | 0.0        | 0.0  | 104.05      |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0         |
    Then Admin can successfully undone the loan approval
    Then Loan status will be "SUBMITTED_AND_PENDING_APPROVAL"

  @TestRailId:C3564
  Scenario: Verify amount+interest disbursement charge for interest bearing progressive loan with partial disbursal and with undo disbursement - UC3.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 0.0  | 0.0    | 2.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.43 |           | 1.43  | 1.43 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44      | 1.43 | 0.0       | 72.87 | 1.43 | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.43 | 1.43 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.43   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.43  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.43 |           | 1.43  | 1.43  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.43 | 0.0       | 72.87 | 13.34 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
# -- undo disbursement ----
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.04 |           | 2.04  |      |            |      | 2.04         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.04 | 0.0       | 104.09 | 0.0   | 0.0        | 0.0  | 104.09      |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 0.0  | 0.0    | 2.04        |
    Then Admin can successfully undone the loan approval
    Then Loan status will be "SUBMITTED_AND_PENDING_APPROVAL"

  @TestRailId:C3565
  Scenario: Verify interest disbursement charge for interest bearing progressive loan with partial disbursal - UC4.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.0  | 0.0    | 0.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 0.03 |           | 0.03  | 0.03 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 0.03 | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.03 | 0.03 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.03   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.03  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 0.03 |           | 0.03  | 0.03  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 11.94 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3566
  Scenario: Verify amount disbursement charge with reversed repayment for backdated interest bearing progressive loan - UC9
    When Admin sets the business date to "01 March 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.00            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.0  | 0.0  | 0.0    | 1.0        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 1.0  |           | 1.0   | 1.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.14           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.52           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.8            | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.99           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.99         | 0.1      | 0.0  | 0.0       | 17.09 | 0.0  | 0.0        | 0.0  | 17.09       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.14     | 1.0  | 0.0       | 103.14 | 1.0  | 0.0        | 0.0  | 102.14      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.0  | 1.0  | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.0   |        |
#    --- 1st repayment - 1 February, 2024  ---
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 18.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "02 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |       |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0   | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0   | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0   | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 35.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
#    --- First repayment reversed ---
    When Admin sets the business date to "03 March 2024"
    When Customer undo "1"th "Repayment" transaction made on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |       |             |
      | 1  | 31   | 01 February 2024 | 01 March 2024    | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 17.01 | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.14           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.53           | 16.61         | 0.4      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.81           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.1      | 0.0  | 0.0       | 17.1  | 0.0   | 0.0        | 0.0   | 17.1        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 100.0         | 2.15     | 1.0  | 0.0       | 103.15 | 18.01 | 0.0        | 17.01 | 85.14       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | true     | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | true     |
    When Loan Pay-off is made on "03 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met