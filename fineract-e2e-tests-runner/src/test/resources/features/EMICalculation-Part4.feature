@Emi
Feature: EMI calculation and repayment schedule checks for interest bearing loans - Part4

  @TestRailId:C3836
  Scenario: Progressive loan - flat interest, multi-disbursement, allowPartialPeriodInterestCalculation = false, actual/actual, second disbursement on the due date of installment period
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE_PART_PERIOD_CALC_DISABLED | 01 January 2024   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "3000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 333.34        | 10.0     | 0.0  | 0.0       | 343.34 | 0.0  | 0.0        | 0.0  | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "01 August 2024"
    When Admin successfully disburse the loan on "01 August 2024" with "250" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      |    |      | 01 August 2024    |           | 250.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 583.34        | 12.5     | 0.0  | 0.0       | 595.84 | 0.0  | 0.0        | 0.0  | 595.84      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1250          | 32.5     | 0.0  | 0.0       | 1282.5  | 0.0  | 0.0        | 0.0  | 1282.5      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 August 2024   | Disbursement            | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 June 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 August 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    And Customer makes "AUTOPAY" repayment on "01 August 2024" with 1282.5 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 June 2024      |                | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 01 July 2024      | 01 August 2024 | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 343.33 | 0.0        | 343.33 | 0.0         |
      | 2  | 31   | 01 August 2024    | 01 August 2024 | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 343.33 | 0.0        | 0.0    | 0.0         |
      |    |      | 01 August 2024    |                | 250.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 3  | 31   | 01 September 2024 | 01 August 2024 | 0.0             | 583.34        | 12.5     | 0.0  | 0.0       | 595.84 | 595.84 | 595.84     | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 1250          | 32.5     | 0.0  | 0.0       | 1282.5  | 1282.5 | 595.84     | 343.33 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 August 2024   | Disbursement            | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
      | 01 August 2024   | Repayment               | 1282.5 | 1250.0    | 32.5     | 0.0  | 0.0       | 0.0          |
      | 01 August 2024   | Accrual                 | 32.5   | 0.0       | 32.5     | 0.0  | 0.0       | 0.0          |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 August 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1250.0 |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 32.5   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1282.5 |        |
    When Customer makes a repayment undo on "01 August 2024"
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 333.34        | 10.0     | 0.0  | 0.0       | 343.34 | 0.0  | 0.0        | 0.0  | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 August 2024   | Repayment               | 1282.5 | 1250.0    | 32.5     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 01 August 2024   | Accrual                 | 32.5   | 0.0       | 32.5     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3837
  Scenario: Progressive loan - flat interest, multi-disbursement, allowPartialPeriodInterestCalculation = false, 360/30, second disbursement in the middle of installment period
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_INTEREST_FLAT_360_30_ADV_PMT_ALLOC_MULTIDISBURSE_PART_PERIOD_CALC_DISABLED | 01 January 2024   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "3000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 333.34        | 10.0     | 0.0  | 0.0       | 343.34 | 0.0  | 0.0        | 0.0  | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "15 July 2024"
    When Admin successfully disburse the loan on "15 July 2024" with "250" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      |    |      | 15 July 2024      |           | 250.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 2  | 31   | 01 August 2024    |           | 458.33          | 458.34        | 12.5     | 0.0  | 0.0       | 470.84 | 0.0  | 0.0        | 0.0  | 470.84      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 458.33        | 12.5     | 0.0  | 0.0       | 470.83 | 0.0  | 0.0        | 0.0  | 470.83      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1250          | 35.0     | 0.0  | 0.0       | 1285.0 | 0.0  | 0.0        | 0.0  | 1285.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 July 2024     | Disbursement     | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 June 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "15 July 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    And Customer makes "AUTOPAY" repayment on "15 July 2024" with 814.17 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 01 July 2024      | 15 July 2024 | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 343.33 | 0.0        | 343.33 | 0.0         |
      |    |      | 15 July 2024      |              | 250.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 31   | 01 August 2024    | 15 July 2024 | 458.33          | 458.34        | 12.5     | 0.0  | 0.0       | 470.84 | 470.84 | 470.84     | 0.0    | 0.0         |
      | 3  | 31   | 01 September 2024 |              | 0.0             | 458.33        | 12.5     | 0.0  | 0.0       | 470.83 | 0.0    | 0.0        | 0.0    | 470.83      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      | 1250          | 35.0     | 0.0  | 0.0       | 1285.0 | 814.17 | 470.84     | 343.33 | 470.83      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 July 2024     | Disbursement     | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
      | 15 July 2024     | Repayment        | 814.17 | 791.67    | 22.5     | 0.0  | 0.0       | 458.33       |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "15 July 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 791.67 |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 22.5   |
      | LIABILITY | 145023       | Suspense/Clearing account | 814.17 |        |
    When Customer makes a repayment undo on "15 July 2024"
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 333.34        | 10.0     | 0.0  | 0.0       | 343.34 | 0.0  | 0.0        | 0.0  | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 July 2024     | Repayment        | 814.17 | 791.67    | 22.5     | 0.0  | 0.0       | 458.33       | true     | false    |

  @TestRailId:C3838
  Scenario: Progressive loan - flat interest, multi-disbursement, allowPartialPeriodInterestCalculation = false, 360/30, second disbursement on the due date of installment period
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_INTEREST_FLAT_360_30_ADV_PMT_ALLOC_MULTIDISBURSE_PART_PERIOD_CALC_DISABLED | 01 January 2024   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "3000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 333.34        | 10.0     | 0.0  | 0.0       | 343.34 | 0.0  | 0.0        | 0.0  | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "01 August 2024"
    When Admin successfully disburse the loan on "01 August 2024" with "250" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      |    |      | 01 August 2024    |           | 250.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 583.34        | 12.5     | 0.0  | 0.0       | 595.84 | 0.0  | 0.0        | 0.0  | 595.84      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1250          | 32.5     | 0.0  | 0.0       | 1282.5  | 0.0  | 0.0        | 0.0  | 1282.5      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 August 2024   | Disbursement            | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 June 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 August 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    And Customer makes "AUTOPAY" repayment on "01 August 2024" with 1282.5 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 June 2024      |                | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 01 July 2024      | 01 August 2024 | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 343.33 | 0.0        | 343.33 | 0.0         |
      | 2  | 31   | 01 August 2024    | 01 August 2024 | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 343.33 | 0.0        | 0.0    | 0.0         |
      |    |      | 01 August 2024    |                | 250.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 3  | 31   | 01 September 2024 | 01 August 2024 | 0.0             | 583.34        | 12.5     | 0.0  | 0.0       | 595.84 | 595.84 | 595.84     | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 1250          | 32.5     | 0.0  | 0.0       | 1282.5  | 1282.5 | 595.84     | 343.33 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 August 2024   | Disbursement            | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
      | 01 August 2024   | Repayment               | 1282.5 | 1250.0    | 32.5     | 0.0  | 0.0       | 0.0          |
      | 01 August 2024   | Accrual                 | 32.5   | 0.0       | 32.5     | 0.0  | 0.0       | 0.0          |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 August 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1250.0 |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 32.5   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1282.5 |        |
    When Customer makes a repayment undo on "01 August 2024"
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 333.34        | 10.0     | 0.0  | 0.0       | 343.34 | 0.0  | 0.0        | 0.0  | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 August 2024   | Repayment               | 1282.5 | 1250.0    | 32.5     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 01 August 2024   | Accrual                 | 32.5   | 0.0       | 32.5     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3839
  Scenario: Progressive loan - down payment, flat interest, multi-disbursement, allowPartialPeriodInterestCalculation = false, actual/actual, second disbursement in the middle of installment period
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE_PART_PERIOD_CALC_DISABLED | 01 January 2024   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "3000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 June 2024      |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 30   | 01 July 2024      |           | 500.0           | 250.0         | 7.5      | 0.0  | 0.0       | 257.5 | 0.0  | 0.0        | 0.0  | 257.5       |
      | 3  | 31   | 01 August 2024    |           | 250.0           | 250.0         | 7.5      | 0.0  | 0.0       | 257.5 | 0.0  | 0.0        | 0.0  | 257.5       |
      | 4  | 31   | 01 September 2024 |           | 0.0             | 250.0         | 7.5      | 0.0  | 0.0       | 257.5 | 0.0  | 0.0        | 0.0  | 257.5       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 22.5     | 0.0  | 0.0       | 1022.5 | 0.0  | 0.0        | 0.0  | 1022.5      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "15 July 2024"
    When Admin successfully disburse the loan on "15 July 2024" with "250" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 0    | 01 June 2024      |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 30   | 01 July 2024      |           | 500.0           | 250.0         | 7.5      | 0.0  | 0.0       | 257.5  | 0.0  | 0.0        | 0.0  | 257.5       |
      |    |      | 15 July 2024      |           | 250.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 3  | 0    | 15 July 2024      |           | 687.5           | 62.5          | 0.0      | 0.0  | 0.0       | 62.5   | 0.0  | 0.0        | 0.0  | 62.5        |
      | 4  | 31   | 01 August 2024    |           | 343.75          | 343.75        | 9.37     | 0.0  | 0.0       | 353.12 | 0.0  | 0.0        | 0.0  | 353.12      |
      | 5  | 31   | 01 September 2024 |           | 0.0             | 343.75        | 9.37     | 0.0  | 0.0       | 353.12 | 0.0  | 0.0        | 0.0  | 353.12      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1250          | 26.24    | 0.0  | 0.0       | 1276.24 | 0.0  | 0.0        | 0.0  | 1276.24     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 July 2024     | Disbursement            | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 June 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "15 July 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    And Customer makes "AUTOPAY" repayment on "15 July 2024" with 923.12 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |       |             |
      | 1  | 0    | 01 June 2024      | 15 July 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0  | 0.0        | 250.0 | 0.0         |
      | 2  | 30   | 01 July 2024      | 15 July 2024 | 500.0           | 250.0         | 7.5      | 0.0  | 0.0       | 257.5  | 257.5  | 0.0        | 257.5 | 0.0         |
      |    |      | 15 July 2024      |              | 250.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |       |             |
      | 3  | 0    | 15 July 2024      | 15 July 2024 | 687.5           | 62.5          | 0.0      | 0.0  | 0.0       | 62.5   | 62.5   | 0.0        | 0.0   | 0.0         |
      | 4  | 31   | 01 August 2024    | 15 July 2024 | 343.75          | 343.75        | 9.37     | 0.0  | 0.0       | 353.12 | 353.12 | 353.12     | 0.0   | 0.0         |
      | 5  | 31   | 01 September 2024 |              | 0.0             | 343.75        | 9.37     | 0.0  | 0.0       | 353.12 | 0.0    | 0.0        | 0.0   | 353.12      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late  | Outstanding |
      | 1250          | 26.24    | 0.0  | 0.0       | 1276.24 | 923.12 | 353.12     | 507.5 | 353.12      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 July 2024     | Disbursement            | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |
      | 15 July 2024     | Repayment               | 923.12 | 906.25    | 16.87    | 0.0  | 0.0       | 343.75       |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "15 July 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 906.25 |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 16.87  |
      | LIABILITY | 145023       | Suspense/Clearing account | 923.12 |        |

  @TestRailId:С3893
  Scenario: Progressive loan - flat interest DAILY, multi-disbursement, actual/actual, second disbursement in the middle of installment period
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE | 01 January 2024   | 3000           | 12                     | FLAT          | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "3000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.45          | 333.55        | 9.84     | 0.0  | 0.0       | 343.39 | 0.0  | 0.0        | 0.0  | 343.39      |
      | 2  | 31   | 01 August 2024    |           | 333.22          | 333.23        | 10.16    | 0.0  | 0.0       | 343.39 | 0.0  | 0.0        | 0.0  | 343.39      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 333.22        | 10.16    | 0.0  | 0.0       | 343.38 | 0.0  | 0.0        | 0.0  | 343.38      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 30.16    | 0.0  | 0.0       | 1030.16 | 0.0  | 0.0        | 0.0  | 1030.16     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "15 July 2024"
    When Admin successfully disburse the loan on "15 July 2024" with "250" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.45          | 333.55        | 9.84     | 0.0  | 0.0       | 343.39 | 0.0  | 0.0        | 0.0  | 343.39      |
      |    |      | 15 July 2024      |           | 250.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 2  | 31   | 01 August 2024    |           | 457.65          | 458.8         | 11.56    | 0.0  | 0.0       | 470.36 | 0.0  | 0.0        | 0.0  | 470.36      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 457.65        | 12.7     | 0.0  | 0.0       | 470.35 | 0.0  | 0.0        | 0.0  | 470.35      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1250          | 34.1     | 0.0  | 0.0       | 1284.1 | 0.0  | 0.0        | 0.0  | 1284.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 July 2024     | Disbursement     | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |

  @TestRailId:С3894
  Scenario: Progressive loan - flat interest DAILY, down-payment, multi-disbursement, actual/actual, second disbursement on the due date of installment period
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE | 01 January 2024   | 3000           | 12                     | FLAT          | DAILY                      | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "3000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 0    | 01 June 2024      |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 30   | 01 July 2024      |           | 499.84          | 250.16        | 7.38     | 0.0  | 0.0       | 257.54 | 0.0  | 0.0        | 0.0  | 257.54      |
      | 3  | 31   | 01 August 2024    |           | 249.92          | 249.92        | 7.62     | 0.0  | 0.0       | 257.54 | 0.0  | 0.0        | 0.0  | 257.54      |
      | 4  | 31   | 01 September 2024 |           | 0.0             | 249.92        | 7.62     | 0.0  | 0.0       | 257.54 | 0.0  | 0.0        | 0.0  | 257.54      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 22.62    | 0.0  | 0.0       | 1022.62 | 0.0  | 0.0        | 0.0  | 1022.62     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "01 August 2024"
    When Admin successfully disburse the loan on "01 August 2024" with "250" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 0    | 01 June 2024      |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 30   | 01 July 2024      |           | 499.84          | 250.16        | 7.38     | 0.0  | 0.0       | 257.54 | 0.0  | 0.0        | 0.0  | 257.54      |
      | 3  | 31   | 01 August 2024    |           | 249.92          | 249.92        | 7.62     | 0.0  | 0.0       | 257.54 | 0.0  | 0.0        | 0.0  | 257.54      |
      |    |      | 01 August 2024    |           | 250.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 4  | 0    | 01 August 2024    |           | 437.42          | 62.5          | 0.0      | 0.0  | 0.0       | 62.5   | 0.0  | 0.0        | 0.0  | 62.5        |
      | 5  | 31   | 01 September 2024 |           | 0.0             | 437.42        | 9.53     | 0.0  | 0.0       | 446.95 | 0.0  | 0.0        | 0.0  | 446.95      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1250          | 24.53    | 0.0  | 0.0       | 1274.53 | 0.0  | 0.0        | 0.0  | 1274.53     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 August 2024   | Disbursement     | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       |

  @TestRailId:C3897
  Scenario: Progressive loan - flat interest DAILY, actual/actual, 4% interest rate
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE  | 01 January 2024   | 100            | 4                      | FLAT          | DAILY                      | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024|           | 66.67           | 33.33         | 0.34     | 0.0  | 0.0       | 33.67 | 0.0  | 0.0        | 0.0  | 33.67       |
      | 2  | 29   | 01 March 2024   |           | 33.32           | 33.35         | 0.32     | 0.0  | 0.0       | 33.67 | 0.0  | 0.0        | 0.0  | 33.67       |
      | 3  | 31   | 01 April 2024   |           | 0.0             | 33.32         | 0.34     | 0.0  | 0.0       | 33.66 | 0.0  | 0.0        | 0.0  | 33.66       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 100           | 1.0      | 0.0  | 0.0       | 101.0 | 0.0  | 0.0        | 0.0  | 101.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |

  @TestRailId:C3898
  Scenario: Progressive loan with downpayment - flat interest DAILY, actual/actual, 4% interest rate
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE | 01 January 2024   | 1000           | 4                      | FLAT          | DAILY                      | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024 |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2024|           | 500.05          | 249.95        | 2.54     | 0.0  | 0.0       | 252.49 | 0.0  | 0.0        | 0.0  | 252.49      |
      | 3  | 29   | 01 March 2024   |           | 249.94          | 250.11        | 2.38     | 0.0  | 0.0       | 252.49 | 0.0  | 0.0        | 0.0  | 252.49      |
      | 4  | 31   | 01 April 2024   |           | 0.0             | 249.94        | 2.54     | 0.0  | 0.0       | 252.48 | 0.0  | 0.0        | 0.0  | 252.48      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 7.46     | 0.0  | 0.0       | 1007.46| 0.0  | 0.0        | 0.0  | 1007.46     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |

