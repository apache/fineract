@LoanDownPaymentFeature
Feature: Loan DownPayment - Part2

  @TestRailId:C3063 @AdvancedPaymentAllocation
  Scenario: Verify enhanced auto downpayment for overpaid loans - UC3: overpaid amount < second disbursement amount BUT overpaid amount > related downpayment amount
    When Admin sets the business date to "01 February 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 February 2024  | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 February 2024" with "1000" amount and expected disbursement date on "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "500" EUR transaction amount
    When Admin sets the business date to "16 February 2024"
    And Customer makes "AUTOPAY" repayment on "16 February 2024" with 575 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 200 overpaid amount
    When Admin sets the business date to "20 February 2024"
    When Admin successfully disburse the loan on "20 February 2024" with "400" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 200 outstanding amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 February 2024 |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 February 2024 | 01 February 2024 | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 February 2024 | 16 February 2024 | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 20 February 2024 |                  | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 3  | 0    | 20 February 2024 | 20 February 2024 | 550.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 02 March 2024    |                  | 275.0           | 275.0         | 0.0      | 0.0  | 0.0       | 275.0 | 225.0 | 225.0      | 0.0  | 50.0        |
      | 5  | 15   | 17 March 2024    |                  | 0.0             | 275.0         | 0.0      | 0.0  | 0.0       | 275.0 | 125.0 | 125.0      | 0.0  | 150.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 900.0         | 0.0      | 0.0  | 0.0       | 900.0 | 700.0 | 350.0      | 0.0  | 200.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2024 | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 February 2024 | Down Payment     | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 February 2024 | Repayment        | 575.0  | 375.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 20 February 2024 | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        |
# --- close the loan --- #
    When Loan Pay-off is made on "20 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3064 @AdvancedPaymentAllocation
  Scenario: Verify enhanced auto downpayment for overpaid loans - UC4: overpaid amount < second disbursement amount AND overpaid amount < related downpayment amount
    When Admin sets the business date to "01 February 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 February 2024  | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 February 2024" with "1000" amount and expected disbursement date on "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "500" EUR transaction amount
    When Admin sets the business date to "16 February 2024"
    And Customer makes "AUTOPAY" repayment on "16 February 2024" with 475 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount
    When Admin sets the business date to "20 February 2024"
    When Admin successfully disburse the loan on "20 February 2024" with "600" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 450 outstanding amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 February 2024 |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 February 2024 | 01 February 2024 | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 February 2024 | 16 February 2024 | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 20 February 2024 |                  | 600.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 3  | 0    | 20 February 2024 | 20 February 2024 | 700.0           | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 150.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 02 March 2024    |                  | 350.0           | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 125.0 | 125.0      | 0.0  | 225.0       |
      | 5  | 15   | 17 March 2024    |                  | 0.0             | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 125.0 | 125.0      | 0.0  | 225.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1100.0        | 0.0      | 0.0  | 0.0       | 1100.0 | 650.0 | 250.0      | 0.0  | 450.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2024 | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 February 2024 | Down Payment     | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 February 2024 | Repayment        | 475.0  | 375.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 20 February 2024 | Disbursement     | 600.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 20 February 2024 | Down Payment     | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 450.0        |
