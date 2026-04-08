@LoanChargesTrancheDisbursementChargesFeature
Feature: LoanChargesTrancheDisbursementCharges

  @TestRailId:C3650
  Scenario: Tranche disbursement charges - disbursement flat charge
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type                        | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 130            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT | 10.0          | 01 January 2024                | 100.0                      | 03 March 2024                  | 30.0                       |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    And Admin successfully approves the loan on "01 January 2024" with "130" amount and expected disbursement date on "01 January 2024"
    When Admin disburses the loan on "01 January 2024" with "100" EUR transaction amount
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
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 10.0  |        |
    Then LoanDisbursalTransactionBusinessEvent has changedTerms "false"
    # Add repayment on 01 February 2024
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
    # Add repayment on 01 March 2024
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
    # Add additional disbursement on 03 March 2024
    When Admin sets the business date to "03 March 2024"
    And Admin successfully add disbursement detail to the loan on "03 March 2024" with 30 EUR transaction amount
    When Admin disburses the loan on "03 March 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 10.0 |           | 10.0  | 10.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 30.0            |               |          | 10.0 |           | 10.0  | 10.0  |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 72.99           | 24.06         | 0.55     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 4  | 30   | 01 May 2024      |                  | 48.81           | 24.18         | 0.43     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 5  | 31   | 01 June 2024     |                  | 24.48           | 24.33         | 0.28     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 24.48         | 0.14     | 0.0  | 0.0       | 24.62 | 0.0   | 0.0        | 0.0  | 24.62       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 130.0         | 2.47     | 20.0 | 0.0       | 152.47 | 54.02 | 0.0        | 0.0  | 98.45       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 97.05        | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 97.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 03 March 2024   | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then LoanDisbursalTransactionBusinessEvent has changedTerms "false"
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 10.0  |        |
    When Loan Pay-off is made on "03 March 2024"
    #TDOD - need fix, as loan account has outstanding amount equal to Tranche disb charge amount and status is Active after pay-off
    #Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3652
  Scenario: Tranche disbursement charges - disbursement percentage charge
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT" with "PERCENTAGE_DISBURSEMENT_AMOUNT" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type                         | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 130            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT | 1.0           | 01 January 2024                | 100.0                      | 03 March 2024                  | 30.0                       |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type                | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 01 January 2024 | % Disbursement Amount           | 1.0 | 0.0  | 0.0    | 1.0         |
    And Admin successfully approves the loan on "01 January 2024" with "130" amount and expected disbursement date on "01 January 2024"
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
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type       | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 01 January 2024 | % Disbursement Amount  | 1.0 | 1.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.0   |        |
    # First repayment
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
    # Second repayment
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
    # Second disbursement
    When Admin sets the business date to "03 March 2024"
    And Admin successfully add disbursement detail to the loan on "03 March 2024" with 30 EUR transaction amount
    When Admin successfully disburse the loan on "03 March 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 30.0            |               |          | 0.3  |           | 0.3   | 0.3   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 72.99           | 24.06         | 0.55     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 4  | 30   | 01 May 2024      |                  | 48.81           | 24.18         | 0.43     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 5  | 31   | 01 June 2024     |                  | 24.48           | 24.33         | 0.28     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 24.48         | 0.14     | 0.0  | 0.0       | 24.62 | 0.0   | 0.0        | 0.0  | 24.62       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 130.0         | 2.47     | 1.3  | 0.0       | 133.77 | 35.32 | 0.0        | 0.0  | 98.45       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 97.05        | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 0.3    | 0.0       | 0.0      | 0.3  | 0.0       | 97.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type       | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 03 March 2024   | % Disbursement Amount  | 0.3 | 0.3  | 0.0    | 0.0         |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 01 January 2024 | % Disbursement Amount  | 1.0 | 1.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.3    |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.3   |        |
    When Loan Pay-off is made on "01 March 2024"
    #TDOD - need fix, as loan account has outstanding amount equal to Tranche disb charge amount and status is Active after pay-off
    #Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3653
  Scenario: Tranche disbursement charges - flat and cash based accounting
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT" with "FLAT" calculation type and 0.02 EUR amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type                        | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT | 0.02          | 01 January 2024                | 100.0                      | 03 March 2024                  | 100.0                      |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount  | false     | Tranche Disbursement | 01 January 2024 | Flat             | 0.02 | 0.0  | 0.0    | 0.02        |
    And Admin successfully approves the loan on "01 January 2024" with "130" amount and expected disbursement date on "01 January 2024"
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
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount  | false     | Tranche Disbursement | 01 January 2024 | Flat             | 0.02 | 0.02 | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.02   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.02  |        |
    # First repayment
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
    # Second repayment
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
    # Second disbursement
    When Admin sets the business date to "03 March 2024"
    And Admin successfully add disbursement detail to the loan on "03 March 2024" with 30 EUR transaction amount
    When Admin successfully disburse the loan on "03 March 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 30.0            |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 72.99           | 24.06         | 0.55     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 4  | 30   | 01 May 2024      |                  | 48.81           | 24.18         | 0.43     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 5  | 31   | 01 June 2024     |                  | 24.48           | 24.33         | 0.28     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 24.48         | 0.14     | 0.0  | 0.0       | 24.62 | 0.0   | 0.0        | 0.0  | 24.62       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 130.0         | 2.47     | 0.04 | 0.0       | 132.51 | 34.06 | 0.0        | 0.0  | 98.45       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 97.05        | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 97.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 03 March 2024   | Flat             | 0.02 | 0.02 | 0.0    | 0.0         |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 0.02 | 0.02 | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.02   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.02  |        |
    When Loan Pay-off is made on "03 March 2024"
    #TDOD - need fix, as loan account has outstanding amount equal to Tranche disb charge amount and status is Active after pay-off
    #Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3654
  Scenario: Tranche disbursement charges - percentage disbursement and accrual based accounting
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT" with "PERCENTAGE_DISBURSEMENT_AMOUNT" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type                         | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT | 1.0           | 01 January 2024                | 100.0                      | 03 March 2024                  | 100.0                      |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type                | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 01 January 2024 | % Disbursement Amount           | 1.0 | 0.0  | 0.0    | 1.0         |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
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
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type       | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 01 January 2024 | % Disbursement Amount  | 1.0 | 1.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.0   |        |
    # First repayment
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
    # Second repayment
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
    # Second disbursement
    When Admin sets the business date to "03 March 2024"
    And Admin successfully add disbursement detail to the loan on "03 March 2024" with 100 EUR transaction amount
    When Admin successfully disburse the loan on "03 March 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 125.62          | 41.43         | 0.94     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 4  | 30   | 01 May 2024      |                  | 83.98           | 41.64         | 0.73     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 5  | 31   | 01 June 2024     |                  | 42.1            | 41.88         | 0.49     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 42.1          | 0.25     | 0.0  | 0.0       | 42.35 | 0.0   | 0.0        | 0.0  | 42.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.48     | 2.0  | 0.0       | 205.48  | 36.02 | 0.0        | 0.0  | 169.46      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 167.05       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type       | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 03 March 2024   | % Disbursement Amount  | 1.0 | 1.0  | 0.0    | 0.0         |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 01 January 2024 | % Disbursement Amount  | 1.0 | 1.0  | 0.0    | 0.0         |

    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.0   |        |
    # Run income recognition for accrual test
    And Admin runs the Accrual Activity Posting job
    And Admin runs the Add Accrual Transactions job
    And Admin runs the Add Accrual Transactions For Loans With Income Posted As Transactions job
    And Admin runs the Add Periodic Accrual Transactions job
    And Admin runs the Recalculate Interest for Loans job
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Accrual                             | 1.1    | 0.0       | 1.1      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "03 March 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 1.1   |        |
      | INCOME | 404000       | Interest Income         |       | 1.1    |
    When Loan Pay-off is made on "03 March 2024"
    #TDOD - need fix, as loan account has outstanding amount equal to Tranche disb charge amount and status is Active after pay-off
    #Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3655
  Scenario: Tranche disbursement charges - flat and accrual based accounting - undo disbursement
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT" with "FLAT" calculation type and 5.0 EUR amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type                        | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT | 5.0           | 01 January 2024                | 100.0                      | 03 March 2024                  | 100.0                      |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 0.0  | 0.0    | 5.0         |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 5.0  |           | 5.0   | 5.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 0.0       | 107.05 | 5.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 5.0   |        |
    # First repayment
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 0.0       | 107.05 | 22.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    # Second repayment
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 0.0       | 107.05 | 39.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    # Second disbursement
    When Admin sets the business date to "03 March 2024"
    And Admin successfully add disbursement detail to the loan on "03 March 2024" with 100 EUR transaction amount
    When Admin successfully disburse the loan on "03 March 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 125.62          | 41.43         | 0.94     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 4  | 30   | 01 May 2024      |                  | 83.98           | 41.64         | 0.73     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 5  | 31   | 01 June 2024     |                  | 42.1            | 41.88         | 0.49     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 42.1          | 0.25     | 0.0  | 0.0       | 42.35 | 0.0   | 0.0        | 0.0  | 42.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.48     | 10.0 | 0.0       | 213.48 | 44.02 | 0.0        | 0.0  | 169.46      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 167.05       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 03 March 2024   | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 5.0   |        |
    # Run income recognition for accrual test
    When Admin sets the business date to "03 March 2024"
    And Admin runs the Accrual Activity Posting job
    And Admin runs the Add Accrual Transactions job
    And Admin runs the Add Accrual Transactions For Loans With Income Posted As Transactions job
    And Admin runs the Add Periodic Accrual Transactions job
    And Admin runs the Recalculate Interest for Loans job
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Accrual                             | 1.1    | 0.0       | 1.1      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "03 March 2024" which has the following Journal entries:
      | Type   | Account code | Account name              | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable   | 1.1   |        |
      | INCOME | 404000       | Interest Income           |       | 1.1    |
    # Undo disbursement
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 5.0  |           | 5.0   |      |            |      | 5.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      |    |      | 03 March 2024    |           | 100.0           |               |          | 5.0  |           | 5.0   |      |            |      | 5.0         |
      | 3  | 31   | 01 April 2024    |           | 125.62          | 41.43         | 0.94     | 0.0  | 0.0       | 42.37 | 0.0  | 0.0        | 0.0  | 42.37       |
      | 4  | 30   | 01 May 2024      |           | 83.98           | 41.64         | 0.73     | 0.0  | 0.0       | 42.37 | 0.0  | 0.0        | 0.0  | 42.37       |
      | 5  | 31   | 01 June 2024     |           | 42.1            | 41.88         | 0.49     | 0.0  | 0.0       | 42.37 | 0.0  | 0.0        | 0.0  | 42.37       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 42.1          | 0.25     | 0.0  | 0.0       | 42.35 | 0.0  | 0.0        | 0.0  | 42.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 3.48     | 10.0 | 0.0       | 213.48 | 0.0  | 0.0        | 0.0  | 213.48      |
    Then Loan Transactions tab has none transaction
    Then Admin can successfully undone the loan approval
    Then Loan status will be "SUBMITTED_AND_PENDING_APPROVAL"

  @TestRailId:C3656
  Scenario: Tranche disbursement charges - flat and accrual based accounting - undo last disbursement
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT" with "FLAT" calculation type and 5.0 EUR amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type                        | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT | 5.0           | 01 January 2024                | 100.0                      | 03 March 2024                  | 100.0                      |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount  | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 0.0  | 0.0    | 5.0         |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 5.0  |           | 5.0   | 5.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 0.0       | 107.05 | 5.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount  | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 5.0   |        |
    # First repayment
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 0.0       | 107.05 | 22.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    # Second repayment
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 0.0       | 107.05 | 39.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    # Second disbursement
    When Admin sets the business date to "03 March 2024"
    And Admin successfully add disbursement detail to the loan on "03 March 2024" with 100 EUR transaction amount
    When Admin successfully disburse the loan on "03 March 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 125.62          | 41.43         | 0.94     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 4  | 30   | 01 May 2024      |                  | 83.98           | 41.64         | 0.73     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 5  | 31   | 01 June 2024     |                  | 42.1            | 41.88         | 0.49     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 42.1          | 0.25     | 0.0  | 0.0       | 42.35 | 0.0   | 0.0        | 0.0  | 42.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.48     | 10.0 | 0.0       | 213.48  | 44.02 | 0.0        | 0.0  | 169.46      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 167.05       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 03 March 2024   | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 5.0   |        |
    # Run income recognition for accrual test
    When Admin sets the business date to "03 March 2024"
    And Admin runs the Accrual Activity Posting job
    And Admin runs the Add Accrual Transactions job
    And Admin runs the Add Accrual Transactions For Loans With Income Posted As Transactions job
    And Admin runs the Add Periodic Accrual Transactions job
    And Admin runs the Recalculate Interest for Loans job
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Accrual                             | 1.1    | 0.0       | 1.1      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "03 March 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 1.1   |        |
      | INCOME | 404000       | Interest Income         |       | 1.1    |
    # Undo last disbursement
    When Admin successfully undo last disbursal
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
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Accrual                             | 1.1    | 0.0       | 1.1      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 5.0   |        |
    When Loan Pay-off is made on "03 March 2024"
    #TDOD - need fix, as loan account has outstanding amount equal to Tranche disb charge amount and status is Active after pay-off
    #Then Loan is closed with zero outstanding balance and it's all installments have obligations met

