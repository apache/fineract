@LoanChargeback
Feature: LoanChargeback - Part3

  @TestRailId:C3448
  Scenario: Two chargeback before the maturity date with interest recalculation ENABLED - Allocation priority: principal, interest, fees - UC4
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_PRINCIPAL_INTEREST_FEE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "1 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
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
      | 100           | 2.05     | 0    | 0         | 102.05 | 34.02 | 0          | 0    | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
# --- partial chargeback for 1st repayment made on  February, 1st --- #
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 15 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.52           | 31.53         | 0.48     | 0.0  | 0.0       | 32.01 | 0.0   | 0.0        | 0.0  | 32.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.8            | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.99           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.99         | 0.1      | 0.0  | 0.0       | 17.09 | 0.0   | 0.0        | 0.0  | 17.09       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 115.0         | 2.14     | 0    | 0         | 117.14 | 34.02 | 0          | 0    | 83.12       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 82.05        | false    | false    |
# --- full chargeback for 2nd repayment made on  March, 1st --- #
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.61           | 47.96         | 1.06     | 0.0  | 0.0       | 49.02 | 0.0   | 0.0        | 0.0  | 49.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.9            | 16.71         | 0.3      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.09           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.09         | 0.1      | 0.0  | 0.0       | 17.19 | 0.0   | 0.0        | 0.0  | 17.19       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 131.52        | 2.73     | 0    | 0         | 134.25 | 34.02 | 0          | 0    | 100.23      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 82.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 98.57        | false    | false    |
    When Customer undo "2"th "Chargeback" transaction made on "01 March 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.52           | 31.53         | 0.48     | 0.0  | 0.0       | 32.01 | 0.0   | 0.0        | 0.0  | 32.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.8            | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.99           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.99         | 0.1      | 0.0  | 0.0       | 17.09 | 0.0   | 0.0        | 0.0  | 17.09       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 115.0         | 2.14     | 0    | 0         | 117.14 | 34.02 | 0          | 0    | 83.12       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 82.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 98.57        | true     | false    |
    When Customer undo "1"th "Chargeback" transaction made on "01 March 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 5 EUR transaction amount for Payment nr. 1
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 12.01 EUR transaction amount for Payment nr. 1
    When Customer undo "3"th "Chargeback" transaction made on "01 March 2024"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 82.05        | true     | false    |
      | 01 March 2024    | Chargeback       | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 98.57        | true     | false    |
      | 01 March 2024    | Chargeback       | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 72.05        | true     | false    |
      | 01 March 2024    | Chargeback       | 12.01  | 12.01     | 0.0      | 0.0  | 0.0       | 79.06        | false    | true     |

  @TestRailId:C3449
  Scenario: Full chargeback before the maturity date on different business date with interest recalculation ENABLED - Allocation priority: interest, fees, principal - UC5
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "1 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
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
      | 100           | 2.05     | 0    | 0         | 102.05 | 34.02 | 0          | 0    | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
# --- full chargeback for 2nd repayment made on  March, 1st --- #
    When Admin sets the business date to "15 March 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.48           | 33.09         | 0.93     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.76           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.95           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0   | 0.0        | 0.0  | 17.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 116.52        | 2.59     | 0    | 0         | 119.11 | 34.02 | 0          | 0    | 85.09       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 15 March 2024    | Chargeback       | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 83.57        | false    | false    |

  @TestRailId:C3450
  Scenario: Full chargeback after the maturity date with interest recalculation ENABLED - Allocation priority: interest, fees, principal - UC6
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 May 2024"
    And Customer makes "AUTOPAY" repayment on "01 May 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 June 2024"
    And Customer makes "AUTOPAY" repayment on "01 June 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 July 2024"
    And Customer makes "AUTOPAY" repayment on "01 July 2024" with 17 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024     | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 July 2024     | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 17.0  | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 102.05 | 0          | 0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 50.43        | false    | false    |
      | 01 May 2024      | Repayment        | 17.01  | 16.72     | 0.29     | 0.0  | 0.0       | 33.71        | false    | false    |
      | 01 June 2024     | Repayment        | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 16.9         | false    | false    |
      | 01 July 2024     | Repayment        | 17.0   | 16.9      | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 July 2024     | Accrual          | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
# --- full chargeback for 5th repayment made on  June, 1st --- #
    When Admin sets the business date to "15 July 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 5
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024     | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 July 2024     | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 17.0  | 0.0        | 0.0  | 0.0         |
      | 7  | 14   | 15 July 2024     |                  | 0.0             | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 116.81        | 2.25     | 0    | 0         | 119.06 | 102.05 | 0          | 0    | 17.01       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 50.43        | false    | false    |
      | 01 May 2024      | Repayment        | 17.01  | 16.72     | 0.29     | 0.0  | 0.0       | 33.71        | false    | false    |
      | 01 June 2024     | Repayment        | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 16.9         | false    | false    |
      | 01 July 2024     | Repayment        | 17.0   | 16.9      | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 July 2024     | Accrual          | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 July 2024     | Chargeback       | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 16.81        | false    | false    |
    And Admin does charge-off the loan on "15 July 2024"
    Then Loan Details response contains chargedOff flag set to true
    When Customer undo "1"th "Chargeback" transaction made on "15 July 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024     | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 July 2024     | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 17.0  | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 102.05 | 0          | 0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 50.43        | false    | false    |
      | 01 May 2024      | Repayment        | 17.01  | 16.72     | 0.29     | 0.0  | 0.0       | 33.71        | false    | false    |
      | 01 June 2024     | Repayment        | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 16.9         | false    | false    |
      | 01 July 2024     | Repayment        | 17.0   | 16.9      | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 July 2024     | Accrual          | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 July 2024     | Chargeback       | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 16.81        | true     | false    |
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan closedon_date is "01 July 2024"
    And Loan Details response does not contain chargedOff flag and chargedOffOnDate field after repayment and reverted charge off

  @TestRailId:C3451
  Scenario: Two chargebacks after the maturity date with interest recalculation ENABLED - Allocation priority: interest, fees, principal - UC7
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 May 2024"
    And Customer makes "AUTOPAY" repayment on "01 May 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 June 2024"
    And Customer makes "AUTOPAY" repayment on "01 June 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 July 2024"
    And Customer makes "AUTOPAY" repayment on "01 July 2024" with 17 EUR transaction amount
# --- full chargeback for 5th repayment made on  June, 1st --- #
    When Admin sets the business date to "15 July 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 5
# --- full chargeback for 6th repayment made on  July, 1st --- #
    When Admin sets the business date to "30 July 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17 EUR transaction amount for Payment nr. 6
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024     | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 July 2024     | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 17.0  | 0.0        | 0.0  | 0.0         |
      | 7  | 29   | 30 July 2024     |                  | 0.0             | 33.71         | 0.3      | 0.0  | 0.0       | 34.01 | 0.0   | 0.0        | 0.0  | 34.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 133.71        | 2.35     | 0    | 0         | 136.06 | 102.05 | 0          | 0    | 34.01       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 50.43        | false    | false    |
      | 01 May 2024      | Repayment        | 17.01  | 16.72     | 0.29     | 0.0  | 0.0       | 33.71        | false    | false    |
      | 01 June 2024     | Repayment        | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 16.9         | false    | false    |
      | 01 July 2024     | Repayment        | 17.0   | 16.9      | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 July 2024     | Accrual          | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 July 2024     | Chargeback       | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 16.81        | false    | false    |
      | 30 July 2024     | Chargeback       | 17.0   | 16.9      | 0.1      | 0.0  | 0.0       | 33.71        | false    | false    |

  @TestRailId:C3452
  Scenario: Chargeback on the last installment due date with interest recalculation ENABLED - Allocation priority: interest, fees, principal - UC8
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_INTEREST_PENALTY_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 May 2024"
    And Customer makes "AUTOPAY" repayment on "01 May 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 June 2024"
    And Customer makes "AUTOPAY" repayment on "01 June 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 July 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024     | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 85.05  | 0          | 0    | 17.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 50.43        | false    | false    |
      | 01 May 2024      | Repayment        | 17.01  | 16.72     | 0.29     | 0.0  | 0.0       | 33.71        | false    | false    |
      | 01 June 2024     | Repayment        | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 16.9         | false    | false    |