# --- close the loan --- #
    When Loan Pay-off is made on "20 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3065 @AdvancedPaymentAllocation
  Scenario: Verify enhanced auto downpayment for overpaid loans - UC5: repayment reverted
    When Admin sets the business date to "01 February 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 February 2024  | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 February 2024" with "1000" amount and expected disbursement date on "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "500" EUR transaction amount
    When Admin sets the business date to "10 February 2024"
    And Customer makes "AUTOPAY" repayment on "10 February 2024" with 100 EUR transaction amount
    Then Loan has 275 outstanding amount
    When Admin sets the business date to "16 February 2024"
    And Customer makes "AUTOPAY" repayment on "16 February 2024" with 375 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount
    When Admin sets the business date to "20 February 2024"
    When Admin successfully disburse the loan on "20 February 2024" with "600" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 450 outstanding amount
    When Customer undo "1"th "Repayment" transaction made on "16 February 2024"
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 February 2024 |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 February 2024 | 01 February 2024 | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 February 2024 | 20 February 2024 | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 100.0      | 25.0 | 0.0         |
      |    |      | 20 February 2024 |                  | 600.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 3  | 0    | 20 February 2024 |                  | 700.0           | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 25.0  | 0.0        | 0.0  | 125.0       |
      | 4  | 15   | 02 March 2024    |                  | 350.0           | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
      | 5  | 15   | 17 March 2024    |                  | 0.0             | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1100.0        | 0.0      | 0.0  | 0.0       | 1100.0 | 275.0 | 100.0      | 25.0 | 825.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 February 2024 | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    |
      | 01 February 2024 | Down Payment     | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        | false    |
      | 10 February 2024 | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 275.0        | false    |
      | 16 February 2024 | Repayment        | 375.0  | 275.0     | 0.0      | 0.0  | 0.0       | 0.0          | true     |
      | 20 February 2024 | Disbursement     | 600.0  | 0.0       | 0.0      | 0.0  | 0.0       | 875.0        | false    |
      | 20 February 2024 | Down Payment     | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 825.0        | false    |
# --- close the loan --- #
    When Loan Pay-off is made on "20 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3105 @AdvancedPaymentAllocation
  Scenario: Verify multi disbursement on same day with Advanced payment allocation works properly
    When Admin sets the business date to "01 February 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 February 2024  | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 February 2024" with "1000" amount and expected disbursement date on "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 February 2024 | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 February 2024 |                  | 50.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 3  | 15   | 02 March 2024    |                  | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 15   | 17 March 2024    |                  | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 25.0 | 0.0        | 0.0  | 75.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    |
    When Admin sets the business date to "02 February 2024"
    When Admin successfully disburse the loan on "02 February 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 February 2024 | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 25.0  | 0.0        | 0.0  | 0.0         |
      |    |      | 02 February 2024 |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 02 February 2024 | 02 February 2024 | 825.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 16 February 2024 |                  | 550.0           | 275.0         | 0.0      | 0.0  | 0.0       | 275.0 | 0.0   | 0.0        | 0.0  | 275.0       |
      | 4  | 15   | 02 March 2024    |                  | 275.0           | 275.0         | 0.0      | 0.0  | 0.0       | 275.0 | 0.0   | 0.0        | 0.0  | 275.0       |
      | 5  | 15   | 17 March 2024    |                  | 0.0             | 275.0         | 0.0      | 0.0  | 0.0       | 275.0 | 0.0   | 0.0        | 0.0  | 275.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1100.0        | 0.0      | 0.0  | 0.0       | 1100.0 | 275.0 | 0.0        | 0.0  | 825.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    |
      | 02 February 2024 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1075.0       | false    |
      | 02 February 2024 | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 825.0        | false    |
    When Admin successfully disburse the loan on "02 February 2024" with "112" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 February 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 February 2024 | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 25.0  | 0.0        | 0.0  | 0.0         |
      |    |      | 02 February 2024 |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      |    |      | 02 February 2024 |                  | 112.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 02 February 2024 | 02 February 2024 | 937.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 0    | 02 February 2024 | 02 February 2024 | 909.0           | 28.0          | 0.0      | 0.0  | 0.0       | 28.0  | 28.0  | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 16 February 2024 |                  | 606.0           | 303.0         | 0.0      | 0.0  | 0.0       | 303.0 | 0.0   | 0.0        | 0.0  | 303.0       |
      | 5  | 15   | 02 March 2024    |                  | 303.0           | 303.0         | 0.0      | 0.0  | 0.0       | 303.0 | 0.0   | 0.0        | 0.0  | 303.0       |
      | 6  | 15   | 17 March 2024    |                  | 0.0             | 303.0         | 0.0      | 0.0  | 0.0       | 303.0 | 0.0   | 0.0        | 0.0  | 303.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1212.0        | 0.0      | 0.0  | 0.0       | 1212.0 | 303.0 | 0.0        | 0.0  | 909.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 February 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    |
      | 02 February 2024 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1075.0       | false    |
      | 02 February 2024 | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 825.0        | false    |
      | 02 February 2024 | Disbursement     | 112.0  | 0.0       | 0.0      | 0.0  | 0.0       | 937.0        | false    |
      | 02 February 2024 | Down Payment     | 28.0   | 28.0      | 0.0      | 0.0  | 0.0       | 909.0        | false    |
