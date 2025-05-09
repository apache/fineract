@CapitalizedIncomeFeature
Feature: Capitalized Income

  @TestRailId:C3635
  Scenario: As a user I want to add capitalized income to a progressive loan after disbursement for multidisbursal loan with downpayment - UC1
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | 1 January 2024    | 1000.0         | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "900" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "2 January 2024" with "100" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 01 January 2024  | Down Payment       | 225.0  | 225.0     | 0.0      | 0.0  | 0.0       | 675.0        | false    |
      | 02 January 2024  | Capitalized Income | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 775.0        | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 100.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 100.0  |

  @TestRailId:C3637
  Scenario: As a user I want to add capitalized income to a progressive loan after disbursement - UC2
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "900" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "2 January 2024" with "100" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 100.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 100.0  |

  @TestRailId:C3638
  Scenario: Verify capitalized income amount with disbursement amount calculation within approved amount amount for multidisbursal progressive loan - UC3
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSAL_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "600" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin successfully disburse the loan on "2 January 2024" with "200" EUR transaction amount
    When Admin sets the business date to "2 January 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "3 January 2024" with "200" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 600.0  | 0.0       | 0.0      | 0.0  | 0.0       | 600.0        | false    |
      | 02 January 2024  | Disbursement       | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0         | false    |
      | 03 January 2024  | Capitalized Income | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 1000.0        | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "03 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 200.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 200.0  |

  @TestRailId:C3639
  Scenario: Verify validation of capitalized income amount with disbursement amount within approved amount for progressive loan - UC4
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "900" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    Then Capitalized income with payment type "AUTOPAY" on "02 January 2024" is forbidden with amount "200" while exceed approved amount

  @TestRailId:C3640
  Scenario: Verify validation of capitalized income amount with disbursement amount within approved amount for multidisbursal progressive loan - UC5
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | 1 January 2024    | 1000.0         | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "500" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin successfully disburse the loan on "2 January 2024" with "300" EUR transaction amount
    Then Capitalized income with payment type "AUTOPAY" on "2 January 2024" is forbidden with amount "300" while exceed approved amount

  @TestRailId:C3641
  Scenario: Verify validation of capitalized income amount with disbursement amount within approved amount after undo disbursement - UC6
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "900" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "2 January 2024" with "100" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 100.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 100.0  |
    When Admin successfully undo disbursal
    Then Loan status will be "APPROVED"
    When Admin sets the business date to "3 January 2024"
    And Admin successfully disburse the loan on "3 January 2024" with "800" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "3 January 2024" with "200" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 03 January 2024  | Disbursement       | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    |
      | 03 January 2024  | Capitalized Income | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "03 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 200.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 200.0  |

  @TestRailId:C3642
  Scenario: Verify capitalized income amount with disbursement amount calculation within approved amount amount for multidisbursal loan after undo disbursement - UC7
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSAL_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "600" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin successfully disburse the loan on "2 January 2024" with "200" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "2 January 2024" with "200" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 600.0  | 0.0       | 0.0      | 0.0  | 0.0       | 600.0        | false    |
      | 02 January 2024  | Disbursement       | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    |
      | 02 January 2024  | Capitalized Income | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 200.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 200.0  |
    When Admin successfully undo disbursal
    Then Loan status will be "APPROVED"
    When Admin sets the business date to "3 January 2024"
    And Admin successfully disburse the loan on "3 January 2024" with "500" EUR transaction amount
    When Admin sets the business date to "4 January 2024"
    And Admin successfully disburse the loan on "4 January 2024" with "200" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "4 January 2024" with "300" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 03 January 2024  | Disbursement       | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    |
      | 04 January 2024  | Disbursement       | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    |
      | 04 January 2024  | Capitalized Income | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "04 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 300.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 300.0  |

  @TestRailId:C3646
  Scenario: As a user I want to add capitalized income to a progressive loan after disbursement and then make a full repayment - amortization in case of loan close event
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "900" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "2 January 2024" with "100" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 100.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 100.0  |
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "2 January 2024" with 900.17 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement                    | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 02 January 2024  | Repayment                       | 900.17 | 900.0     | 0.17     | 0.0  | 0.0       | 100.0        | false    |
      | 02 January 2024  | Accrual                         | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2024  | Capitalized Income Amortization | 100.0  | 0.0       | 100.0    | 0.0  | 0.0       | 0.0          | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | INCOME    | 404000       | Interest Income              |        | 100.0  |
      | LIABILITY | 145024       | Deferred Capitalized Income  | 100.0  |        |
    Then LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent is raised on "02 January 2024"

  @TestRailId:C3647
  Scenario: As a user I want to add capitalized income to a progressive loan after disbursement and then overpay loan - amortization in case of loan close event
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "900" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "2 January 2024" with "100" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 100.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 100.0  |
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "2 January 2024" with 1000 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement                    | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 02 January 2024  | Repayment                       | 1000.0 | 900.0     | 0.17     | 0.0  | 0.0       | 100.0        | false    |
      | 02 January 2024  | Accrual                         | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2024  | Capitalized Income Amortization | 100.0  | 0.0       | 100.0    | 0.0  | 0.0       | 0.0          | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | INCOME    | 404000       | Interest Income              |        | 100.0  |
      | LIABILITY | 145024       | Deferred Capitalized Income  | 100.0  |        |
    Then LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent is raised on "02 January 2024"

  @TestRailId:C3651
  Scenario: As a user I want to add capitalized income to a progressive loan after disbursement and then write off loan - amortization in case of loan close event
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "900" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "2 January 2024" with "100" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 100.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 100.0  |
    And Admin does write-off the loan on "02 January 2024"
    Then Loan status will be "CLOSED_WRITTEN_OFF"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement                    | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 02 January 2024  | Close (as written-off)          | 905.25 | 900.0     | 5.25     | 0.0  | 0.0       | 100.0        | false    |
      | 02 January 2024  | Capitalized Income Amortization | 100.0  | 0.0       | 100.0    | 0.0  | 0.0       | 0.0          | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | EXPENSE   | e4           | Written off                  |        | 100.0  |
      | LIABILITY | 145024       | Deferred Capitalized Income  | 100.0  |        |
    Then LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent is raised on "02 January 2024"
  @TestRailId:C3648
  Scenario: Verify capitalized income: daily amortization - Capitalized Income type: interest
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2024   | 200            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "01 January 2024" with "50" EUR transaction amount
    Then Loan status will be "ACTIVE"
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Capitalized Income | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   |
      | ASSET     | 112601       | Loans Receivable            | 50.0  |        |
    When Admin sets the business date to "02 January 2024"
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "01 February 2024"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "01 March 2024"
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "01 April 2024"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 01 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 404000       | Interest Income             |       | 0.55   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 0.55  |        |
    And Loan Capitalized Income Amortization Transaction Created Business Event is created on "31 March 2024"

  @TestRailId:C3649
  Scenario: Verify capitalized income: daily amortization - Capitalized Income type: fee
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_FEE | 01 January 2024   | 200            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "01 January 2024" with "50" EUR transaction amount
    Then Loan status will be "ACTIVE"
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Capitalized Income | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   |
      | ASSET     | 112601       | Loans Receivable            | 50.0  |        |
    When Admin sets the business date to "02 January 2024"
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "01 February 2024"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "01 March 2024"
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "01 April 2024"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 01 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |

      | 01 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 16 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |

      | 01 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 02 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 04 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 05 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 06 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 07 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 08 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 09 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 10 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 11 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 12 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 13 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 14 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 16 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 17 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 18 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 19 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 20 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 21 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 22 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.0     | 0.54  | 0.0       | 0.0          | false    | false    |
      | 23 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.0     | 0.54  | 0.0       | 0.0          | false    | false    |
      | 25 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 26 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.0     | 0.54  | 0.0       | 0.0          | false    | false    |
      | 27 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 28 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.0     | 0.54  | 0.0       | 0.0          | false    | false    |
      | 29 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 30 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.0     | 0.54  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 404000       | Interest Income             |       | 0.55   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 0.55  |        |
    And Loan Capitalized Income Amortization Transaction Created Business Event is created on "31 March 2024"