# --- full chargeback for 5th repayment made on  June, 1st --- #
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 5
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 June 2024     | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 33.71         | 0.3      | 0.0  | 0.0       | 34.01 | 0.0   | 0.0        | 0.0  | 34.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 116.81        | 2.25     | 0    | 0         | 119.06 | 85.05  | 0          | 0    | 34.01       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 50.43        | false    | false    |
      | 01 May 2024      | Repayment        | 17.01  | 16.72     | 0.29     | 0.0  | 0.0       | 33.71        | false    | false    |
      | 01 June 2024     | Repayment        | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 16.9         | false    | false    |
      | 01 July 2024     | Chargeback       | 17.01  | 16.81     | 0.2      | 0.0  | 0.0       | 33.71        | false    | false    |

  @TestRailId:C3453
  Scenario: Prepay loan after few installments repay and chargeback with interest recalculation ENABLED - Allocation priority: interest, fees, principal - UC9
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 17.01 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 3
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      |                  | 33.81           | 33.24         | 0.78     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.1      | 0.0  | 0.0       | 17.1  | 0.0   | 0.0        | 0.0  | 17.1        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 116.62        | 2.54     | 0    | 0         | 119.16 | 51.03  | 0          | 0    | 68.13       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 50.43        | false    | false    |
   #   | 01 July 2024     | Accrual          | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      |  01 April 2024   | Chargeback       | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 67.05        | false    | false    |
# --- PayOff loan with prepay amount to close a loan --- #
    When Loan Pay-off is made on "01 April 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 April 2024    | 33.42           | 33.63         | 0.39     | 0.0  | 0.0       | 34.02 | 34.02 | 34.02      | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 April 2024    | 16.41           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 April 2024    | 0.0             | 16.41         | 0.0      | 0.0  | 0.0       | 16.41 | 16.41 | 16.41      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 116.62        | 1.85     | 0    | 0         | 118.47 | 118.47 | 67.44      | 0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 50.43        | false    | false    |
      | 01 April 2024    | Chargeback       | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Repayment        | 67.44  | 67.05     | 0.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual          | 1.46   | 0.0       | 1.46     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan closedon_date is "01 April 2024"
    When Customer undo "1"th "Chargeback" transaction made on "01 April 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 April 2024    | 33.42           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     | 01 April 2024    | 16.41           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2024     | 01 April 2024    | 0.0             | 16.41         | 0.0      | 0.0  | 0.0       | 16.41 | 16.41 | 16.41      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 100           | 1.46     | 0    | 0         | 101.46 | 101.46 | 50.43      | 0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 April 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 50.43        | false    | false    |
      | 01 April 2024    | Chargeback       | 17.01  | 16.62     | 0.39     | 0.0  | 0.0       | 67.05        | true     | false    |
      | 01 April 2024    | Repayment        | 67.44  | 50.43     | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 01 April 2024    | Accrual          | 1.46   | 0.0       | 1.46     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan status will be "OVERPAID"
    Then Loan has 17.01 overpaid amount

  @TestRailId:C3454
  Scenario: Full chargeback before the maturity date with run COB and interest recalculation ENABLED - Allocation priority: interest, fees, principal - UC10
    When Admin sets the business date to "2 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_INTEREST_PENALTY_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0     |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0   | 0.0       | 17.01   | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72	       | 0.29     | 0.0   | 0.0       | 17.01	| 0.0  | 0.0        | 0.0  | 17.01	     |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0   | 0.0       | 17.01	| 0.0  | 0.0        | 0.0  | 17.01	     |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0   | 0.0       | 17.0 	| 0.0  | 0.0        | 0.0  | 17.0 	     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05  | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "1 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0     | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0   | 0.0       | 17.01   | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0   | 0.0       | 17.01   | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0   | 0.0       | 17.01   | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72	      | 0.29     | 0.0   | 0.0       | 17.01   | 0.0   | 0.0        | 0.0  | 17.01	     |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0   | 0.0       | 17.01   | 0.0   | 0.0        | 0.0  | 17.01	     |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0   | 0.0       | 17.0    | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05  | 17.01 | 0          | 0    | 85.04     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount  | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement      | 100.0   | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment         | 17.01   | 16.43     | 0.58     | 0.0   | 0.0       | 83.57        | false    | false    |