# --- close the loan --- #
    When Loan Pay-off is made on "02 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3162
  Scenario: Verify that downpayment can be enabled during loan creation if the product has downpayment enabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with forced disabled downpayment with the following data:
      | LoanProduct     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2022 |           | 667.0           | 333.0         | 0.0      | 0.0  | 0.0       | 333.0 | 0.0  | 0.0        | 0.0  | 333.0       |
      | 2  | 28   | 01 March 2022    |           | 334.0           | 333.0         | 0.0      | 0.0  | 0.0       | 333.0 | 0.0  | 0.0        | 0.0  | 333.0       |
      | 3  | 31   | 01 April 2022    |           | 0.0             | 334.0         | 0.0      | 0.0  | 0.0       | 334.0 | 0.0  | 0.0        | 0.0  | 334.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0.0  | 0          | 0    | 1000        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
# --- close the loan --- #
    When Loan Pay-off is made on "1 January 2022"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3163
  Scenario: Verify that downpayment cannot be enabled during loan creation if the product has downpayment disabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    Then Admin fails to create a fully customized loan with forced enabled downpayment with the following data:
      | LoanProduct                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL | 01 January 2022   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |

  @TestRailId:C3180
  Scenario: Verify that auto repayment and downpayment percentage value can be set during loan creation in case of downpayment is enabled on Loan product level - UC1
    When Admin sets the business date to "01 July 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with auto downpayment 15% and with the following data:
      | LoanProduct     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 July 2024      | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 July 2024" with "1000" amount and expected disbursement date on "01 July 2024"
    When Admin successfully disburse the loan on "01 July 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 July 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 July 2024      | 01 July 2024 | 850.0           | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 150.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    |              | 567.0           | 283.0         | 0.0      | 0.0  | 0.0       | 283.0 | 0.0   | 0.0        | 0.0  | 283.0       |
      | 3  | 31   | 01 September 2024 |              | 284.0           | 283.0         | 0.0      | 0.0  | 0.0       | 283.0 | 0.0   | 0.0        | 0.0  | 283.0       |
      | 4  | 30   | 01 October 2024   |              | 0.0             | 284.0         | 0.0      | 0.0  | 0.0       | 284.0 | 0.0   | 0.0        | 0.0  | 284.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 150.0 | 0          | 0    | 850         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2024     | Down Payment     | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 850.0        | false    |
# --- close the loan --- #
    When Loan Pay-off is made on "01 July 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3181
  Scenario: Verify that auto repayment and downpayment percentage value can be set during loan creation in case of downpayment is enabled on Loan product level - UC2
    When Admin sets the business date to "01 July 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with downpayment 15%, NO auto downpayment, and with the following data:
      | LoanProduct     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 July 2024      | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 July 2024" with "1000" amount and expected disbursement date on "01 July 2024"
    When Admin successfully disburse the loan on "01 July 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 July 2024      |           | 850.0           | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
      | 2  | 31   | 01 August 2024    |           | 567.0           | 283.0         | 0.0      | 0.0  | 0.0       | 283.0 | 0.0  | 0.0        | 0.0  | 283.0       |
      | 3  | 31   | 01 September 2024 |           | 284.0           | 283.0         | 0.0      | 0.0  | 0.0       | 283.0 | 0.0  | 0.0        | 0.0  | 283.0       |
      | 4  | 30   | 01 October 2024   |           | 0.0             | 284.0         | 0.0      | 0.0  | 0.0       | 284.0 | 0.0  | 0.0        | 0.0  | 284.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0.0  | 0          | 0    | 1000        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
