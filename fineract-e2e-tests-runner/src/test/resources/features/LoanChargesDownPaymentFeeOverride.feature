@LoanChargesDownPaymentFeeOverrideFeature
Feature: LoanChargesDownPaymentFeeOverride

  Background:
    Given Down payment fee charges and loan products exist

  @TestRailId:C119637
  Scenario: Verify flat down payment fee amount overridden on disbursement with auto down payment - UC1
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount and "15" EUR down payment fee amount
    Then LoanDeleteChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026" with 10.0 EUR amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 15.0 | 15.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026   | 01 January 2026 | 750.0           | 250.0         | 0.0      | 15.0 | 0.0       | 265.0 | 265.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2026   |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 15.0 | 0.0       | 1015.0 | 265.0 | 0.0        | 0.0  | 750.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 265.0  | 250.0     | 0.0      | 15.0 | 0.0       | 750.0        | false    | false    |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 15.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 265.0 |        |
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119638
  Scenario: Verify percentage down payment fee amount overridden on disbursement is a flat amount rounded to the currency precision - UC2
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_PERCENTAGE_FEE" with "PERCENTAGE_AMOUNT" calculation type and 10.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type          | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_PERCENTAGE_FEE | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount and "12.345" EUR down payment fee amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment percentage fee" charge due on "01 January 2026"
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due   | Paid  | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | Flat             | 12.34 | 12.34 | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 12.34 | 0.0       | 1012.34 | 262.34 | 0.0        | 0.0  | 750.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0   | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 262.34 | 250.0     | 0.0      | 12.34 | 0.0       | 750.0        | false    | false    |
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119639
  Scenario: Verify overridden percentage down payment fee amount is kept on reverse-replay after undo of the down payment - UC3
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_PERCENTAGE_FEE" with "PERCENTAGE_AMOUNT" calculation type and 10.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type          | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_PERCENTAGE_FEE | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount and "30" EUR down payment fee amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment percentage fee" charge due on "01 January 2026"
    When Admin sets the business date to "16 January 2026"
    And Customer makes "AUTOPAY" repayment on "16 January 2026" with 250 EUR transaction amount
    When Customer undo "1"th "Down Payment" transaction made on "01 January 2026"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 280.0  | 250.0     | 0.0      | 30.0 | 0.0       | 750.0        | true     | false    |
      | 16 January 2026  | Repayment        | 250.0  | 220.0     | 0.0      | 30.0 | 0.0       | 780.0        | false    | true     |
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | Flat             | 30.0 | 30.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2026   |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2026   |           | 750.0           | 250.0         | 0.0      | 30.0 | 0.0       | 280.0 | 250.0 | 0.0        | 250.0 | 30.0        |
      | 2  | 15   | 16 January 2026   |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 3  | 15   | 31 January 2026   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 4  | 15   | 15 February 2026  |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
    When Loan Pay-off is made on "16 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119640
  Scenario: Verify down payment fee amount overridden on one disbursement does not change the fees of earlier and later disbursements - UC4
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "400" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    When Admin successfully disburse the loan on "02 January 2026" with "300" EUR transaction amount and "15" EUR down payment fee amount
    When Admin sets the business date to "03 January 2026"
    When Admin successfully disburse the loan on "03 January 2026" with "300" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
      | Down Payment Fee | false     | Down Payment   | 02 January 2026 | Flat             | 15.0 | 15.0 | 0.0    | 0.0         |
      | Down Payment Fee | false     | Down Payment   | 03 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        | false    | false    |
      | 01 January 2026  | Down Payment     | 110.0  | 100.0     | 0.0      | 10.0 | 0.0       | 300.0        | false    | false    |
      | 02 January 2026  | Disbursement     | 300.0  | 0.0       | 0.0      | 0.0  | 0.0       | 600.0        | false    | false    |
      | 02 January 2026  | Down Payment     | 90.0   | 75.0      | 0.0      | 15.0 | 0.0       | 525.0        | false    | false    |
      | 03 January 2026  | Disbursement     | 300.0  | 0.0       | 0.0      | 0.0  | 0.0       | 825.0        | false    | false    |
      | 03 January 2026  | Down Payment     | 85.0   | 75.0      | 0.0      | 10.0 | 0.0       | 750.0        | false    | false    |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 35.0 | 0.0       | 1035.0 | 285.0 | 0.0        | 0.0  | 750.0       |
    When Loan Pay-off is made on "03 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119641
  Scenario: Verify down payment fee amount overridden on disbursement of an expected tranche changes only the fee of that tranche - UC5
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_EXPECTED_TRANCHES" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and disbursements details and following data:
      | LoanProduct                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_EXPECTED_TRANCHES | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.0          | 01 January 2026                | 600.0                      | 10 January 2026                | 400.0                      |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "600" EUR transaction amount and "15" EUR down payment fee amount
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 15.0 | 15.0 | 0.0    | 0.0         |
      | Down Payment Fee | false     | Down Payment   | 10 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    When Admin sets the business date to "10 January 2026"
    When Admin successfully disburse the loan on "10 January 2026" with "400" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 15.0 | 15.0 | 0.0    | 0.0         |
      | Down Payment Fee | false     | Down Payment   | 10 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 600.0  | 0.0       | 0.0      | 0.0  | 0.0       | 600.0        | false    | false    |
      | 01 January 2026  | Down Payment     | 165.0  | 150.0     | 0.0      | 15.0 | 0.0       | 450.0        | false    | false    |
      | 10 January 2026  | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 850.0        | false    | false    |
      | 10 January 2026  | Down Payment     | 110.0  | 100.0     | 0.0      | 10.0 | 0.0       | 750.0        | false    | false    |
    When Loan Pay-off is made on "10 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119642
  Scenario: Verify undo last disbursal deactivates the overridden down payment fee and re-disbursement uses the configured amount - UC6
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "500" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    When Admin successfully disburse the loan on "02 January 2026" with "500" EUR transaction amount and "15" EUR down payment fee amount
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
      | Down Payment Fee | false     | Down Payment   | 02 January 2026 | Flat             | 15.0 | 15.0 | 0.0    | 0.0         |
    When Admin successfully undo last disbursal
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    When Admin successfully disburse the loan on "02 January 2026" with "500" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
      | Down Payment Fee | false     | Down Payment   | 02 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 270.0 | 0.0        | 0.0  | 750.0       |
    When Loan Pay-off is made on "02 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119643
  Scenario: Verify accrual and waive of a down payment fee amount overridden on disbursement without auto down payment - UC7
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount and "15" EUR down payment fee amount
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 15.0 | 0.0  | 0.0    | 15.0        |
    When Admin sets the business date to "02 January 2026"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "01 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 15.0  |        |
      | INCOME | 404007       | Fee Income              |       | 15.0   |
    And Admin waives the last unpaid "LOAN_DOWN_PAYMENT_FEE" type charge which is due on "01 January 2026"
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 15.0 | 0.0  | 15.0   | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement       | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Accrual            | 15.0   | 0.0       | 0.0      | 15.0 | 0.0       | 0.0          |
      | 01 January 2026  | Waive loan charges | 15.0   | 0.0       | 0.0      | 15.0 | 0.0       | 1000.0       |
    Then Loan Transactions tab has a "WAIVE_CHARGES" transaction with date "01 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit | Credit |
      | EXPENSE | e4           | Written off             | 15.0  |        |
      | ASSET   | 112603       | Interest/Fee Receivable |       | 15.0   |
    When Loan Pay-off is made on "02 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119644
  Scenario: Verify charge adjustment of a down payment fee amount overridden on disbursement with auto down payment - UC8
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount and "15" EUR down payment fee amount
    When Admin makes a charge adjustment for the last "LOAN_DOWN_PAYMENT_FEE" type charge which is due on "01 January 2026" with 5 EUR transaction amount and externalId ""
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Down Payment      | 265.0  | 250.0     | 0.0      | 15.0 | 0.0       | 750.0        |
      | 01 January 2026  | Charge Adjustment | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 745.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 15.0 | 0.0       | 1015.0 | 270.0 | 5.0        | 0.0  | 745.0       |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 5.0    |
      | INCOME | 404007       | Fee Income       | 5.0   |        |
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119645
  Scenario Outline: Verify disbursement fails when the down payment fee amount is not greater than zero - UC9
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    Then Admin fails to disburse the loan on "01 January 2026" with "1000" EUR transaction amount and "<downPaymentFeeAmount>" EUR down payment fee amount and got an error "The parameter `downPaymentFeeAmount` must be greater than 0."
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

    Examples:
      | downPaymentFeeAmount |
      | 0                    |
      | -5                   |

  @TestRailId:C119646
  Scenario: Verify disbursement fails when the down payment fee amount is sent together with skipDownPaymentFee - UC10
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    Then Admin fails to disburse the loan on "01 January 2026" with "1000" EUR transaction amount and "15" EUR down payment fee amount skipping down payment fee and got an error "Down payment fee amount cannot be passed together with skipDownPaymentFee."
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119647
  Scenario: Verify disbursement fails with a down payment fee amount when the loan has no down payment fee charge - UC11
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    Then Admin fails to disburse the loan on "01 January 2026" with "1000" EUR transaction amount and "15" EUR down payment fee amount and got an error "Down payment fee amount cannot be passed because the loan has no down payment fee charge."
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119648
  Scenario: Verify disbursement fails with a down payment fee amount when the disbursement creates no down payment period - UC12
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL | 01 January 2026   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    Then Admin fails to disburse the loan on "01 January 2026" with "1000" EUR transaction amount and "15" EUR down payment fee amount and got an error "Down payment fee amount cannot be passed because the disbursement does not create a down payment period."
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119649
  Scenario: Verify disbursement fails with a down payment fee amount when the loan has more than one down payment fee charge - UC13
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin updates charge "LOAN_DOWN_PAYMENT_PERCENTAGE_FEE" with "PERCENTAGE_AMOUNT" calculation type and 10.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "500" EUR transaction amount
    When Admin adds "LOAN_DOWN_PAYMENT_PERCENTAGE_FEE" due date charge with "01 January 2026" due date and 10.0 EUR transaction amount
    Then Admin fails to disburse the loan on "01 January 2026" with "500" EUR transaction amount and "15" EUR down payment fee amount and got an error "Down payment fee amount cannot be passed because the loan has more than one down payment fee charge."
    When Admin successfully disburse the loan on "01 January 2026" with "500" EUR transaction amount
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119650
  Scenario: Verify disbursement fails with a down payment fee amount when the same down payment fee charge is on the loan twice - UC14
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    When Admin adds "LOAN_DOWN_PAYMENT_FEE" due date charge with "01 January 2026" due date and 10.0 EUR transaction amount
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    Then Admin fails to disburse the loan on "01 January 2026" with "1000" EUR transaction amount and "15" EUR down payment fee amount and got an error "Down payment fee amount cannot be passed because the loan has more than one down payment fee charge."
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Down Payment     | 270.0  | 250.0     | 0.0      | 20.0 | 0.0       | 750.0        |
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119651
  Scenario: Verify disbursement fails with a down payment fee amount when it disburses several tranches at once - UC15
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_EXPECTED_TRANCHES" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and disbursements details and following data:
      | LoanProduct                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_EXPECTED_TRANCHES | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.0          | 01 January 2026                | 600.0                      | 01 January 2026                | 400.0                      |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    Then Admin fails to disburse all expected tranches of the loan on "01 January 2026" with "15" EUR down payment fee amount and got an error "Down payment fee amount cannot be passed because the disbursement creates more than one down payment period."
    When Admin successfully disburse the loan on "01 January 2026" with "600" EUR transaction amount
    When Admin successfully disburse the loan on "01 January 2026" with "400" EUR transaction amount
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119652
  Scenario: Verify full undo disbursal of a loan without expected tranches restores the configured percentage down payment fee replaced by the overridden amount - UC16
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_PERCENTAGE_FEE" with "PERCENTAGE_AMOUNT" calculation type and 10.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type          | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_PERCENTAGE_FEE | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount and "30" EUR down payment fee amount
    Then LoanDeleteChargeBusinessEvent is raised for the "Down Payment percentage fee" charge due on "01 January 2026" with 25.0 EUR amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment percentage fee" charge due on "01 January 2026"
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | Flat             | 30.0 | 30.0 | 0.0    | 0.0         |
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then LoanDeleteChargeBusinessEvent is raised for the "Down Payment percentage fee" charge due on "01 January 2026" with 30.0 EUR amount
    Then LoanUpdateChargeBusinessEvent is raised for the "Down Payment percentage fee" charge due on "01 January 2026"
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | % Amount         | 25.0 | 0.0  | 0.0    | 25.0        |
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | % Amount         | 25.0 | 25.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Down Payment     | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 25.0 | 0.0       | 1025.0 | 275.0 | 0.0        | 0.0  | 750.0       |
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119653
  Scenario: Verify repeated override and full undo disbursal always restore the down payment fee amount given in the loan application - UC17
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type          | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE            | 15.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount and "30" EUR down payment fee amount
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee            | false     | Down Payment   | 01 January 2026 | Flat             | 30.0 | 30.0 | 0.0    | 0.0         |
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee            | false     | Down Payment   | 01 January 2026 | Flat             | 15.0 | 0.0  | 0.0    | 15.0        |
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount and "20" EUR down payment fee amount
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee            | false     | Down Payment   | 01 January 2026 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee            | false     | Down Payment   | 01 January 2026 | Flat             | 15.0 | 0.0  | 0.0    | 15.0        |
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee            | false     | Down Payment   | 01 January 2026 | Flat             | 15.0 | 15.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Down Payment     | 265.0  | 250.0     | 0.0      | 15.0 | 0.0       | 750.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 15.0 | 0.0       | 1015.0 | 265.0 | 0.0        | 0.0  | 750.0       |
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119654
  Scenario: Verify full undo disbursal of a loan with expected tranches restores the configured percentage down payment fee of the tranche replaced by the overridden amount - UC18
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_EXPECTED_TRANCHES" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_PERCENTAGE_FEE" with "PERCENTAGE_AMOUNT" calculation type and 10.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursements details and following data:
      | LoanProduct                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type          | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_EXPECTED_TRANCHES | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_PERCENTAGE_FEE | 10.0          | 01 January 2026                | 600.0                      | 10 January 2026                | 400.0                      |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "600" EUR transaction amount and "30" EUR down payment fee amount
    Then LoanDeleteChargeBusinessEvent is raised for the "Down Payment percentage fee" charge due on "01 January 2026" with 15.0 EUR amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment percentage fee" charge due on "01 January 2026"
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | Flat             | 30.0 | 30.0 | 0.0    | 0.0         |
      | Down Payment percentage fee | false     | Down Payment   | 10 January 2026 | % Amount         | 10.0 | 0.0  | 0.0    | 10.0        |
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then LoanDeleteChargeBusinessEvent is raised for the "Down Payment percentage fee" charge due on "01 January 2026" with 30.0 EUR amount
    Then LoanUpdateChargeBusinessEvent is raised for the "Down Payment percentage fee" charge due on "01 January 2026"
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | % Amount         | 15.0 | 0.0  | 0.0    | 15.0        |
      | Down Payment percentage fee | false     | Down Payment   | 10 January 2026 | % Amount         | 10.0 | 0.0  | 0.0    | 10.0        |
    When Admin successfully disburse the loan on "01 January 2026" with "600" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | % Amount         | 15.0 | 15.0 | 0.0    | 0.0         |
      | Down Payment percentage fee | false     | Down Payment   | 10 January 2026 | % Amount         | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 600.0  | 0.0       | 0.0      | 0.0  | 0.0       | 600.0        |
      | 01 January 2026  | Down Payment     | 165.0  | 150.0     | 0.0      | 15.0 | 0.0       | 450.0        |
    When Admin sets the business date to "10 January 2026"
    When Admin successfully disburse the loan on "10 January 2026" with "400" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | % Amount         | 15.0 | 15.0 | 0.0    | 0.0         |
      | Down Payment percentage fee | false     | Down Payment   | 10 January 2026 | % Amount         | 10.0 | 10.0 | 0.0    | 0.0         |
    When Loan Pay-off is made on "10 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119655
  Scenario: Verify full undo disbursal of a loan without expected tranches deactivates an overridden down payment fee that replaced no fee - UC19
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_PERCENTAGE_FEE" with "PERCENTAGE_AMOUNT" calculation type and 10.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type          | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_PERCENTAGE_FEE | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount skipping down payment fee
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount and "40" EUR down payment fee amount
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | Flat             | 40.0 | 40.0 | 0.0    | 0.0         |
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | % Amount         | 25.0 | 25.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Down Payment     | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        |
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119656
  Scenario: Verify full undo disbursal of a loan with expected tranches deactivates an overridden down payment fee of a tranche disbursed earlier with skipDownPaymentFee - UC20
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_EXPECTED_TRANCHES" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_PERCENTAGE_FEE" with "PERCENTAGE_AMOUNT" calculation type and 10.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursements details and following data:
      | LoanProduct                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type          | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_EXPECTED_TRANCHES | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_PERCENTAGE_FEE | 10.0          | 01 January 2026                | 600.0                      | 10 January 2026                | 400.0                      |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "600" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    When Admin successfully disburse the loan on "10 January 2026" with "400" EUR transaction amount skipping down payment fee
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | % Amount         | 15.0 | 0.0  | 0.0    | 15.0        |
    When Admin successfully disburse the loan on "01 January 2026" with "600" EUR transaction amount
    When Admin successfully disburse the loan on "10 January 2026" with "400" EUR transaction amount and "30" EUR down payment fee amount
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | % Amount         | 15.0 | 15.0 | 0.0    | 0.0         |
      | Down Payment percentage fee | false     | Down Payment   | 10 January 2026 | Flat             | 30.0 | 30.0 | 0.0    | 0.0         |
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | % Amount         | 15.0 | 0.0  | 0.0    | 15.0        |
    When Admin successfully disburse the loan on "01 January 2026" with "600" EUR transaction amount
    When Admin successfully disburse the loan on "10 January 2026" with "400" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | % Amount         | 15.0 | 15.0 | 0.0    | 0.0         |
      | Down Payment percentage fee | false     | Down Payment   | 10 January 2026 | % Amount         | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 600.0  | 0.0       | 0.0      | 0.0  | 0.0       | 600.0        |
      | 01 January 2026  | Down Payment     | 165.0  | 150.0     | 0.0      | 15.0 | 0.0       | 450.0        |
      | 10 January 2026  | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 850.0        |
      | 10 January 2026  | Down Payment     | 110.0  | 100.0     | 0.0      | 10.0 | 0.0       | 750.0        |
    When Loan Pay-off is made on "10 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