# --- full chargeback for the repayment made on February, 1st --- #
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.14           | 32.86         | 1.16     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 3  | 31   | 01 April 2024    |                  | 50.52           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.8            | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.99           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.99         | 0.1      | 0.0  | 0.0       | 17.09 | 0.0   | 0.0        | 0.0  | 17.09       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 116.43        | 2.72     | 0    | 0         | 119.15 | 17.01 | 0          | 0    | 102.14      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 February 2024 | Chargeback       | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 100.0        | false    | false    |
    When Admin sets the business date to "2 February 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.14           | 32.86         | 1.16     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 3  | 31   | 01 April 2024    |                  | 50.52           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.8            | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.99           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.99         | 0.1      | 0.0  | 0.0       | 17.09 | 0.0   | 0.0        | 0.0  | 17.09       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 116.43        | 2.72     | 0    | 0         | 119.15 | 17.01 | 0          | 0    | 102.14      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 02 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual          | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 February 2024 | Chargeback       | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "12 February 2024"
    When Admin runs inline COB job for Loan
    When Customer undo "1"th "Chargeback" transaction made on "01 February 2024"
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
      | 100           | 2.05     | 0    | 0         | 102.05 | 17.01 | 0          | 0    | 85.04       |
    When Admin sets the business date to "13 February 2024"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 02 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual            | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Repayment          | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 February 2024 | Chargeback         | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 100.0        | true     | false    |
      | 01 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Accrual            | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Accrual Adjustment | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C110993
  Scenario: Repayment undo with linked chargeback fails after backdated repayment and undo of former partial chargeback with interest recalculation ENABLED - Allocation priority: interest, fees, principal
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "10 February 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 5 EUR transaction amount for Payment nr. 1
    When Admin sets the business date to "15 February 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 12.01 EUR transaction amount for Payment nr. 1
    When Customer makes "AUTOPAY" repayment on "15 January 2024" with 10 EUR transaction amount
    Then Loan Transactions tab has a transaction with date: "01 February 2024", and with the following data:
      | Transaction Type | Amount | Reverted | Replayed |
      | Repayment        | 17.01  | false    | true     |
    Then Loan Transactions tab has a transaction with date: "15 February 2024", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Reverted | Replayed |
      | Chargeback       | 12.01  | 12.01     | 0.0      | false    | false    |
    When Customer undo "1"th "Chargeback" transaction made on "10 February 2024"
    Then Loan Transactions tab has a transaction with date: "15 February 2024", and with the following data:
      | Transaction Type | Amount | Reverted | Replayed |
      | Chargeback       | 12.01  | false    | true     |
    Then Customer undo "1"th transaction made on "01 February 2024" results a 403 error and "update not allowed as loan transaction is linked to other transactions" error message

  @TestRailId:C4015
  Scenario: Verify early repayment with MIR and chargeback afterwards for 24m progressive loan
    When Admin sets the business date to "11 April 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_ZERO_CHARGE_OFF | 11 April 2025     | 209.72         | 12.25                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 24                | MONTHS                | 1              | MONTHS                 | 24                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "11 April 2025" with "209.72" amount and expected disbursement date on "11 April 2025"
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date                 | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 11 April 2025        |                  | 209.72          |               |          | 0.0  |           | 0.0   |       |            |      | 0.0         |
      | 1  | 30   | 11 May 2025          |                  | 201.96          |  7.76         |  2.14    | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 2  | 31   | 11 June 2025         |                  | 194.12          |  7.84         |  2.06    | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 3  | 30   | 11 July 2025         |                  | 186.2           |  7.92         |  1.98    | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 4  | 31   | 11 August 2025       |                  | 178.2           |  8.0          |  1.9     | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 5  | 31   | 11 September 2025    |                  | 170.12          |  8.08         |  1.82    | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 6  | 30   | 11 October 2025      |                  | 161.96          |  8.16         |  1.74    | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 7  | 31   | 11 November 2025     |                  | 153.71          |  8.25         |  1.65    | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 8  | 30   | 11 December 2025     |                  | 145.38          |  8.33         |  1.57    | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 9  | 31   | 11 January 2026      |                  | 136.96          |  8.42         |  1.48    | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 10 | 31   | 11 February 2026     |                  | 128.46          |  8.5          |  1.4     | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 11 | 28   | 11 March 2026        |                  | 119.87          |  8.59         |  1.31    | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 12 | 31   | 11 April 2026        |                  | 111.19          |  8.68         |  1.22    | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 13 | 30   | 11 May 2026          |                  | 102.43          |  8.76         |  1.14    | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 14 | 31   | 11 June 2026         |                  |  93.58          |  8.85         |  1.05    | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 15 | 30   | 11 July 2026         |                  |  84.64          |  8.94         |  0.96    | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 16 | 31   | 11 August 2026       |                  |  75.6           |  9.04         |  0.86    | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 17 | 31   | 11 September 2026    |                  |  66.47          |  9.13         |  0.77    | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 18 | 30   | 11 October 2026      |                  |  57.25          |  9.22         |  0.68    | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 19 | 31   | 11 November 2026     |                  |  47.93          |  9.32         |  0.58    | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 20 | 30   | 11 December 2026     |                  |  38.52          |  9.41         |  0.49    | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 21 | 31   | 11 January 2027      |                  |  29.01          |  9.51         |  0.39    | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 22 | 31   | 11 February 2027     |                  |  19.41          |  9.6          |  0.3     | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 23 | 28   | 11 March 2027        |                  |   9.71          |  9.7          |  0.2     | 0.0  | 0.0       | 9.9   | 0.0   | 0.0        | 0.0  | 9.9         |
      | 24 | 31   | 11 April 2027        |                  |   0.0           |  9.71         |  0.1     | 0.0  | 0.0       | 9.81  | 0.0   | 0.0        | 0.0  | 9.81        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 209.72        | 27.79    | 0.0  | 0.0       | 237.51 |  0.0 | 0.0        | 0.0  | 237.51      |

    When Admin successfully disburse the loan on "11 April 2025" with "209.72" EUR transaction amount
    When Admin sets the business date to "11 May 2025"
    And Customer makes "AUTOPAY" repayment on "11 May 2025" with 9.9 EUR transaction amount
    When Admin sets the business date to "11 June 2025"
    And Customer makes "AUTOPAY" repayment on "11 June 2025" with 9.9 EUR transaction amount
    When Admin sets the business date to "11 July 2025"
    And Customer makes "AUTOPAY" repayment on "11 July 2025" with 9.9 EUR transaction amount
    When Admin sets the business date to "11 August 2025"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "11 August 2025" with 9.9 EUR transaction amount
    When Admin sets the business date to "13 August 2025"
    When Admin runs inline COB job for Loan
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "13 August 2025" with 188.8 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "15 August 2025"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date                 | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 11 April 2025        |                  | 209.72          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 11 May 2025          | 11 May 2025      | 201.96          |  7.76         |  2.14    | 0.0  | 0.0       | 9.9   | 9.9   | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 11 June 2025         | 11 June 2025     | 194.12          |  7.84         |  2.06    | 0.0  | 0.0       | 9.9   | 9.9   | 0.0        | 0.0  | 0.0         |
      | 3  | 30   | 11 July 2025         | 11 July 2025     | 186.2           |  7.92         |  1.98    | 0.0  | 0.0       | 9.9   | 9.9   | 0.0        | 0.0  | 0.0         |
      | 4  | 31   | 11 August 2025       | 11 August 2025   | 178.2           |  8.0          |  1.9     | 0.0  | 0.0       | 9.9   | 9.9   | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 11 September 2025    | 13 August 2025   | 178.2           |  0.0          |  0.12    | 0.0  | 0.0       | 0.12  | 0.12  | 0.12       | 0.0  | 0.0         |
      | 6  | 30   | 11 October 2025      | 13 August 2025   | 178.2           |  0.0          |  0.0     | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 7  | 31   | 11 November 2025     | 13 August 2025   | 168.3           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 8  | 30   | 11 December 2025     | 13 August 2025   | 158.4           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 9  | 31   | 11 January 2026      | 13 August 2025   | 148.5           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 10 | 31   | 11 February 2026     | 13 August 2025   | 138.6           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 11 | 28   | 11 March 2026        | 13 August 2025   | 128.7           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 12 | 31   | 11 April 2026        | 13 August 2025   | 118.8           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 13 | 30   | 11 May 2026          | 13 August 2025   | 108.9           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 14 | 31   | 11 June 2026         | 13 August 2025   |  99.0           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 15 | 30   | 11 July 2026         | 13 August 2025   |  89.1           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 16 | 31   | 11 August 2026       | 13 August 2025   |  79.2           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 17 | 31   | 11 September 2026    | 13 August 2025   |  69.3           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 18 | 30   | 11 October 2026      | 13 August 2025   |  59.4           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 19 | 31   | 11 November 2026     | 13 August 2025   |  49.5           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 20 | 30   | 11 December 2026     | 13 August 2025   |  39.6           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 21 | 31   | 11 January 2027      | 13 August 2025   |  29.7           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 22 | 31   | 11 February 2027     | 13 August 2025   |  19.8           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 23 | 28   | 11 March 2027        | 13 August 2025   |   9.9           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 24 | 31   | 11 April 2027        | 13 August 2025   |   0.0           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 209.72        | 8.2      | 0.0  | 0.0       | 217.92 | 217.92 | 178.32     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance  | Reverted | Replayed |
      | 11 April 2025    | Disbursement           | 209.72  | 0.0       | 0.0      | 0.0  | 0.0       | 209.72        | false    | false    |
      | 11 May 2025      | Repayment              | 9.9     | 7.76      | 2.14     | 0.0  | 0.0       | 201.96        | false    | false    |
      | 11 May 2025      | Accrual Activity       | 2.14    | 0.0       | 2.14     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 11 June 2025     | Repayment              | 9.9     | 7.84      | 2.06     | 0.0  | 0.0       | 194.12        | false    | false    |
      | 11 June 2025     | Accrual Activity       | 2.06    | 0.0       | 2.06     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 11 July 2025     | Repayment              | 9.9     | 7.92      | 1.98     | 0.0  | 0.0       | 186.2         | false    | false    |
      | 11 July 2025     | Accrual Activity       | 1.98    | 0.0       | 1.98     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 10 August 2025   | Accrual                | 8.02    | 0.0       | 8.02     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 11 August 2025   | Repayment              | 9.9     | 8.0       | 1.9      | 0.0  | 0.0       | 178.2         | false    | false    |
      | 11 August 2025   | Accrual                | 0.06    | 0.0       | 0.06     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 11 August 2025   | Accrual Activity       | 1.9     | 0.0       | 1.9      | 0.0  | 0.0       | 0.0           | false    | false    |
      | 12 August 2025   | Accrual                | 0.06    | 0.0       | 0.06     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 13 August 2025   | Merchant Issued Refund | 188.8   | 178.2     | 0.12     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 13 August 2025   | Interest Refund        | 7.87    | 0.0       | 0.0      | 0.0  | 0.0       | 0.0           | false    | false    |
      | 13 August 2025   | Accrual                | 0.06    | 0.0       | 0.06     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 13 August 2025   | Accrual Activity       | 0.12    | 0.0       | 0.12     | 0.0  | 0.0       | 0.0           | false    | false    |