# --- close the loan --- #
    When Loan Pay-off is made on "01 July 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4674 @AdvancedPaymentAllocation
  Scenario: Verify pay-off loan after 2nd disbursement with downpayment on interest bearing loan
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_AUTO_DOWNPAYMENT | 01 January 2024   | 150            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "150" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 25.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2024 |                 | 62.68           | 12.32         | 0.44     | 0.0  | 0.0       | 12.76 | 0.0  | 0.0        | 0.0  | 12.76       |
      | 3  | 29   | 01 March 2024    |                 | 50.29           | 12.39         | 0.37     | 0.0  | 0.0       | 12.76 | 0.0  | 0.0        | 0.0  | 12.76       |
      | 4  | 31   | 01 April 2024    |                 | 37.82           | 12.47         | 0.29     | 0.0  | 0.0       | 12.76 | 0.0  | 0.0        | 0.0  | 12.76       |
      | 5  | 30   | 01 May 2024      |                 | 25.28           | 12.54         | 0.22     | 0.0  | 0.0       | 12.76 | 0.0  | 0.0        | 0.0  | 12.76       |
      | 6  | 31   | 01 June 2024     |                 | 12.67           | 12.61         | 0.15     | 0.0  | 0.0       | 12.76 | 0.0  | 0.0        | 0.0  | 12.76       |
      | 7  | 30   | 01 July 2024     |                 | 0.0             | 12.67         | 0.07     | 0.0  | 0.0       | 12.74 | 0.0  | 0.0        | 0.0  | 12.74       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.54     | 0.0  | 0.0       | 101.54 | 25.0 | 0.0        | 0.0  | 76.54       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    | false    |
# --- First installment paid --- #
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 12.76 EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024  | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 25.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 62.68           | 12.32         | 0.44     | 0.0  | 0.0       | 12.76 | 12.76 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    |                  | 50.29           | 12.39         | 0.37     | 0.0  | 0.0       | 12.76 | 0.0   | 0.0        | 0.0  | 12.76       |
      | 4  | 31   | 01 April 2024    |                  | 37.82           | 12.47         | 0.29     | 0.0  | 0.0       | 12.76 | 0.0   | 0.0        | 0.0  | 12.76       |
      | 5  | 30   | 01 May 2024      |                  | 25.28           | 12.54         | 0.22     | 0.0  | 0.0       | 12.76 | 0.0   | 0.0        | 0.0  | 12.76       |
      | 6  | 31   | 01 June 2024     |                  | 12.67           | 12.61         | 0.15     | 0.0  | 0.0       | 12.76 | 0.0   | 0.0        | 0.0  | 12.76       |
      | 7  | 30   | 01 July 2024     |                  | 0.0             | 12.67         | 0.07     | 0.0  | 0.0       | 12.74 | 0.0   | 0.0        | 0.0  | 12.74       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.54     | 0.0  | 0.0       | 101.54 | 37.76 | 0.0        | 0.0  | 63.78       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    | false    |
      | 01 February 2024 | Repayment        | 12.76  | 12.32     | 0.44     | 0.0  | 0.0       | 62.68        | false    | false    |
