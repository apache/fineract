@WriteOffFeature
  Feature: Write-off

    @TestRailId:C2934
    Scenario: As a user I want to do Write-off a loan and verify that undo repayment post write-off results in error
      When Admin sets the business date to "1 January 2023"
      And Admin creates a client with random data
      When Admin creates a fully customized loan with the following data:
        | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
        | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
      And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
      And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
      When Admin sets the business date to "22 January 2023"
      And Customer makes "AUTOPAY" repayment on "22 January 2023" with 100 EUR transaction amount
      When Admin sets the business date to "29 January 2023"
      And Admin does write-off the loan on "29 January 2023"
      Then Loan status will be "CLOSED_WRITTEN_OFF"
      Then Loan Transactions tab has a transaction with date: "29 January 2023", and with the following data:
        | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
        | Close (as written-off) | 650.0  | 650.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      Then Admin fails to undo "1"th transaction made on "22 January 2023"


    @TestRailId:C2935
    Scenario: As a user I want to do Write-off a loan and verify that backdate repayment post write-off results in error
      When Admin sets the business date to "1 January 2023"
      And Admin creates a client with random data
      When Admin creates a fully customized loan with the following data:
        | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
        | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
      And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
      And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
      When Admin sets the business date to "22 January 2023"
      And Customer makes "AUTOPAY" repayment on "22 January 2023" with 100 EUR transaction amount
      When Admin sets the business date to "29 January 2023"
      And Admin does write-off the loan on "29 January 2023"
      Then Loan status will be "CLOSED_WRITTEN_OFF"
      Then Loan Transactions tab has a transaction with date: "29 January 2023", and with the following data:
        | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
        | Close (as written-off) | 650.0  | 650.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      Then Loan "AUTOPAY" repayment transaction on "26 January 2023" with 50 EUR transaction amount results in error


    @TestRailId:C2936
    Scenario: As a user I want to do Write-off a loan and verify that undo write-off results in error
      When Admin sets the business date to "1 January 2023"
      And Admin creates a client with random data
      When Admin creates a fully customized loan with the following data:
        | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
        | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
      And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
      And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
      When Admin sets the business date to "22 January 2023"
      And Customer makes "AUTOPAY" repayment on "22 January 2023" with 100 EUR transaction amount
      When Admin sets the business date to "29 January 2023"
      And Admin does write-off the loan on "29 January 2023"
      Then Loan status will be "CLOSED_WRITTEN_OFF"
      Then Loan Transactions tab has a transaction with date: "29 January 2023", and with the following data:
        | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
        | Close (as written-off) | 650.0  | 650.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      Then Admin fails to undo "1"th transaction made on "29 January 2023"

    @TestRailId:C4006
    Scenario: Verify accounting journal entries are not duplicated during write-off in case the cumulative loan was already charged-off
      When Admin sets the business date to "1 January 2023"
      And Admin creates a client with random data
      When Admin creates a fully customized loan with the following data:
        | LoanProduct       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
        | LP1_INTEREST_FLAT | 1 January 2023    | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
      And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
      And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
      And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
      When Admin sets the business date to "22 February 2023"
      And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "22 February 2023"
      And Admin does charge-off the loan on "22 February 2023"
      Then Loan marked as charged-off on "22 February 2023"
      Then Loan status will be "ACTIVE"
      Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
        | Type      | Account code | Account name              | Debit  | Credit |
        | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
        | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
      Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
        | Type    | Account code | Account name               | Debit  | Credit |
        | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
        | ASSET   | 112603       | Interest/Fee Receivable    |        | 143.0  |
        | EXPENSE | 744007       | Credit Loss/Bad Debt       | 1000.0 |        |
        | INCOME  | 404001       | Interest Income Charge Off | 30.0   |        |
        | INCOME  | 404008       | Fee Charge Off             | 113.0  |        |
      When Admin sets the business date to "1 March 2023"
      And Admin does write-off the loan on "1 March 2023"
      Then Loan status will be "CLOSED_WRITTEN_OFF"
      Then Loan Transactions tab has a "WRITE_OFF" transaction with date "01 March 2023" which has the following Journal entries:
        | Type    | Account code | Account name               | Debit  | Credit |
        | EXPENSE | 744007       | Credit Loss/Bad Debt       |        | 1000.0 |
        | INCOME  | 404001       | Interest Income Charge Off |        | 30.0   |
        | INCOME  | 404008       | Fee Charge Off             |        | 113.0  |
        | EXPENSE | e4           | Written off                | 1143.0 |        |

    @TestRailId:C4007
    Scenario: Verify accounting journal entries during write-off when cumulative loan was not charged-off before
      When Admin sets the business date to "1 January 2023"
      And Admin creates a client with random data
      When Admin creates a fully customized loan with the following data:
        | LoanProduct       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
        | LP1_INTEREST_FLAT | 1 January 2023    | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
      And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
      And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
      And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
      When Admin sets the business date to "22 February 2023"
      And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "22 February 2023"
      Then Loan status will be "ACTIVE"
      Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
        | Type      | Account code | Account name              | Debit  | Credit |
        | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
        | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
      When Admin sets the business date to "1 March 2023"
      And Admin does write-off the loan on "1 March 2023"
      Then Loan status will be "CLOSED_WRITTEN_OFF"
      Then Loan Transactions tab has a "WRITE_OFF" transaction with date "01 March 2023" which has the following Journal entries:
        | Type    | Account code | Account name               | Debit  | Credit |
        | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
        | ASSET   | 112603       | Interest/Fee Receivable    |        | 143.0  |
        | EXPENSE | e4           | Written off                | 1143.0 |        |

    @TestRailId:C4010
    Scenario: Verify accounting journal entries are not duplicated during write-off in case the progressive loan was already charged-off
      When Admin sets the business date to "01 January 2024"
      When Admin creates a client with random data
      When Admin creates a fully customized loan with the following data:
        | LoanProduct                                                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
        | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE_PMT_ALLOC_1 | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
      And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
      When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
      Then Loan Repayment schedule has 3 periods, with the following data for periods:
        | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
        |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
        | 1  | 31   | 01 February 2024 |           | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
        | 2  | 29   | 01 March 2024    |           | 335.27          | 333.33        | 3.9      | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
        | 3  | 31   | 01 April 2024    |           | 0.0             | 335.27        | 1.96     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      Then Loan Repayment schedule has the following data in Total row:
        | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
        | 1000          | 11.69    | 0    | 0         | 1011.69 | 0    | 0          | 0    | 1011.69     |
      Then Loan Transactions tab has the following data:
        | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
        | 01 January 2024  | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0        | false    | false    |
      When Admin sets the business date to "08 February 2024"
      When Admin sets the business date to "09 February 2024"
      And Admin does charge-off the loan on "09 February 2024"
      Then Loan Repayment schedule has 3 periods, with the following data for periods:
        | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
        |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
        | 1  | 31   | 01 February 2024 |           | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
        | 2  | 29   | 01 March 2024    |           | 335.8           | 332.8         | 4.43     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
        | 3  | 31   | 01 April 2024    |           | 0.0             | 335.8         | 1.96     | 0.0  | 0.0       | 337.76 | 0.0  | 0.0        | 0.0  | 337.76      |
      Then Loan Repayment schedule has the following data in Total row:
        | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
        | 1000          | 12.22    | 0    | 0         | 1012.22 | 0    | 0          | 0    | 1012.22     |
      Then Loan Transactions tab has the following data:
        | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
        | 01 January 2024  | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
        | 09 February 2024 | Accrual          | 7.44    | 0.0       | 7.44     | 0.0  | 0.0       | 0.0          | false    | false    |
        | 09 February 2024 | Charge-off       | 1012.22 | 1000.0    | 12.22    | 0.0  | 0.0       | 0.0          | false    | false    |
      Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "09 February 2024" which has the following Journal entries:
        | Type    | Account code | Account name               | Debit  | Credit |
        | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
        | ASSET   | 112603       | Interest/Fee Receivable    |        | 12.22  |
        | EXPENSE | 744007       | Credit Loss/Bad Debt       | 1000.0 |        |
        | INCOME  | 404001       | Interest Income Charge Off | 12.22  |        |
      When Admin sets the business date to "01 March 2024"
      And Admin does write-off the loan on "01 March 2024"
      Then Loan Repayment schedule has 3 periods, with the following data for periods:
        | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
        |    |      | 01 January 2024  |               | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
        | 1  | 31   | 01 February 2024 | 01 March 2024 | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 0.0         |
        | 2  | 29   | 01 March 2024    | 01 March 2024 | 335.8           | 332.8         | 4.43     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 0.0         |
        | 3  | 31   | 01 April 2024    | 01 March 2024 | 0.0             | 335.8         | 1.96     | 0.0  | 0.0       | 337.76 | 0.0  | 0.0        | 0.0  | 0.0         |
      Then Loan Repayment schedule has the following data in Total row:
        | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
        | 1000          | 12.22    | 0    | 0         | 1012.22 | 0    | 0          | 0    | 0.0         |
      Then Loan Transactions tab has the following data:
        | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
        | 01 January 2024  | Disbursement           | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
        | 09 February 2024 | Accrual                | 7.44    | 0.0       | 7.44     | 0.0  | 0.0       | 0.0          | false    | false    |
        | 09 February 2024 | Charge-off             | 1012.22 | 1000.0    | 12.22    | 0.0  | 0.0       | 0.0          | false    | false    |
        | 01 March 2024    | Close (as written-off) | 1012.22 | 1000.0    | 12.22    | 0.0  | 0.0       | 0.0          | false    | false    |
      Then Loan Transactions tab has a "WRITE_OFF" transaction with date "01 March 2024" which has the following Journal entries:
        | Type    | Account code | Account name               | Debit   | Credit |
        | EXPENSE | 744007       | Credit Loss/Bad Debt       |         | 1000.0 |
        | INCOME  | 404001       | Interest Income Charge Off |         | 12.22  |
        | EXPENSE | e4           | Written off                | 1012.22 |        |

    @TestRailId:C4011
    Scenario: Verify accounting journal entries are not duplicated during write-off in case the progressive loan was already charged-off after repayment
      When Admin sets the business date to "1 January 2024"
      And Admin creates a client with random data
      And Admin creates a fully customized loan with the following data:
        | LoanProduct                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
        | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
      When Admin sets the business date to "1 February 2024"
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
        | 100           | 2.05     | 0    | 0         | 102.05 | 17.01 | 0          | 0    | 85.04       |
      When Admin sets the business date to "1 March 2024"
      And Admin does charge-off the loan on "1 March 2024"
      Then Loan Repayment schedule has 6 periods, with the following data for periods:
        | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
        |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
        | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
        | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
        | 3  | 31   | 01 April 2024    |                  | 50.04           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
        | 4  | 30   | 01 May 2024      |                  | 33.03           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
        | 5  | 31   | 01 June 2024     |                  | 16.02           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
        | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.02         | 0.0      | 0.0  | 0.0       | 16.02 | 0.0   | 0.0        | 0.0  | 16.02       |
      Then Loan Repayment schedule has the following data in Total row:
        | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
        | 100           | 1.07     | 0    | 0         | 101.07 | 17.01 | 0          | 0    | 84.06       |
      Then Loan Transactions tab has the following data:
        | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
        | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
        | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
        | 01 March 2024    | Accrual          | 1.07   | 0.0       | 1.07     | 0.0  | 0.0       | 0.0          | false    | false    |
        | 01 March 2024    | Charge-off       | 84.06  | 83.57     | 0.49     | 0.0  | 0.0       | 0.0          | false    | false    |
      Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "01 March 2024" which has the following Journal entries:
        | Type    | Account code | Account name               | Debit  | Credit |
        | ASSET   | 112601       | Loans Receivable           |        | 83.57  |
        | ASSET   | 112603       | Interest/Fee Receivable    |        | 0.49   |
        | EXPENSE | 744007       | Credit Loss/Bad Debt       | 83.57  |        |
        | INCOME  | 404001       | Interest Income Charge Off | 0.49   |        |
      And Admin does write-off the loan on "01 March 2024"
      Then Loan Repayment schedule has 6 periods, with the following data for periods:
        | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
        |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
        | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
        | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 0.0         |
        | 3  | 31   | 01 April 2024    | 01 March 2024    | 50.43           | 16.62         | 0.39      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 0.0         |
        | 4  | 30   | 01 May 2024      | 01 March 2024    | 33.71           | 16.72         | 0.29      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 0.0         |
        | 5  | 31   | 01 June 2024     | 01 March 2024    | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 0.0         |
        | 6  | 30   | 01 July 2024     | 01 March 2024    | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0 | 0.0   | 0.0        | 0.0  | 0.0         |
      Then Loan Repayment schedule has the following data in Total row:
        | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
        | 100           | 2.05     | 0    | 0         | 102.05 | 17.01 | 0          | 0    | 0.0         |
      Then Loan Transactions tab has the following data:
        | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
        | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
        | 01 February 2024 | Repayment              | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
        | 01 March 2024    | Accrual                | 1.07   | 0.0       | 1.07     | 0.0  | 0.0       | 0.0          | false    | false    |
        | 01 March 2024    | Charge-off             | 84.06  | 83.57     | 0.49     | 0.0  | 0.0       | 0.0          | false    | false    |
        | 01 March 2024    | Close (as written-off) | 85.04  | 83.57     | 1.47     | 0.0  | 0.0       | 0.0          | false    | false    |
      Then Loan Transactions tab has a "WRITE_OFF" transaction with date "01 March 2024" which has the following Journal entries:
        | Type    | Account code | Account name               | Debit | Credit |
        | EXPENSE | 744007       | Credit Loss/Bad Debt       |       | 83.57  |
        | INCOME  | 404001       | Interest Income Charge Off |       | 1.47   |
        | EXPENSE | e4           | Written off                | 85.04 |        |

    @TestRailId:C4012
    Scenario: Verify accounting journal entries during write-off in case the progressive loan was already charged-off and then charge-off is undone
      When Admin sets the business date to "1 January 2024"
      And Admin creates a client with random data
      And Admin creates a fully customized loan with the following data:
        | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
        | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_DELINQUENT_REASON | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
      Then Loan Repayment schedule has 6 periods, with the following data for periods:
        | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
        |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
        | 1  | 31   | 01 February 2024 |           | 83.58           | 16.42         | 0.58     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
        | 2  | 29   | 01 March 2024    |           | 67.07           | 16.51         | 0.49     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
        | 3  | 31   | 01 April 2024    |           | 50.46           | 16.61         | 0.39     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
        | 4  | 30   | 01 May 2024      |           | 33.75           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
        | 5  | 31   | 01 June 2024     |           | 16.95           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
        | 6  | 30   | 01 July 2024     |           | 0.0             | 16.95         | 0.1      | 0.0  | 0.0       | 17.05 | 0.0  | 0.0        | 0.0  | 17.05       |
      Then Loan Repayment schedule has the following data in Total row:
        | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
        | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
      And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
      And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
      Then Admin can successfully set Fraud flag to the loan
      When Admin sets the business date to "03 February 2024"
      And Admin does charge-off the loan with reason "DELINQUENT" on "03 February 2024"
      Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "03 February 2024" which has the following Journal entries:
        | Type    | Account code | Account name               | Debit | Credit |
        | ASSET   | 112601       | Loans Receivable           |       | 100.0  |
        | ASSET   | 112603       | Interest/Fee Receivable    |       | 0.61   |
        | EXPENSE | 744007       | Credit Loss/Bad Debt       | 100.0 |        |
        | INCOME  | 404001       | Interest Income Charge Off | 0.61  |        |
      Then Admin does a charge-off undo the loan
      Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "03 February 2024" which has the following Journal entries:
        | Type    | Account code | Account name               | Debit | Credit |
        | ASSET   | 112601       | Loans Receivable           |       | 100.0  |
        | ASSET   | 112601       | Loans Receivable           | 100.0 |        |
        | ASSET   | 112603       | Interest/Fee Receivable    |       | 0.61   |
        | ASSET   | 112603       | Interest/Fee Receivable    | 0.61  |        |
        | EXPENSE | 744007       | Credit Loss/Bad Debt       | 100.0 |        |
        | EXPENSE | 744007       | Credit Loss/Bad Debt       |       | 100.0  |
        | INCOME  | 404001       | Interest Income Charge Off | 0.61  |        |
        | INCOME  | 404001       | Interest Income Charge Off |       | 0.61   |
      And Admin does write-off the loan on "03 February 2024"
      Then Loan Transactions tab has a "WRITE_OFF" transaction with date "03 February 2024" which has the following Journal entries:
        | Type    | Account code | Account name               | Debit   | Credit |
        | ASSET   | 112601       | Loans Receivable           |         | 100.0  |
        | ASSET   | 112603       | Interest/Fee Receivable    |         | 2.05   |
        | EXPENSE | e4           | Written off                | 102.05  |        |

    @TestRailId:C4013
    Scenario: Verify accounting journal entries are not duplicated during write-off in case the progressive reverse-replayed fraud loan was charged-off
      When Admin sets the business date to "01 February 2024"
      When Admin creates a client with random data
      When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
      When Admin creates a fully customized loan with the following data:
        | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
        | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 February 2024  | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
      And Admin successfully approves the loan on "01 February 2024" with "1000" amount and expected disbursement date on "01 February 2024"
      When Admin successfully disburse the loan on "01 February 2024" with "1000" EUR transaction amount
      When Admin sets the business date to "02 February 2024"
      And Customer makes "AUTOPAY" repayment on "02 February 2024" with 100 EUR transaction amount
      When Admin sets the business date to "03 February 2024"
      Then Admin can successfully set Fraud flag to the loan
      When Admin sets the business date to "03 February 2024"
      And Admin does charge-off the loan on "03 February 2024"
      Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "03 February 2024" which has the following Journal entries:
        | Type    | Account code | Account name               | Debit | Credit |
        | ASSET   | 112601       | Loans Receivable           |       | 650.0  |
        | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 650.0 |        |
      When Admin sets the business date to "04 February 2024"
      When Customer undo "1"th "Repayment" transaction made on "02 February 2024"
      Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "03 February 2024" which has the following Journal entries:
        | Type    | Account code | Account name               | Debit | Credit |
        | ASSET   | 112601       | Loans Receivable           |       | 750.0  |
        | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 750.0 |        |
      Then In Loan transactions the replayed "CHARGE_OFF" transaction with date "03 February 2024" has a reverted transaction pair with the following Journal entries:
        | Type    | Account code | Account name               | Debit | Credit |
        | ASSET   | 112601       | Loans Receivable           |       | 650.0  |
        | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 650.0 |        |
        | ASSET   | 112601       | Loans Receivable           | 650.0 |        |
        | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud |       | 650.0  |
      And Admin does write-off the loan on "03 February 2024"
      Then Loan Transactions tab has a "WRITE_OFF" transaction with date "03 February 2024" which has the following Journal entries:
        | Type    | Account code | Account name               | Debit | Credit |
        | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud |       | 750.0  |
        | EXPENSE | e4           | Written off                | 750.0 |        |