# --- make this reversal of repayment --- #
    When Customer undo "1"th "Repayment" transaction made on "11 August 2025"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 15 EUR transaction amount for MIR nr. 1
    When Admin sets the business date to "16 August 2025"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date                 | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 11 April 2025        |                  | 209.72          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 11 May 2025          | 11 May 2025      | 201.96          |  7.76         |  2.14    | 0.0  | 0.0       | 9.9   | 9.9   | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 11 June 2025         | 11 June 2025     | 194.12          |  7.84         |  2.06    | 0.0  | 0.0       | 9.9   | 9.9   | 0.0        | 0.0  | 0.0         |
      | 3  | 30   | 11 July 2025         | 11 July 2025     | 186.2           |  7.92         |  1.98    | 0.0  | 0.0       | 9.9   | 9.9   | 0.0        | 0.0  | 0.0         |
      | 4  | 31   | 11 August 2025       | 13 August 2025   | 178.2           |  8.0          |  1.9     | 0.0  | 0.0       | 9.9   | 9.9   | 0.0        | 9.9  | 0.0         |
      | 5  | 31   | 11 September 2025    |                  | 178.2           |  15.0         |  0.18    | 0.0  | 0.0       | 15.18 | 8.57  | 8.57       | 0.0  | 6.61        |
      | 6  | 30   | 11 October 2025      | 13 August 2025   | 178.2           |  0.0          |  0.0     | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 7  | 31   | 11 November 2025     | 13 August 2025   | 168.3           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 8  | 30   | 11 December 2025     | 13 August 2025   | 158.4           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 9  | 31   | 11 January 2026      | 13 August 2025   | 148.5           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 10 | 31   | 11 February 2026     | 13 August 2025   | 138.6           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 11 | 28   | 11 March 2026        | 13 August 2025   | 128.7           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 12 | 31   | 11 April 2026        | 13 August 2025   | 118.8           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 13 | 30   | 11 May 2026          | 13 August 2025   | 108.9           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 14 | 31   | 11 June 2026         | 13 August 2025   |  99.0           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 15 | 30   | 11 July 2026         | 13 August 2025   |  89.1           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 16 | 31   | 11 August 2026       | 13 August 2025   |  79.2           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 17 | 31   | 11 September 2026    | 13 August 2025   |  69.3           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 18 | 30   | 11 October 2026      | 13 August 2025   |  59.4           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 19 | 31   | 11 November 2026     | 13 August 2025   |  49.5           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 20 | 30   | 11 December 2026     | 13 August 2025   |  39.6           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 21 | 31   | 11 January 2027      | 13 August 2025   |  29.7           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 22 | 31   | 11 February 2027     | 13 August 2025   |  19.8           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 23 | 28   | 11 March 2027        | 13 August 2025   |   9.9           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
      | 24 | 31   | 11 April 2027        | 13 August 2025   |   0.0           |  9.9          |  0.0     | 0.0  | 0.0       | 9.9   | 9.9   | 9.9        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 224.72        | 8.26     | 0.0  |  0.0      | 232.98 | 226.37 | 186.77     | 9.9  | 6.61        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance  | Reverted | Replayed |
      | 11 April 2025    | Disbursement           | 209.72  | 0.0       | 0.0      | 0.0  | 0.0       | 209.72        | false    | false    |
      | 11 May 2025      | Repayment              | 9.9     | 7.76      | 2.14     | 0.0  | 0.0       | 201.96        | false    | false    |
      | 11 May 2025      | Accrual Activity       | 2.14    | 0.0       | 2.14     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 11 June 2025     | Repayment              | 9.9     | 7.84      | 2.06     | 0.0  | 0.0       | 194.12        | false    | false    |
      | 11 June 2025     | Accrual Activity       | 2.06    | 0.0       | 2.06     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 11 July 2025     | Repayment              | 9.9     | 7.92      | 1.98     | 0.0  | 0.0       | 186.2         | false    | false    |
      | 11 July 2025     | Accrual Activity       | 1.98    | 0.0       | 1.98     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 10 August 2025   | Accrual                | 8.02    | 0.0       | 8.02     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 11 August 2025   | Repayment              | 9.9     | 8.0       | 1.9      | 0.0  | 0.0       | 178.2         | true     | false    |
      | 11 August 2025   | Accrual                | 0.06    | 0.0       | 0.06     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 11 August 2025   | Accrual Activity       | 1.9     | 0.0       | 1.9      | 0.0  | 0.0       | 0.0           | false    | false    |
      | 12 August 2025   | Accrual                | 0.06    | 0.0       | 0.06     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 13 August 2025   | Accrual                | 0.06    | 0.0       | 0.06     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 13 August 2025   | Merchant Issued Refund | 188.8   | 186.2     | 2.02     | 0.0  | 0.0       | 0.0           | false    | true     |
      | 13 August 2025   | Interest Refund        | 7.87    | 0.0       | 0.0      | 0.0  | 0.0       | 0.0           | false    | true     |
      | 15 August 2025   | Chargeback             | 15.0    | 15.0      | 0.0      | 0.0  | 0.0       | 6.55          | false    | false    |

  @TestRailId:C4677 @AdvancedPaymentAllocation
  Scenario: Verify changedTerms value for LoanDisbursalTransactionBusinessEvent with n+1 trm for interest bearing multidisb loan - backdated 2nd disb with chargeback after maturity date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_CHARGEBACK | 01 January 2024   | 150            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "150" amount and expected disbursement date on "01 January 2024"
    When Admin disburses the loan on "01 January 2024" with "100" EUR transaction amount
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
# --- add chargeback after maturity date - n+1 trn -- #
    When Admin sets the business date to "01 August 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.53           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 34.01           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.49           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.49         | 0.49     | 0.0  | 0.0       | 17.98 | 0.0   | 0.0        | 0.0  | 17.98       |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 116.43        | 3.61     | 0.0  | 0.0       | 120.04 | 17.01 | 0.0        | 0.0  | 103.03      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 August 2024   | Chargeback       | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 100.0        | false    | false    |