# --- 2nd disbursement --- #
    When Admin sets the business date to "01 April 2024"
    When Admin successfully disburse the loan on "01 April 2024" with "50" EUR transaction amount
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024  | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 25.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 62.68           | 12.32         | 0.44     | 0.0  | 0.0       | 12.76 | 12.76 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    |                  | 50.29           | 12.39         | 0.37     | 0.0  | 0.0       | 12.76 | 12.5  | 0.0        | 12.5 | 0.26        |
      | 4  | 31   | 01 April 2024    |                  | 37.9            | 12.39         | 0.37     | 0.0  | 0.0       | 12.76 | 0.0   | 0.0        | 0.0  | 12.76       |
      |    |      | 01 April 2024    |                  | 50.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 5  |  0   | 01 April 2024    |                  | 75.4            | 12.5          | 0.0      | 0.0  | 0.0       | 12.5  | 0.0   | 0.0        | 0.0  | 12.5        |
      | 6  | 30   | 01 May 2024      |                  | 50.44           | 24.96         | 0.44     | 0.0  | 0.0       | 25.4  | 0.0   | 0.0        | 0.0  | 25.4        |
      | 7  | 31   | 01 June 2024     |                  | 25.33           | 25.11         | 0.29     | 0.0  | 0.0       | 25.4  | 0.0   | 0.0        | 0.0  | 25.4        |
      | 8  | 30   | 01 July 2024     |                  | 0.0             | 25.33	      | 0.15     | 0.0  | 0.0       | 25.48 | 0.0   | 0.0        | 0.0  | 25.48       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 150.0         | 2.06     | 0.0  | 0.0       | 152.06 | 50.26 | 0.0        | 12.5 | 101.8       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    | false    |
      | 01 February 2024 | Repayment        | 12.76  | 12.32     | 0.44     | 0.0  | 0.0       | 62.68        | false    | false    |
      | 01 April 2024    | Disbursement     | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 112.68       | false    | false    |
      | 01 April 2024    | Down Payment     | 12.5   | 12.39     | 0.11     | 0.0  | 0.0       | 100.29       | false    | false    |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4675 @AdvancedPaymentAllocation
  Scenario: Verify pay-off loan after 3rd disbursement with downpayment on interest bearing loan
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_AUTO_DOWNPAYMENT | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 25.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2024 |                 | 62.68           | 12.32         | 0.44     | 0.0  | 0.0       | 12.76 | 0.0  | 0.0        | 0.0  | 12.76       |
      | 3  | 29   | 01 March 2024    |                 | 50.29           | 12.39         | 0.37     | 0.0  | 0.0       | 12.76 | 0.0  | 0.0        | 0.0  | 12.76       |
      | 4  | 31   | 01 April 2024    |                 | 37.82           | 12.47         | 0.29     | 0.0  | 0.0       | 12.76 | 0.0  | 0.0        | 0.0  | 12.76       |
      | 5  | 30   | 01 May 2024      |                 | 25.28           | 12.54         | 0.22     | 0.0  | 0.0       | 12.76 | 0.0  | 0.0        | 0.0  | 12.76       |
      | 6  | 31   | 01 June 2024     |                 | 12.67           | 12.61         | 0.15     | 0.0  | 0.0       | 12.76 | 0.0  | 0.0        | 0.0  | 12.76       |
      | 7  | 30   | 01 July 2024     |                 | 0.0             | 12.67         | 0.07     | 0.0  | 0.0       | 12.74 | 0.0  | 0.0        | 0.0  | 12.74       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.54     | 0.0  | 0.0       | 101.54 | 25.0 | 0.0        | 0.0  | 76.54       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    | false    |
# --- First installment paid --- #
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 12.76 EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024  | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 25.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 62.68           | 12.32         | 0.44     | 0.0  | 0.0       | 12.76 | 12.76 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    |                  | 50.29           | 12.39         | 0.37     | 0.0  | 0.0       | 12.76 | 0.0   | 0.0        | 0.0  | 12.76       |
      | 4  | 31   | 01 April 2024    |                  | 37.82           | 12.47         | 0.29     | 0.0  | 0.0       | 12.76 | 0.0   | 0.0        | 0.0  | 12.76       |
      | 5  | 30   | 01 May 2024      |                  | 25.28           | 12.54         | 0.22     | 0.0  | 0.0       | 12.76 | 0.0   | 0.0        | 0.0  | 12.76       |
      | 6  | 31   | 01 June 2024     |                  | 12.67           | 12.61         | 0.15     | 0.0  | 0.0       | 12.76 | 0.0   | 0.0        | 0.0  | 12.76       |
      | 7  | 30   | 01 July 2024     |                  | 0.0             | 12.67         | 0.07     | 0.0  | 0.0       | 12.74 | 0.0   | 0.0        | 0.0  | 12.74       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.54     | 0.0  | 0.0       | 101.54 | 37.76 | 0.0        | 0.0  | 63.78       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    | false    |
      | 01 February 2024 | Repayment        | 12.76  | 12.32     | 0.44     | 0.0  | 0.0       | 62.68        | false    | false    |
