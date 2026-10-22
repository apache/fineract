@SerialChargeAccrualConfig
@LoanChargeRealTimeAccrualFeature
Feature: LoanChargeRealTimeAccrual

  @TestRailId:C115338
  Scenario: Verify Progressive Loan - Real-time accrual posting for charges - UC1: charge added under pl-charge-accrual-time real-time is accrued immediately with journal entries and event, and COB does not post it again
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2026   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Global config "charge-accrual-date" value set to "due-date"
    When Global config "pl-charge-accrual-time" value set to "Real-Time"
    When Admin sets the business date to "05 January 2026"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2026" due date and 10 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0           |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "05 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 January 2026"
    When Admin sets the business date to "06 January 2026"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0           |
    When Admin sets the business date to "21 January 2026"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0           |

  @TestRailId:C115339
  Scenario: Verify Progressive Loan - Real-time accrual posting for charges - UC2: multiple charges on the same day create one Accrual each, equal amounts are not deduplicated, and COB adds no adjustment
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2026   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Global config "pl-charge-accrual-time" value set to "real-time"
    When Admin sets the business date to "05 January 2026"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "05 January 2026" due date and 10 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "05 January 2026" due date and 10 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 January 2026" due date and 5 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0           |
      | 05 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0           |
      | 05 January 2026  | Accrual          | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0           |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "05 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
      | ASSET  | 112603       | Interest/Fee Receivable | 5.0   |        |
      | INCOME | 404007       | Fee Income              |       | 5.0    |
    When Admin sets the business date to "06 January 2026"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0           |
      | 05 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0           |
      | 05 January 2026  | Accrual          | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0           |

  @TestRailId:C115340
  Scenario: Verify Progressive Loan - Real-time accrual posting for charges - UC3: default pl-charge-accrual-time EOD keeps the charge accrual on the COB of the due date
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2026   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Global config "charge-accrual-date" value set to "due-date"
    When Global config "pl-charge-accrual-time" value set to "EOD"
    When Admin sets the business date to "05 January 2026"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "08 January 2026" due date and 10 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "08 January 2026"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "09 January 2026"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 08 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0           |

  @TestRailId:C115341
  Scenario: Verify Progressive Loan - Real-time accrual posting for charges - UC4: backdated charge under real-time is accrued immediately with the charge due date as transaction date
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2026   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Global config "pl-charge-accrual-time" value set to "real-time"
    When Admin sets the business date to "10 January 2026"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "05 January 2026" due date and 10 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0           |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 January 2026"
    When Admin sets the business date to "11 January 2026"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0           |

  @TestRailId:C115342
  Scenario: Verify Progressive Loan - Real-time accrual posting for charges - UC5: waiving a charge accrued in real time before its due date books the waiver against the receivable and COB posts no accrual adjustment
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2026   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Global config "pl-charge-accrual-time" value set to "real-time"
    When Admin sets the business date to "05 January 2026"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2026" due date and 10 EUR transaction amount
    When Admin sets the business date to "06 January 2026"
    And Admin waives due date charge
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0           |
      | 06 January 2026  | Waive loan charges | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 1000.0       |
    Then Loan Transactions tab has a "WAIVE_CHARGES" transaction with date "06 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit | Credit |
      | EXPENSE | e4           | Written off             | 10.0  |        |
      | ASSET   | 112603       | Interest/Fee Receivable |       | 10.0   |
    When Admin sets the business date to "07 January 2026"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0           |
      | 06 January 2026  | Waive loan charges | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 1000.0       |

  @TestRailId:C115343
  Scenario: Verify Progressive Loan - Real-time accrual posting for charges - UC6: early closure does not duplicate the accrual of a charge already accrued in real time
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2026   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Global config "pl-charge-accrual-time" value set to "real-time"
    When Admin sets the business date to "05 January 2026"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2026" due date and 10 EUR transaction amount
    When Admin sets the business date to "06 January 2026"
    And Customer makes "AUTOPAY" repayment on "06 January 2026" with 1010 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0           |
      | 06 January 2026  | Repayment        | 1010.0 | 1000.0    | 0.0      | 10.0 | 0.0       | 0.0          |
    When Admin sets the business date to "07 January 2026"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0           |
      | 06 January 2026  | Repayment        | 1010.0 | 1000.0    | 0.0      | 10.0 | 0.0       | 0.0          |

  @TestRailId:C115344
  Scenario: Verify Progressive Loan - Real-time accrual posting for charges - UC7: pl-charge-accrual-time real-time does not apply to a cumulative loan, whose charge is still accrued by COB on the due date
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2026"
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Global config "charge-accrual-date" value set to "due-date"
    When Global config "pl-charge-accrual-time" value set to "real-time"
    When Admin sets the business date to "05 January 2026"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "08 January 2026" due date and 10 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "09 January 2026"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 08 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0           |

  @TestRailId:C115345
  Scenario: Verify Progressive Loan - Real-time accrual posting for charges - UC8: progressive loan on a product without accrual accounting posts no accrual under real-time, neither at charge creation nor at COB
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_ACCOUNTING_RULE_NONE | 01 January 2026   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Global config "pl-charge-accrual-time" value set to "real-time"
    When Admin sets the business date to "05 January 2026"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "05 January 2026" due date and 10 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "06 January 2026"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |

  @TestRailId:C115346
  Scenario: Verify Progressive Loan - Real-time accrual posting for charges - UC9: switching from EOD to real-time accrues a charge added under EOD at the next COB, before its due date, while new charges accrue immediately
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2026   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Global config "charge-accrual-date" value set to "due-date"
    When Global config "pl-charge-accrual-time" value set to "EOD"
    When Admin sets the business date to "05 January 2026"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2026" due date and 10 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "06 January 2026"
    When Global config "pl-charge-accrual-time" value set to "real-time"
    When Admin adds "LOAN_NSF_FEE" due date charge with "25 January 2026" due date and 7 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 06 January 2026  | Accrual          | 7.0    | 0.0       | 0.0      | 0.0  | 7.0       | 0.0           |
    When Admin sets the business date to "07 January 2026"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 06 January 2026  | Accrual          | 7.0    | 0.0       | 0.0      | 0.0  | 7.0       | 0.0           |
      | 06 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0           |

  @TestRailId:C115347
  Scenario: Verify Progressive Loan - Real-time accrual posting for charges - UC10: with enable-immediate-charge-accrual-post-maturity also enabled, a post-maturity charge under real-time is accrued exactly once
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2026   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "05 February 2026"
    When Admin runs inline COB job for Loan
    When Global configuration "enable-immediate-charge-accrual-post-maturity" is enabled
    When Global config "pl-charge-accrual-time" value set to "real-time"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "05 February 2026" due date and 20 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 February 2026 | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0           |
    When Admin sets the business date to "06 February 2026"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 February 2026 | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0           |

  @TestRailId:C115348
  Scenario: Verify Progressive Loan - Real-time accrual posting for charges - UC11: undo disbursal reverses the real-time charge accrual and COB re-accrues the charge after re-disbursement
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2026   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Global config "pl-charge-accrual-time" value set to "real-time"
    When Admin sets the business date to "05 January 2026"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2026" due date and 10 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0           |
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    And Admin successfully disburse the loan on "05 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "06 January 2026"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 05 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0           |

  @TestRailId:C115349
  Scenario: Verify Progressive Loan - Real-time accrual posting for charges - UC12: charge-off and charge-off undo keep a single real-time charge accrual and COB posts no adjustment
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2026   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Global config "pl-charge-accrual-time" value set to "real-time"
    When Admin sets the business date to "05 January 2026"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2026" due date and 10 EUR transaction amount
    When Admin sets the business date to "06 January 2026"
    And Admin does charge-off the loan on "06 January 2026"
    When Admin sets the business date to "07 January 2026"
    And Admin does a charge-off undo the loan
    When Admin sets the business date to "08 January 2026"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0           |
      | 06 January 2026  | Charge-off       | 1010.0 | 1000.0    | 0.0      | 10.0 | 0.0       | 0.0          |
    Then On Loan Transactions tab the "Charge-off" Transaction with date "06 January 2026" is reverted

  @TestRailId:C115350
  Scenario: Verify Progressive Loan - Real-time accrual posting for charges - UC13: charge added after maturity to a closed loan reopens it with an N+1 installment and is accrued immediately under real-time
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2026   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "05 January 2026"
    And Customer makes "AUTOPAY" repayment on "05 January 2026" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Global config "pl-charge-accrual-time" value set to "real-time"
    When Admin sets the business date to "05 February 2026"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "05 February 2026" due date and 20 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2026 | 05 January 2026 | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 1000.0     | 0.0  | 0.0         |
      | 2  | 4    | 05 February 2026 |                 | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0   | 0.0    | 0.0        | 0.0  | 20.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2026  | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
      | 05 February 2026 | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 February 2026"
    When Admin sets the business date to "06 February 2026"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2026  | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
      | 05 February 2026 | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |

  @TestRailId:C115351
  Scenario: Verify Progressive Loan - Real-time accrual posting for charges - UC14: switching back from real-time to EOD keeps the real-time accrual and the due-date COB posts neither a second accrual nor an adjustment
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2026   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Global config "charge-accrual-date" value set to "due-date"
    When Global config "pl-charge-accrual-time" value set to "real-time"
    When Admin sets the business date to "05 January 2026"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "08 January 2026" due date and 10 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
    When Global config "pl-charge-accrual-time" value set to "EOD"
    When Admin sets the business date to "09 January 2026"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |

  @TestRailId:C115352
  Scenario: Verify Progressive Loan - Real-time accrual posting for charges - UC15: real-time accrual behaves the same under charge-accrual-date submitted-date, charge accrued at posting and not again at COB
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2026   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Global config "charge-accrual-date" value set to "submitted-date"
    When Global config "pl-charge-accrual-time" value set to "real-time"
    When Admin sets the business date to "05 January 2026"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2026" due date and 10 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
    When Admin sets the business date to "06 January 2026"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2026  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