# --- add backdated 2nd disbursement --- #
    When Admin disburses the loan on "20 January 2024" with "50" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      |    |      | 20 January 2024  |                  |  50.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |                  | 125.22          | 24.78         | 0.7      | 0.0  | 0.0       | 25.48 | 17.01 | 0.0        | 0.0  | 8.47        |
      | 2  | 29   | 01 March 2024    |                  | 100.52          | 24.7          | 0.78     | 0.0  | 0.0       | 25.48 | 0.0   | 0.0        | 0.0  | 25.48       |
      | 3  | 31   | 01 April 2024    |                  | 75.82           | 24.7          | 0.78     | 0.0  | 0.0       | 25.48 | 0.0   | 0.0        | 0.0  | 25.48       |
      | 4  | 30   | 01 May 2024      |                  | 51.12           | 24.7          | 0.78     | 0.0  | 0.0       | 25.48 | 0.0   | 0.0        | 0.0  | 25.48       |
      | 5  | 31   | 01 June 2024     |                  | 26.42           | 24.7          | 0.78     | 0.0  | 0.0       | 25.48 | 0.0   | 0.0        | 0.0  | 25.48       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 26.42         | 0.78     | 0.0  | 0.0       | 27.2  | 0.0   | 0.0        | 0.0  | 27.2        |
      | 7  | 31   | 01 August 2024   |                  | 0.0             | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 167.01        | 4.6      | 0.0  | 0.0       | 171.61 | 17.01 | 0.0        | 0.0  | 154.6      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 20 January 2024  | Disbursement     | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 132.99       | false    | true     |
      | 01 August 2024   | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 150.0        | false    | true     |
    Then LoanDisbursalTransactionBusinessEvent has changedTerms "false"
    When Loan Pay-off is made on "01 August 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  # Reproduces BE ticket "[BE] PL - Chargeback interest Handling Improvements":
  # after a chargeback, the reinstated interest portion must NOT be re-added to
  # Accrual Activity, since it was already considered when the transaction was
  # originally accrued and paid.
  @TestRailId:C110988
  Scenario: Verify Accrual Activity excludes chargeback interest and matches period accruals
    When Admin sets the business date to "01 August 2026"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_INTEREST_RECALC_CHARGEBACK_ALLOCATION_INTEREST_FIRST_ACCRUAL_ACTIVITY | 01 August 2026    | 1000           | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 August 2026" with "1000" amount and expected disbursement date on "01 August 2026"
    And Admin successfully disburse the loan on "01 August 2026" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 August 2026    |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 September 2026 |           | 669.42          | 330.58        | 8.32     | 0.0  | 0.0       | 338.9  | 0.0  | 0.0        | 0.0  | 338.9       |
      | 2  | 30   | 01 October 2026   |           | 336.09          | 333.33        | 5.57     | 0.0  | 0.0       | 338.9  | 0.0  | 0.0        | 0.0  | 338.9       |
      | 3  | 31   | 01 November 2026  |           | 0.0             | 336.09        | 2.8      | 0.0  | 0.0       | 338.89 | 0.0  | 0.0        | 0.0  | 338.89      |
    # --- Repayment of period 1 (full interest 8.32 paid) ---
    When Admin sets the business date to "23 September 2026"
    And Customer makes "AUTOPAY" repayment on "23 September 2026" with 340 EUR transaction amount
    Then Loan Transactions tab has a transaction with date: "23 September 2026", and with the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 23 September 2026 | Repayment        | 340.0  | 331.68    | 8.32     | 0.0  | 0.0       | 668.32       | false    | false    |
    # --- Chargeback reinstates the period 1 interest (8.32); this is the portion that must be
    # --- excluded from Accrual Activity going forward since it was already accrued/paid once ---
    When Admin sets the business date to "24 September 2026"
    And Admin runs inline COB job for Loan
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 340 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 August 2026    |                   | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 31   | 01 September 2026 | 23 September 2026 | 669.42          | 330.58        | 8.32     | 0.0  | 0.0       | 338.9  | 338.9 | 0.0        | 338.9 | 0.0         |
      | 2  | 30   | 01 October 2026   |                   | 338.75          | 662.35        | 16.55    | 0.0  | 0.0       | 678.9  | 1.1   | 1.1        | 0.0   | 677.8       |
      | 3  | 31   | 01 November 2026  |                   | 0.0             | 338.75        | 2.82     | 0.0  | 0.0       | 341.57 | 0.0   | 0.0        | 0.0   | 341.57      |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 August 2026    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 September 2026 | Accrual Activity | 8.32   | 0.0       | 8.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 September 2026 | Repayment        | 340.0  | 331.68    | 8.32     | 0.0  | 0.0       | 668.32       | false    | false    |
      | 23 September 2026 | Accrual          | 14.42  | 0.0       | 14.42    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 September 2026 | Chargeback       | 340.0  | 331.68    | 8.32     | 0.0  | 0.0       | 1000.0       | false    | false    |
    # --- Run COB past the end of period 2: the final "Accrual Activity" for the period must be
    # --- 8.23 (total accrued amount of the period minus total accrual adjustment of the period),
    # --- NOT 16.55 (period 2 due interest), which would mean the chargeback interest (8.32) was
    # --- wrongly re-added - this is the core regression check for this ticket ---
    When Admin sets the business date to "02 October 2026"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 August 2026    |                   | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 31   | 01 September 2026 | 23 September 2026 | 669.42          | 330.58        | 8.32     | 0.0  | 0.0       | 338.9  | 338.9 | 0.0        | 338.9 | 0.0         |
      | 2  | 30   | 01 October 2026   |                   | 338.75          | 662.35        | 16.55    | 0.0  | 0.0       | 678.9  | 1.1   | 1.1        | 0.0   | 677.8       |
      | 3  | 31   | 01 November 2026  |                   | 0.0             | 338.75        | 3.0      | 0.0  | 0.0       | 341.75 | 0.0   | 0.0        | 0.0   | 341.75      |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 August 2026    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 September 2026 | Accrual Activity | 8.32   | 0.0       | 8.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 September 2026 | Repayment        | 340.0  | 331.68    | 8.32     | 0.0  | 0.0       | 668.32       | false    | false    |
      | 23 September 2026 | Accrual          | 14.42  | 0.0       | 14.42    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 September 2026 | Chargeback       | 340.0  | 331.68    | 8.32     | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 24 September 2026 | Accrual          | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 September 2026 | Accrual          | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 September 2026 | Accrual          | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 September 2026 | Accrual          | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 September 2026 | Accrual          | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 September 2026 | Accrual          | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 September 2026 | Accrual          | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 October 2026   | Accrual          | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 October 2026   | Accrual Activity | 8.23   | 0.0       | 8.23     | 0.0  | 0.0       | 0.0          | false    | false    |
    # --- Closure Accrual Activity must hold only the remaining period 3 interest (0.27), NOT 8.59,
    # --- which would mean the chargeback interest (8.32) was re-added on loan closure ---
    When Loan Pay-off is made on "02 October 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    Then Loan Transactions tab has a transaction with date: "02 October 2026", and with the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 02 October 2026  | Accrual Activity | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |

  # Reproduces BE ticket "[BE] PL - Chargeback interest Handling Improvements" for the
  # interest-recalculation-DISABLED path: the chargeback is booked on the first installment due
  # after the chargeback date (period 2), so the reinstated (credited) interest is added to that
  # installment's interest and must NOT be included in its Accrual Activity.
  @TestRailId:C110989
  Scenario: Verify Accrual Activity excludes chargeback interest and matches period accruals - UC2: interest recalculation disabled
    When Admin sets the business date to "01 August 2026"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_INTEREST_FIRST_ACCRUAL_ACTIVITY | 01 August 2026    | 1000           | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 August 2026" with "1000" amount and expected disbursement date on "01 August 2026"
    And Admin successfully disburse the loan on "01 August 2026" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 August 2026    |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 September 2026 |           | 669.42          | 330.58        | 8.32     | 0.0  | 0.0       | 338.9  | 0.0  | 0.0        | 0.0  | 338.9       |
      | 2  | 30   | 01 October 2026   |           | 336.09          | 333.33        | 5.57     | 0.0  | 0.0       | 338.9  | 0.0  | 0.0        | 0.0  | 338.9       |
      | 3  | 31   | 01 November 2026  |           | 0.0             | 336.09        | 2.8      | 0.0  | 0.0       | 338.89 | 0.0  | 0.0        | 0.0  | 338.89      |
    # --- Repayment of period 1 on its due date (full interest 8.32 paid) ---
    When Admin sets the business date to "01 September 2026"
    And Customer makes "AUTOPAY" repayment on "01 September 2026" with 338.9 EUR transaction amount
    Then Loan Transactions tab has a transaction with date: "01 September 2026", and with the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 September 2026 | Repayment        | 338.9  | 330.58    | 8.32     | 0.0  | 0.0       | 669.42       | false    | false    |
    # --- COB (run on the next business day) posts period 1's Accrual Activity for 01 September,
    # --- full interest 8.32 ---
    When Admin sets the business date to "02 September 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has a transaction with date: "01 September 2026", and with the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 September 2026 | Accrual Activity | 8.32   | 0.0       | 8.32     | 0.0  | 0.0       | 0.0          | false    | false    |
    # --- Chargeback on 02 September reinstates the period 1 interest (8.32); it is booked on period 2
    # --- (first installment due after the chargeback date), period 1 and its Accrual Activity stay untouched ---
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 338.9 EUR transaction amount for Payment nr. 1
    When Admin sets the business date to "03 September 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has a transaction with date: "01 August 2026", and with the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 August 2026   | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    Then Loan Transactions tab has a transaction with date: "01 September 2026", and with the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 September 2026 | Accrual Activity | 8.32   | 0.0       | 8.32     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a transaction with date: "02 September 2026", and with the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 02 September 2026 | Chargeback       | 338.9  | 330.58    | 8.32     | 0.0  | 0.0       | 1000.0       | false    | false    |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date            | Principal due | Interest | Due   |
      | 2  | 30   | 01 October 2026 | 663.91        | 13.89    | 677.8 |
    # --- Run COB past the end of period 2: its Accrual Activity must be 5.57 (its own interest),
    # --- NOT 13.89 (period 2 due interest incl. the 8.32 chargeback interest) - this is the core
    # --- regression check for the recalc-disabled path ---
    When Admin sets the business date to "02 October 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has a transaction with date: "01 October 2026", and with the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 October 2026  | Accrual Activity | 5.57   | 0.0       | 5.57     | 0.0  | 0.0       | 0.0          | false    | false    |
    # --- Closure Accrual Activity must hold only the remaining period 3 interest (2.8), NOT 11.12,
    # --- which would mean the chargeback interest (8.32) was re-added on loan closure ---
    When Loan Pay-off is made on "02 October 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    Then Loan Transactions tab has a transaction with date: "02 October 2026", and with the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 02 October 2026  | Accrual Activity | 2.8    | 0.0       | 2.8      | 0.0  | 0.0       | 0.0          | false    | false    |

  # Reproduces with an accrual
  # adjustment in the period: a backdated repayment after the chargeback lowers the period 2 interest,
  # so the Accrual Activity must equal the total accrued amount of the period minus the total accrual
  # adjustment of the period, excluding the chargeback interest.
  @TestRailId:C111042
  Scenario: Verify Accrual Activity excludes chargeback interest and matches period accruals minus accrual adjustment - UC3: backdated repayment
    When Admin sets the business date to "01 August 2026"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_INTEREST_RECALC_CHARGEBACK_ALLOCATION_INTEREST_FIRST_ACCRUAL_ACTIVITY | 01 August 2026    | 1000           | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 August 2026" with "1000" amount and expected disbursement date on "01 August 2026"
    And Admin successfully disburse the loan on "01 August 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "23 September 2026"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 August 2026    |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 September 2026 |           | 669.42          | 330.58        | 8.32     | 0.0  | 0.0       | 338.9  | 0.0  | 0.0        | 0.0  | 338.9       |
      | 2  | 30   | 01 October 2026   |           | 336.09          | 333.33        | 5.57     | 0.0  | 0.0       | 338.9  | 0.0  | 0.0        | 0.0  | 338.9       |
      | 3  | 31   | 01 November 2026  |           | 0.0             | 336.09        | 2.8      | 0.0  | 0.0       | 338.89 | 0.0  | 0.0        | 0.0  | 338.89      |
    And Customer makes "AUTOPAY" repayment on "23 September 2026" with 340 EUR transaction amount
    # --- Chargeback reinstates the period 1 interest (8.32) on period 2 ---
    When Admin sets the business date to "24 September 2026"
    And Admin runs inline COB job for Loan
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 340 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 August 2026    |                   | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 31   | 01 September 2026 | 23 September 2026 | 669.42          | 330.58        | 8.32     | 0.0  | 0.0       | 338.9  | 338.9 | 0.0        | 338.9 | 0.0         |
      | 2  | 30   | 01 October 2026   |                   | 338.75          | 662.35        | 16.55    | 0.0  | 0.0       | 678.9  | 1.1   | 1.1        | 0.0   | 677.8       |
      | 3  | 31   | 01 November 2026  |                   | 0.0             | 338.75        | 2.82     | 0.0  | 0.0       | 341.57 | 0.0   | 0.0        | 0.0   | 341.57      |
    # --- Backdated repayment after accruals were posted lowers the period 2 interest -> accrual adjustment ---
    When Admin sets the business date to "30 September 2026"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "25 September 2026" with 600 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 August 2026    |                   | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 31   | 01 September 2026 | 23 September 2026 | 669.42          | 330.58        | 8.32     | 0.0  | 0.0       | 338.9  | 338.9 | 0.0        | 338.9 | 0.0         |
      | 2  | 30   | 01 October 2026   |                   | 337.75          | 663.35        | 15.55    | 0.0  | 0.0       | 678.9  | 601.1 | 601.1      | 0.0   | 77.8        |
      | 3  | 31   | 01 November 2026  |                   | 0.0             | 337.75        | 2.81     | 0.0  | 0.0       | 340.56 | 0.0   | 0.0        | 0.0   | 340.56      |
    # --- Run COB past the end of period 2: the period 2 Accrual Activity must be 7.23 (16.11 total accrued
    # --- - 0.56 accrual adjustment - 8.32 period 1 accrued), NOT 15.55 (period 2 due interest incl. the
    # --- 8.32 chargeback interest) ---
    When Admin sets the business date to "02 October 2026"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 August 2026    |                   | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 31   | 01 September 2026 | 23 September 2026 | 669.42          | 330.58        | 8.32     | 0.0  | 0.0       | 338.9  | 338.9 | 0.0        | 338.9 | 0.0         |
      | 2  | 30   | 01 October 2026   |                   | 337.75          | 663.35        | 15.55    | 0.0  | 0.0       | 678.9  | 601.1 | 601.1      | 0.0   | 77.8        |
      | 3  | 31   | 01 November 2026  |                   | 0.0             | 337.75        | 2.83     | 0.0  | 0.0       | 340.58 | 0.0   | 0.0        | 0.0   | 340.58      |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 August 2026    | Disbursement       | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 September 2026 | Accrual Activity   | 8.32   | 0.0       | 8.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 September 2026 | Repayment          | 340.0  | 331.68    | 8.32     | 0.0  | 0.0       | 668.32       | false    | false    |
      | 23 September 2026 | Accrual            | 14.42  | 0.0       | 14.42    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 September 2026 | Chargeback         | 340.0  | 331.68    | 8.32     | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 24 September 2026 | Accrual            | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 September 2026 | Accrual            | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 September 2026 | Repayment          | 600.0  | 600.0     | 0.0      | 0.0  | 0.0       | 400.0        | false    | false    |
      | 26 September 2026 | Accrual            | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 September 2026 | Accrual            | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 September 2026 | Accrual            | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 September 2026 | Accrual            | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 September 2026 | Accrual Adjustment | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 October 2026   | Accrual            | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 October 2026   | Accrual Activity   | 7.23   | 0.0       | 7.23     | 0.0  | 0.0       | 0.0          | false    | false    |
    # --- Closure Accrual Activity must hold only the remaining period 3 interest (0.11), NOT 8.43,
    # --- which would mean the chargeback interest (8.32) was re-added on loan closure ---
    When Loan Pay-off is made on "02 October 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      |    |      | 01 August 2026    |                   | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |       |             |
      | 1  | 31   | 01 September 2026 | 23 September 2026 | 669.42          | 330.58        | 8.32     | 0.0  | 0.0       | 338.9  | 338.9  | 0.0        | 338.9 | 0.0         |
      | 2  | 30   | 01 October 2026   | 02 October 2026   | 337.75          | 663.35        | 15.55    | 0.0  | 0.0       | 678.9  | 678.9  | 601.1      | 77.8  | 0.0         |
      | 3  | 31   | 01 November 2026  | 02 October 2026   | 0.0             | 337.75        | 0.11     | 0.0  | 0.0       | 337.86 | 337.86 | 337.86     | 0.0   | 0.0         |
    Then Loan Transactions tab has a transaction with date: "02 October 2026", and with the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 02 October 2026  | Repayment        | 415.66 | 400.0     | 15.66    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 October 2026  | Accrual          | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 October 2026  | Accrual Activity | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
  @TestRailId:C110994
  Scenario: Verify chargeback can be made again for the full repayment amount after chargeback undo and excess chargeback is rejected
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                      | 0                       | 0                   | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 1
    When Customer undo "1"th "Chargeback" transaction made on "15 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0|           | 0.0 | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0 | 0.0        | 17.01 | 17.01 | 0.0       | 0.0   | 0.0       |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0           | 16.9          | 0.1      | 0.0 | 0.0        | 17.0  | 0.0  | 0.0         | 0.0 | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0| 0.0       | 102.05 | 17.01 | 0.0      | 0.0  | 85.04       |
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 1
    Then Chargeback with 0.01 EUR transaction amount for Payment nr. 1 results a 503 error and "chargeback not allowed as loan transaction amount is not enough" error message
    Then Customer undo "1"th transaction made on "01 February 2024" results a 403 error and "update not allowed as loan transaction is linked to other transactions" error message
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0|           | 0.0 | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0 | 0.0        | 17.01 | 17.01 | 0.0       | 0.0   | 0.0       |
      | 2  | 29   | 01 March 2024    |                  | 67.1            | 32.9          | 1.12     | 0.0 | 0.0        | 34.02 | 0.0  | 0.0         | 0.0 | 34.02       |
      | 3  | 31   | 01 April 2024    |                  | 50.48           | 16.62         | 0.39     | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.76           | 16.72         | 0.29     | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.95           | 16.81         | 0.2      | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0           | 16.95         | 0.1      | 0.0 | 0.0        | 17.05 | 0.0  | 0.0         | 0.0 | 17.05       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 116.43        | 2.68     | 0.0| 0.0       | 119.11 | 17.01 | 0.0      | 0.0  | 102.1       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0      | 0.0       | 0.0 | 0.0        | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0 | 0.0        | 83.57        | false    | false    |
      | 15 February 2024 | Chargeback       | 17.01  | 16.43     | 0.58     | 0.0 | 0.0        | 100.0        | true     | false    |
      | 15 February 2024 | Chargeback       | 17.01  | 16.43     | 0.58     | 0.0 | 0.0        | 100.0        | false    | false    |
    When Customer undo "2"th "Chargeback" transaction made on "15 February 2024"
    When Customer undo "1"th "Repayment" transaction made on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0|           | 0.0 | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0 | 0.0        | 17.01 | 0.0 | 0.0         | 0.0 | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.09           | 16.48         | 0.53     | 0.0 | 0.0        | 17.01 | 0.0 | 0.0         | 0.0 | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.47           | 16.62         | 0.39     | 0.0 | 0.0        | 17.01 | 0.0 | 0.0         | 0.0 | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.75           | 16.72         | 0.29     | 0.0 | 0.0        | 17.01 | 0.0 | 0.0         | 0.0 | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.81         | 0.2      | 0.0 | 0.0        | 17.01 | 0.0 | 0.0         | 0.0 | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0           | 16.94         | 0.1      | 0.0 | 0.0        | 17.04 | 0.0 | 0.0         | 0.0 | 17.04       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.09     | 0.0| 0.0       | 102.09 | 0.0| 0.0        | 0.0| 102.09      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0      | 0.0       | 0.0 | 0.0        | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0 | 0.0        | 83.57        | true     | false    |
      | 15 February 2024 | Chargeback       | 17.01  | 16.43     | 0.58     | 0.0 | 0.0        | 100.0        | true     | false    |
      | 15 February 2024 | Chargeback       | 17.01  | 16.43     | 0.58     | 0.0 | 0.0        | 100.0        | true     | false    |

  @TestRailId:C110995
  Scenario: Verify chargeback undo on progressive loan with interest and without interest recalculation
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                      | 0                       | 0                   | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0|           | 0.0 | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0 | 0.0        | 17.01 | 17.01 | 0.0       | 0.0   | 0.0       |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 32.95         | 1.07     | 0.0 | 0.0        | 34.02 | 0.0  | 0.0         | 0.0 | 34.02       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0           | 16.9          | 0.1      | 0.0 | 0.0        | 17.0  | 0.0  | 0.0         | 0.0 | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 116.43        | 2.63     | 0.0| 0.0       | 119.06 | 17.01 | 0.0      | 0.0  | 102.05      |
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "15 March 2024"
    When Customer undo "1"th "Chargeback" transaction made on "15 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0|           | 0.0 | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0 | 0.0        | 17.01 | 17.01 | 0.0       | 0.0   | 0.0       |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0 | 0.0        | 17.01 | 17.01 | 0.0       | 0.0   | 0.0       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0           | 16.9          | 0.1      | 0.0 | 0.0        | 17.0  | 0.0  | 0.0         | 0.0 | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0| 0.0       | 102.05 | 34.02 | 0.0      | 0.0  | 68.03       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0      | 0.0       | 0.0 | 0.0        | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0 | 0.0        | 83.57        | false    | false    |
      | 15 February 2024 | Chargeback       | 17.01  | 16.43     | 0.58     | 0.0 | 0.0        | 100.0        | true     | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0 | 0.0        | 67.05        | false    | true     |
    And Loan Transactions tab has a "CHARGEBACK" transaction with date "15 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 17.01  |
      | ASSET     | 112601       | Loans Receivable          | 16.43 |        |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.01 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
    Then Loan has 68.03 outstanding amount

  @TestRailId:C110996
  Scenario: Verify chargeback undo restores fee and penalty portions of the charged back repayment
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_INTEREST_PENALTY_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                      | 0                       | 0                   | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2024" due date and 10 EUR transaction amount
    And Admin adds "LOAN_NSF_FEE" due date charge with "01 February 2024" due date and 5 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 32.01 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 32.01 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0|           | 0.0 | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.0 | 5.0       | 32.01 | 32.01 | 0.0       | 0.0   | 0.0       |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 32.95         | 1.07     | 10.0 | 5.0       | 49.02 | 0.0  | 0.0         | 0.0 | 49.02       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0           | 16.9          | 0.1      | 0.0 | 0.0        | 17.0  | 0.0  | 0.0         | 0.0 | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 116.43        | 2.63     | 20.0 | 10.0      | 149.06 | 32.01 | 0.0      | 0.0  | 117.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0      | 0.0       | 0.0 | 0.0        | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 32.01  | 16.43     | 0.58     | 10.0 | 5.0       | 83.57        | false    | false    |
      | 15 February 2024 | Chargeback       | 32.01  | 16.43     | 0.58     | 10.0 | 5.0       | 100.0        | false    | false    |
    When Customer undo "1"th "Chargeback" transaction made on "15 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0|           | 0.0 | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.0 | 5.0       | 32.01 | 32.01 | 0.0       | 0.0   | 0.0       |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0           | 16.9          | 0.1      | 0.0 | 0.0        | 17.0  | 0.0  | 0.0         | 0.0 | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 5.0       | 117.05 | 32.01 | 0.0      | 0.0  | 85.04       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0      | 0.0       | 0.0 | 0.0        | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 32.01  | 16.43     | 0.58     | 10.0 | 5.0       | 83.57        | false    | false    |
      | 15 February 2024 | Chargeback       | 32.01  | 16.43     | 0.58     | 10.0 | 5.0       | 100.0        | true     | false    |
    And Loan Transactions tab has a "CHARGEBACK" transaction with date "15 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 32.01  |
      | ASSET     | 112601       | Loans Receivable          | 16.43 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 10.0  |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 5.0   |        |
      | LIABILITY | 145023       | Suspense/Clearing account | 32.01 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.0   |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 5.0    |
    And Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2024 | Flat             | 10.0 | 10.0 | 0.0   | 0.0         |
      | NSF fee    | true      | Specified due date | 01 February 2024 | Flat             | 5.0  | 5.0  | 0.0   | 0.0         |

  @TestRailId:C110997
  Scenario: Verify undo of chargeback made after maturity date on closed loan without interest recalculation removes the additional installment
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                      | 0                       | 0                   | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 May 2024"
    And Customer makes "AUTOPAY" repayment on "01 May 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 June 2024"
    And Customer makes "AUTOPAY" repayment on "01 June 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 July 2024"
    And Customer makes "AUTOPAY" repayment on "01 July 2024" with 17.0 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "15 July 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 10.0 EUR transaction amount for Payment nr. 6
    Then Loan status will be "ACTIVE"
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0|           | 0.0 | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0 | 0.0        | 17.01 | 17.01 | 0.0       | 0.0   | 0.0       |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0 | 0.0        | 17.01 | 17.01 | 0.0       | 0.0   | 0.0       |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.43           | 16.62         | 0.39     | 0.0 | 0.0        | 17.01 | 17.01 | 0.0       | 0.0   | 0.0       |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 33.71           | 16.72         | 0.29     | 0.0 | 0.0        | 17.01 | 17.01 | 0.0       | 0.0   | 0.0       |
      | 5  | 31   | 01 June 2024     | 01 June 2024     | 16.9            | 16.81         | 0.2      | 0.0 | 0.0        | 17.01 | 17.01 | 0.0       | 0.0   | 0.0       |
      | 6  | 30   | 01 July 2024     | 01 July 2024     | 0.0           | 16.9          | 0.1      | 0.0 | 0.0        | 17.0  | 17.0  | 0.0       | 0.0   | 0.0       |
      | 7  | 14   | 15 July 2024     |                  | 0.0           | 9.9           | 0.1      | 0.0 | 0.0        | 10.0  | 0.0  | 0.0         | 0.0 | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 109.9         | 2.15     | 0.0| 0.0       | 112.05 | 102.05 | 0.0      | 0.0  | 10.0        |
    When Admin sets the business date to "20 July 2024"
    When Customer undo "1"th "Chargeback" transaction made on "15 July 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0|           | 0.0 | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0 | 0.0        | 17.01 | 17.01 | 0.0       | 0.0   | 0.0       |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0 | 0.0        | 17.01 | 17.01 | 0.0       | 0.0   | 0.0       |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 50.43           | 16.62         | 0.39     | 0.0 | 0.0        | 17.01 | 17.01 | 0.0       | 0.0   | 0.0       |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 33.71           | 16.72         | 0.29     | 0.0 | 0.0        | 17.01 | 17.01 | 0.0       | 0.0   | 0.0       |
      | 5  | 31   | 01 June 2024     | 01 June 2024     | 16.9            | 16.81         | 0.2      | 0.0 | 0.0        | 17.01 | 17.01 | 0.0       | 0.0   | 0.0       |
      | 6  | 30   | 01 July 2024     | 01 July 2024     | 0.0           | 16.9          | 0.1      | 0.0 | 0.0        | 17.0  | 17.0  | 0.0       | 0.0   | 0.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0| 0.0       | 102.05 | 102.05 | 0.0      | 0.0  | 0.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0      | 0.0       | 0.0 | 0.0        | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0 | 0.0        | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0 | 0.0        | 67.05        | false    | false    |
      | 01 April 2024    | Repayment        | 17.01  | 16.62     | 0.39     | 0.0 | 0.0        | 50.43        | false    | false    |
      | 01 May 2024      | Repayment        | 17.01  | 16.72     | 0.29     | 0.0 | 0.0        | 33.71        | false    | false    |
      | 01 June 2024     | Repayment        | 17.01  | 16.81     | 0.2      | 0.0 | 0.0        | 16.9         | false    | false    |
      | 01 July 2024     | Repayment        | 17.0   | 16.9      | 0.1      | 0.0 | 0.0        | 0.0        | false    | false    |
      | 01 July 2024     | Accrual          | 2.05   | 0.0      | 2.05     | 0.0 | 0.0        | 0.0         | false    | false    |
      | 15 July 2024     | Chargeback       | 10.0   | 9.9       | 0.1      | 0.0 | 0.0        | 9.9          | true     | false    |
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan closedon_date is "01 July 2024"

  @TestRailId:C110998
  Scenario: Verify undo of one of two partial chargebacks keeps the other chargeback and still blocks repayment undo
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_PRINCIPAL_INTEREST_FEE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                      | 0                       | 0                   | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "10 February 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 5.0 EUR transaction amount for Payment nr. 1
    When Admin sets the business date to "15 February 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 12.01 EUR transaction amount for Payment nr. 1
    When Customer undo "1"th "Chargeback" transaction made on "10 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0|           | 0.0 | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0 | 0.0        | 17.01 | 17.01 | 0.0       | 0.0   | 0.0       |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 28.53         | 0.49     | 0.0 | 0.0        | 29.02 | 0.0  | 0.0         | 0.0 | 29.02       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0 | 0.0        | 17.01 | 0.0  | 0.0         | 0.0 | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0           | 16.9          | 0.1      | 0.0 | 0.0        | 17.0  | 0.0  | 0.0         | 0.0 | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 112.01        | 2.05     | 0.0| 0.0       | 114.06 | 17.01 | 0.0      | 0.0  | 97.05       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0      | 0.0       | 0.0 | 0.0        | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0 | 0.0        | 83.57        | false    | false    |
      | 10 February 2024 | Chargeback       | 5.0    | 5.0       | 0.0     | 0.0   | 0.0      | 88.57        | true     | false    |
      | 15 February 2024 | Chargeback       | 12.01  | 12.01     | 0.0     | 0.0   | 0.0      | 95.58        | false    | true     |
    Then Customer undo "1"th transaction made on "01 February 2024" results a 403 error and "update not allowed as loan transaction is linked to other transactions" error message
    Then Chargeback with 5.01 EUR transaction amount for Payment nr. 1 results a 503 error and "chargeback not allowed as loan transaction amount is not enough" error message
    When Customer undo "1"th "Chargeback" transaction made on "15 February 2024"
    When Customer undo "1"th "Repayment" transaction made on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0|           | 0.0 | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0 | 0.0        | 17.01 | 0.0 | 0.0         | 0.0 | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0 | 0.0        | 17.01 | 0.0 | 0.0         | 0.0 | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0 | 0.0        | 17.01 | 0.0 | 0.0         | 0.0 | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0 | 0.0        | 17.01 | 0.0 | 0.0         | 0.0 | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0 | 0.0        | 17.01 | 0.0 | 0.0         | 0.0 | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0           | 16.9          | 0.1      | 0.0 | 0.0        | 17.0  | 0.0 | 0.0         | 0.0 | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0| 0.0       | 102.05 | 0.0| 0.0        | 0.0| 102.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0      | 0.0       | 0.0 | 0.0        | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0 | 0.0        | 83.57        | true     | false    |
      | 10 February 2024 | Chargeback       | 5.0    | 5.0       | 0.0     | 0.0   | 0.0      | 88.57        | true     | false    |
      | 15 February 2024 | Chargeback       | 12.01  | 12.01     | 0.0     | 0.0   | 0.0      | 95.58        | true     | true     |

  @TestRailId:C110999
  Scenario: Verify chargeback undo on overpaid loan after credit balance refund
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                      | 0                       | 0                   | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "10 February 2024"
    And Customer makes "AUTOPAY" repayment on "10 February 2024" with 135.04 EUR transaction amount
    Then Loan status will be "OVERPAID"
    And Loan has 51.32 overpaid amount
    When Admin sets the business date to "15 February 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 1
    Then Loan status will be "OVERPAID"
    And Loan has 34.31 overpaid amount
    When Admin sets the business date to "20 February 2024"
    And Admin makes Credit Balance Refund transaction on "20 February 2024" with 20 EUR transaction amount
    Then Loan has 14.31 overpaid amount
    When Admin sets the business date to "25 February 2024"
    When Customer undo "1"th "Chargeback" transaction made on "15 February 2024"
    Then Loan status will be "OVERPAID"
    And Loan has 31.32 overpaid amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0|           | 0.0 | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0 | 0.0        | 17.01 | 17.01 | 0.0       | 0.0   | 0.0       |
      | 2  | 29   | 01 March 2024    | 10 February 2024 | 66.71           | 16.86         | 0.15     | 0.0 | 0.0        | 17.01 | 17.01 | 17.01      | 0.0 | 0.0         |
      | 3  | 31   | 01 April 2024    | 10 February 2024 | 49.7            | 17.01         | 0.0     | 0.0   | 0.0      | 17.01 | 17.01 | 17.01      | 0.0 | 0.0         |
      | 4  | 30   | 01 May 2024      | 10 February 2024 | 32.69           | 17.01         | 0.0     | 0.0   | 0.0      | 17.01 | 17.01 | 17.01      | 0.0 | 0.0         |
      | 5  | 31   | 01 June 2024     | 10 February 2024 | 15.68           | 17.01         | 0.0     | 0.0   | 0.0      | 17.01 | 17.01 | 17.01      | 0.0 | 0.0         |
      | 6  | 30   | 01 July 2024     | 10 February 2024 | 0.0           | 15.68         | 0.0     | 0.0   | 0.0      | 15.68 | 15.68 | 15.68      | 0.0 | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 100.0         | 0.73     | 0.0| 0.0       | 100.73 | 100.73 | 83.72      | 0.0| 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type      | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement          | 100.0  | 0.0      | 0.0       | 0.0 | 0.0        | 100.0        | false    | false    |
      | 01 February 2024 | Repayment             | 17.01  | 16.43     | 0.58     | 0.0 | 0.0        | 83.57        | false    | false    |
      | 10 February 2024 | Repayment             | 135.04 | 83.57     | 0.15     | 0.0 | 0.0        | 0.0        | false    | false    |
      | 10 February 2024 | Accrual               | 0.73   | 0.0      | 0.73     | 0.0 | 0.0        | 0.0         | false    | false    |
      | 15 February 2024 | Chargeback            | 17.01  | 16.43     | 0.58     | 0.0 | 0.0        | 0.0        | true     | false    |
      | 20 February 2024 | Credit Balance Refund | 20.0   | 0.0      | 0.0       | 0.0 | 0.0        | 0.0        | false    | false    |
    And Loan Transactions tab has a "CHARGEBACK" transaction with date "15 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 17.01  |
      | LIABILITY | l1           | Overpayment account       | 17.01 |        |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.01 |        |
      | LIABILITY | l1           | Overpayment account       |       | 17.01  |

  @TestRailId:C111000
  Scenario: Verify chargeback undo is rejected on written-off loan
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                      | 0                       | 0                   | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 1
    When Admin sets the business date to "20 February 2024"
    And Admin does write-off the loan on "20 February 2024"
    Then Customer undo "1"th transaction made on "15 February 2024" results a 403 error and "update not allowed as loan status is written off" error message
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0      | 0.0       | 0.0 | 0.0        | 100.0        | false    | false    |
      | 01 February 2024 | Repayment              | 17.01  | 16.43     | 0.58     | 0.0 | 0.0        | 83.57        | false    | false    |
      | 15 February 2024 | Chargeback             | 17.01  | 16.43     | 0.58     | 0.0 | 0.0        | 100.0        | false    | false    |
      | 20 February 2024 | Close (as written-off) | 102.05 | 100.0     | 2.05     | 0.0 | 0.0        | 0.0        | false    | false    |
    Then Loan status will be "CLOSED_WRITTEN_OFF"