# --- 2nd disbursement --- #
    When Admin sets the business date to "01 April 2024"
    When Admin successfully disburse the loan on "01 April 2024" with "50" EUR transaction amount
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024  | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 25.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 62.68           | 12.32         | 0.44     | 0.0  | 0.0       | 12.76 | 12.76 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    |                  | 50.29           | 12.39         | 0.37     | 0.0  | 0.0       | 12.76 | 12.5  | 0.0        | 12.5 | 0.26        |
      | 4  | 31   | 01 April 2024    |                  | 37.9            | 12.39         | 0.37     | 0.0  | 0.0       | 12.76 | 0.0   | 0.0        | 0.0  | 12.76       |
      |    |      | 01 April 2024    |                  | 50.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 5  |  0   | 01 April 2024    |                  | 75.4            | 12.5          | 0.0      | 0.0  | 0.0       | 12.5  | 0.0   | 0.0        | 0.0  | 12.5        |
      | 6  | 30   | 01 May 2024      |                  | 50.44           | 24.96         | 0.44     | 0.0  | 0.0       | 25.4  | 0.0   | 0.0        | 0.0  | 25.4        |
      | 7  | 31   | 01 June 2024     |                  | 25.33           | 25.11         | 0.29     | 0.0  | 0.0       | 25.4  | 0.0   | 0.0        | 0.0  | 25.4        |
      | 8  | 30   | 01 July 2024     |                  | 0.0             | 25.33	      | 0.15     | 0.0  | 0.0       | 25.48 | 0.0   | 0.0        | 0.0  | 25.48       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 150.0         | 2.06     | 0.0  | 0.0       | 152.06 | 50.26 | 0.0        | 12.5 | 101.8       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    | false    |
      | 01 February 2024 | Repayment        | 12.76  | 12.32     | 0.44     | 0.0  | 0.0       | 62.68        | false    | false    |
      | 01 April 2024    | Disbursement     | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 112.68       | false    | false    |
      | 01 April 2024    | Down Payment     | 12.5   | 12.39     | 0.11     | 0.0  | 0.0       | 100.29       | false    | false    |
    # --- 3rd disbursement --- #
    When Admin sets the business date to "01 May 2024"
    When Admin successfully disburse the loan on "01 May 2024" with "50" EUR transaction amount
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024  | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 25.0  | 0.0        | 0.0   | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 62.68           | 12.32         | 0.44     | 0.0  | 0.0       | 12.76 | 12.76 | 0.0        | 0.0   | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 May 2024      | 50.29           | 12.39         | 0.37     | 0.0  | 0.0       | 12.76 | 12.76 | 0.0        | 12.76 | 0.0         |
      | 4  | 31   | 01 April 2024    |                  | 37.9            | 12.39         | 0.37     | 0.0  | 0.0       | 12.76 | 12.24 | 0.0        | 12.24 | 0.52        |
      |    |      | 01 April 2024    |                  | 50.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 5  |  0   | 01 April 2024    |                  | 75.4            | 12.5          | 0.0      | 0.0  | 0.0       | 12.5  | 0.0   | 0.0        | 0.0   | 12.5        |
      | 6  | 30   | 01 May 2024      |                  | 50.51           | 24.89         | 0.51     | 0.0  | 0.0       | 25.4  | 0.0   | 0.0        | 0.0   | 25.4        |
      |    |      | 01 May 2024      |                  | 50.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 7  | 0    | 01 May 2024      |                  | 88.01           | 12.5          | 0.0      | 0.0  | 0.0       | 12.5  | 0.0   | 0.0        | 0.0   | 12.5        |
      | 8  | 31   | 01 June 2024     |                  | 44.21           | 43.8          | 0.51     | 0.0  | 0.0       | 44.31 | 0.0   | 0.0        | 0.0   | 44.31       |
      | 9  | 30   | 01 July 2024     |                  | 0.0             | 44.21	      | 0.26     | 0.0  | 0.0       | 44.47 | 0.0   | 0.0        | 0.0   | 44.47       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 2.46     | 0.0  | 0.0       | 202.46 | 62.76 | 0.0        | 25.0 | 139.7       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    | false    |
      | 01 February 2024 | Repayment        | 12.76  | 12.32     | 0.44     | 0.0  | 0.0       | 62.68        | false    | false    |
      | 01 April 2024    | Disbursement     | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 112.68       | false    | false    |
      | 01 April 2024    | Down Payment     | 12.5   | 12.39     | 0.11     | 0.0  | 0.0       | 100.29       | false    | false    |
      | 01 May 2024      | Disbursement     | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 150.29       | false    | false    |
      | 01 May 2024      | Down Payment     | 12.5   | 12.24     | 0.26     | 0.0  | 0.0       | 138.05       | false    | false    |
    When Loan Pay-off is made on "01 May 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

