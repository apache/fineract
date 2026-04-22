@LoanDelinquencyFeature
Feature: LoanDelinquency - Part2

  @TestRailId:C3936
  Scenario: Verify nextPaymentAmount value overpayment first installment - progressive loan, interest recalculation daily - UC7
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 June 2024      | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 2  | 31   | 01 August 2024    |           | 335.27          | 333.33        | 3.9      | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 335.27        | 1.96     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 11.69    | 0.0  | 0.0       | 1011.69 | 0.0  | 0.0        | 0.0  | 1011.69     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 337.23            |

    When Admin sets the business date to "25 June 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "25 June 2024" with 400 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 July 2024      | 25 June 2024 | 667.44          | 332.56        | 4.67     | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    |              | 334.44          | 333.0         | 4.23     | 0.0  | 0.0       | 337.23 | 62.77  | 62.77      | 0.0  | 274.46      |
      | 3  | 31   | 01 September 2024 |              | 0.0             | 334.44        | 1.95     | 0.0  | 0.0       | 336.39 | 0.0    | 0.0        | 0.0  | 336.39      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000          | 10.85    | 0.0  | 0.0       | 1010.85 | 400.0  | 400.0      | 0.0  | 610.85      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 24 June 2024     | Accrual                 | 4.47   | 0.0       | 4.47     | 0.0  | 0.0       | 0.0          |
      | 25 June 2024     | Repayment               | 400.0  | 395.33    | 4.67     | 0.0  | 0.0       | 604.67       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 August 2024     | 274.46            |

    When Admin sets the business date to "01 August 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 July 2024      | 25 June 2024 | 667.44          | 332.56        | 4.67     | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    |              | 334.44          | 333.0         | 4.23     | 0.0  | 0.0       | 337.23 | 62.77  | 62.77      | 0.0  | 274.46      |
      | 3  | 31   | 01 September 2024 |              | 0.0             | 334.44        | 1.95     | 0.0  | 0.0       | 336.39 | 0.0    | 0.0        | 0.0  | 336.39      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000          | 10.85    | 0.0  | 0.0       | 1010.85 | 400.0  | 400.0      | 0.0  | 610.85      |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 August 2024     | 274.46            |

    When Admin sets the business date to "03 August 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 July 2024      | 25 June 2024 | 667.44          | 332.56        | 4.67     | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    |              | 334.44          | 333.0         | 4.23     | 0.0  | 0.0       | 337.23 | 62.77  | 62.77      | 0.0  | 274.46      |
      | 3  | 31   | 01 September 2024 |              | 0.0             | 334.44        | 2.05     | 0.0  | 0.0       | 336.49 | 0.0    | 0.0        | 0.0  | 336.49      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000          | 10.95    | 0.0  | 0.0       | 1010.95 | 400.0  | 400.0      | 0.0  | 610.95      |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 August 2024     | 274.46            |

    When Loan Pay-off is made on "3 August 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3937
  Scenario: Verify nextPaymentAmount value for the last installment - progressive loan, interest recalculation daily, next installment - UC8
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 June 2024      | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 2  | 31   | 01 August 2024    |           | 335.27          | 333.33        | 3.9      | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 335.27        | 1.96     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 11.69    | 0.0  | 0.0       | 1011.69 | 0.0  | 0.0        | 0.0  | 1011.69     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 337.23            |

    When Admin sets the business date to "15 August 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 August 2024" with 337.23 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 June 2024      |                | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 01 July 2024      | 15 August 2024 | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 337.23 | 0.0        | 337.23 | 0.0         |
      | 2  | 31   | 01 August 2024    |                | 337.2           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0    | 0.0        | 0.0    | 337.23      |
      | 3  | 31   | 01 September 2024 |                | 0.0             | 337.2         | 3.71     | 0.0  | 0.0       | 340.91 | 0.0    | 0.0        | 0.0    | 340.91      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 1000          | 15.37    | 0.0  | 0.0       | 1015.37 | 337.23 | 0.0        | 337.23 | 678.14      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 14 August 2024   | Accrual                 | 12.18  | 0.0       | 12.18    | 0.0  | 0.0       | 0.0          |
      | 15 August 2024   | Repayment               | 337.23 | 331.4     | 5.83     | 0.0  | 0.0       | 668.6        |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | RANGE_3        | 15 August 2024     | 340.91            |

    When Admin sets the business date to "01 September 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 June 2024      |                | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 01 July 2024      | 15 August 2024 | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 337.23 | 0.0        | 337.23 | 0.0         |
      | 2  | 31   | 01 August 2024    |                | 337.2           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0    | 0.0        | 0.0    | 337.23      |
      | 3  | 31   | 01 September 2024 |                | 0.0             | 337.2         | 4.77     | 0.0  | 0.0       | 341.97 | 0.0    | 0.0        | 0.0    | 341.97      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 1000          | 16.43    | 0.0  | 0.0       | 1016.43 | 337.23 | 0.0        | 337.23 | 679.2       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | RANGE_3        | 15 August 2024     | 341.97            |

    When Admin sets the business date to "03 September 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 June 2024      |                | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 01 July 2024      | 15 August 2024 | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 337.23 | 0.0        | 337.23 | 0.0         |
      | 2  | 31   | 01 August 2024    |                | 337.2           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0    | 0.0        | 0.0    | 337.23      |
      | 3  | 31   | 01 September 2024 |                | 0.0             | 337.2         | 4.77     | 0.0  | 0.0       | 341.97 | 0.0    | 0.0        | 0.0    | 341.97      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 1000          | 16.43    | 0.0  | 0.0       | 1016.43 | 337.23 | 0.0        | 337.23 | 679.2       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | RANGE_3        | 15 August 2024     | 341.97            |

    When Loan Pay-off is made on "3 September 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3938
  Scenario: Verify nextPaymentAmount value for the last installment - progressive loan, interest recalculation daily, last installment - UC9
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "LAST_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 June 2024      | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 2  | 31   | 01 August 2024    |           | 335.27          | 333.33        | 3.9      | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 335.27        | 1.96     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 11.69    | 0.0  | 0.0       | 1011.69 | 0.0  | 0.0        | 0.0  | 1011.69     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |

    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 337.23            |

    When Admin sets the business date to "15 June 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 June 2024" with 337.23 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |       |             |
      | 1  | 30   | 01 July 2024      |              | 667.55          | 332.45        | 4.78     | 0.0  | 0.0       | 337.23 | 0.0    | 0.0        | 0.0   | 337.23      |
      | 2  | 31   | 01 August 2024    |              | 337.23          | 330.32        | 1.93     | 0.0  | 0.0       | 332.25 | 0.0    | 0.0        | 0.0   | 332.25      |
      | 3  | 31   | 01 September 2024 | 15 June 2024 | 0.0             | 337.23        | 0.0      | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late  | Outstanding |
      | 1000          | 6.71     | 0.0  | 0.0       | 1006.71 | 337.23 | 337.23     | 0.0   | 669.48      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 14 June 2024     | Accrual                 | 2.53   | 0.0       | 2.53     | 0.0  | 0.0       | 0.0          |
      | 15 June 2024     | Repayment               | 337.23 | 337.23    | 0.0      | 0.0  | 0.0       | 662.77       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 337.23            |

    When Admin sets the business date to "15 July 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |       |             |
      | 1  | 30   | 01 July 2024      |              | 667.55          | 332.45        | 4.78     | 0.0  | 0.0       | 337.23 | 0.0    | 0.0        | 0.0   | 337.23      |
      | 2  | 31   | 01 August 2024    |              | 337.23          | 330.32        | 2.8      | 0.0  | 0.0       | 333.12 | 0.0    | 0.0        | 0.0   | 333.12      |
      | 3  | 31   | 01 September 2024 | 15 June 2024 | 0.0             | 337.23        | 0.0      | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late  | Outstanding |
      | 1000          | 7.58     | 0.0  | 0.0       | 1007.58 | 337.23 | 337.23     | 0.0   | 670.35      |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | RANGE_3        | 01 July 2024       | 337.23            |

    When Admin sets the business date to "01 August 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |       |             |
      | 1  | 30   | 01 July 2024      |              | 667.55          | 332.45        | 4.78     | 0.0  | 0.0       | 337.23 | 0.0    | 0.0        | 0.0   | 337.23      |
      | 2  | 31   | 01 August 2024    |              | 337.23          | 330.32        | 3.87     | 0.0  | 0.0       | 334.19 | 0.0    | 0.0        | 0.0   | 334.19      |
      | 3  | 31   | 01 September 2024 | 15 June 2024 | 0.0             | 337.23        | 0.0      | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late  | Outstanding |
      | 1000          | 8.65     | 0.0  | 0.0       | 1008.65 | 337.23 | 337.23     | 0.0   | 671.42      |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | RANGE_3        | 01 July 2024       | 337.23            |
    When Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule

    When Loan Pay-off is made on "1 August 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3939
  Scenario: Verify nextPaymentAmount value with loan pay-off on first installment - progressive loan, interest recalculation daily - UC10
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 June 2024      | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 2  | 31   | 01 August 2024    |           | 335.27          | 333.33        | 3.9      | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 335.27        | 1.96     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 11.69    | 0.0  | 0.0       | 1011.69 | 0.0  | 0.0        | 0.0  | 1011.69     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 337.23            |

    When Admin sets the business date to "25 June 2024"
    When Admin runs inline COB job for Loan
    When Loan Pay-off is made on "25 June 2024"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 July 2024      | 25 June 2024 | 667.44          | 332.56        | 4.67     | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    | 25 June 2024 | 330.21          | 337.23        | 0.0      | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0  | 0.0         |
      | 3  | 31   | 01 September 2024 | 25 June 2024 | 0.0             | 330.21        | 0.0      | 0.0  | 0.0       | 330.21 | 330.21 | 330.21     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000          | 4.67     | 0.0  | 0.0       | 1004.67 | 1004.67 | 1004.67    | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount  | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 24 June 2024     | Accrual                 | 4.47    | 0.0       | 4.47     | 0.0  | 0.0       | 0.0          |
      | 25 June 2024     | Repayment               | 1004.67 | 1000.0    | 4.67     | 0.0  | 0.0       | 0.0          |
      | 25 June 2024     | Accrual                 | 0.2     | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 25 June 2024       | 0.0               |

    When Admin sets the business date to "01 August 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 July 2024      | 25 June 2024 | 667.44          | 332.56        | 4.67     | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    | 25 June 2024 | 330.21          | 337.23        | 0.0      | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0  | 0.0         |
      | 3  | 31   | 01 September 2024 | 25 June 2024 | 0.0             | 330.21        | 0.0      | 0.0  | 0.0       | 330.21 | 330.21 | 330.21     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000          | 4.67     | 0.0  | 0.0       | 1004.67 | 1004.67 | 1004.67    | 0.0  | 0.0         |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 August 2024     | 0.0               |

  @TestRailId:C3940
  Scenario: Verify nextPaymentAmount value with downpayment and interest refund - progressive loan, interest recalculation daily - UC11
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_DOWNPAYMENT_ACCRUAL_ACTIVITY | 01 June 2024      | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 01 June 2024      | 01 June 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 01 July 2024      |              | 501.45          | 248.55        | 4.37     | 0.0  | 0.0       | 252.92 | 0.0   | 0.0        | 0.0  | 252.92      |
      | 3  | 31   | 01 August 2024    |              | 251.46          | 249.99        | 2.93     | 0.0  | 0.0       | 252.92 | 0.0   | 0.0        | 0.0  | 252.92      |
      | 4  | 31   | 01 September 2024 |              | 0.0             | 251.46        | 1.47     | 0.0  | 0.0       | 252.93 | 0.0   | 0.0        | 0.0  | 252.93      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000          | 8.77     | 0.0  | 0.0       | 1008.77 | 250.0 | 0.0        | 0.0  | 758.77      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 June 2024     | Down Payment            | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 252.92            |

    When Admin sets the business date to "01 August 2024"
    When Admin runs inline COB job for Loan
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "01 August 2024" with 200 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 0    | 01 June 2024      | 01 June 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0  | 0.0        | 0.0    | 0.0         |
      | 2  | 30   | 01 July 2024      |              | 501.45          | 248.55        | 4.37     | 0.0  | 0.0       | 252.92 | 202.32 | 0.0        | 202.32 | 50.6        |
      | 3  | 31   | 01 August 2024    |              | 252.9           | 248.55        | 4.37     | 0.0  | 0.0       | 252.92 | 0.0    | 0.0        | 0.0    | 252.92      |
      | 4  | 31   | 01 September 2024 |              | 0.0             | 252.9         | 1.48     | 0.0  | 0.0       | 254.38 | 0.0    | 0.0        | 0.0    | 254.38      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 1000          | 10.22    | 0.0  | 0.0       | 1010.22 | 452.32 | 0.0        | 202.32 | 557.9       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 June 2024     | Down Payment            | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 July 2024     | Accrual Activity        | 4.37   | 0.0       | 4.37     | 0.0  | 0.0       | 0.0          |
      | 31 July 2024     | Accrual                 | 8.6    | 0.0       | 8.6      | 0.0  | 0.0       | 0.0          |
      | 01 August 2024   | Merchant Issued Refund  | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 550.0        |
      | 01 August 2024   | Interest Refund         | 2.32   | 2.32      | 0.0      | 0.0  | 0.0       | 547.68       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | RANGE_3        | 01 August 2024     | 252.92            |

    When Admin sets the business date to "01 September 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 0    | 01 June 2024      | 01 June 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0  | 0.0        | 0.0    | 0.0         |
      | 2  | 30   | 01 July 2024      |              | 501.45          | 248.55        | 4.37     | 0.0  | 0.0       | 252.92 | 202.32 | 0.0        | 202.32 | 50.6        |
      | 3  | 31   | 01 August 2024    |              | 252.9           | 248.55        | 4.37     | 0.0  | 0.0       | 252.92 | 0.0    | 0.0        | 0.0    | 252.92      |
      | 4  | 31   | 01 September 2024 |              | 0.0             | 252.9         | 3.19     | 0.0  | 0.0       | 256.09 | 0.0    | 0.0        | 0.0    | 256.09      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 1000          | 11.93    | 0.0  | 0.0       | 1011.93 | 452.32 | 0.0        | 202.32 | 559.61      |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | RANGE_30       | 01 August 2024     | 252.92            |

    When Loan Pay-off is made on "1 September 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C4130
  Scenario: Verify that paused days are not counted in installment level delinquency
    When Admin sets the business date to "28 May 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 28 May 2025       | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "28 May 2025" with "1000" amount and expected disbursement date on "28 May 2025"
    And Admin successfully disburse the loan on "28 May 2025" with "1000" EUR transaction amount
#    --- Delinquency pause ---
    And Admin sets the business date to "15 June 2025"
    And Admin runs inline COB job for Loan
    And Admin initiate a DELINQUENCY PAUSE with startDate: "17 June 2025" and endDate: "19 August 2025"
    And Admin sets the business date to "01 July 2025"
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "01 August 2025"
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "01 September 2025"
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "01 October 2025"
    And Admin runs inline COB job for Loan
    And Admin sets the business date to "31 October 2025"
    And Admin runs inline COB job for Loan
    Then Delinquency-actions have the following data:
      | action | startDate    | endDate        |
      | PAUSE  | 17 June 2025 | 19 August 2025 |
    And Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd |
      | false  | 17 June 2025     | 19 August 2025 |
    And Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | delinquentDays | pastDueDays |
      | RANGE_60       | 875.0            | 31 May 2025    | 90             | 156         |
    And Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 1       | RANGE_1  | 125.00 |
      | 3       | RANGE_30 | 125.00 |
      | 4       | RANGE_60 | 375.00 |
      | 5       | RANGE_90 | 250.00 |

  @TestRailId:C4140
  Scenario: Verify that loan delinquent days are correct when graceOnArrearsAgeing is set on loan product level (value=3)
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 January 2025   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    And Admin sets the business date to "15 May 2025"
    And Admin runs inline COB job for Loan
    Then Admin checks that delinquency range is: "RANGE_90" and has delinquentDate "2025-02-04"
    And Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate   | delinquentDays | pastDueDays |
      | RANGE_90       | 666.68           | 04 February 2025 | 100            | 103         |

  @TestRailId:C4141
  Scenario: Verify that loan delinquent days are correct when graceOnArrearsAgeing is overrided on loan level (value=5)
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with graceOnArrearsAgeing and following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | graceOnArrearsAgeing |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 January 2025   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 5                    |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    And Admin sets the business date to "15 May 2025"
    And Admin runs inline COB job for Loan
    Then Admin checks that delinquency range is: "RANGE_90" and has delinquentDate "2025-02-06"
    And Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate   | delinquentDays | pastDueDays |
      | RANGE_90       | 666.68           | 06 February 2025 | 98             | 103         |

  @TestRailId:C4619
  Scenario: Verify that pastDueDate is returned correctly for overdue loan
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | pastDueDate     | delinquentDays | pastDueDays |
      | RANGE_3        | 250.0            | 04 October 2023 | 01 October 2023 | 6              | 9           |

  @TestRailId:C4620
  Scenario: Verify that pastDueDate is null when loan has no overdue
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 250 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | null        | 0              | 0           |

  @TestRailId:C4621
  Scenario: Verify that pastDueDate equals chargeback date when chargeback creates overdue
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "05 October 2023"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount
    When Admin sets the business date to "10 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | pastDueDate     | delinquentDays | pastDueDays |
      | RANGE_1        | 250.0            | 08 October 2023 | 05 October 2023 | 2              | 5           |

  @TestRailId:C4622
  Scenario: Verify that pastDueDate is present in LoanBalanceChangedBusinessEvent for overdue loan
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type   | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 October 2023   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       |  EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "20 October 2023"
    And Create an interest pause period with start date "25 October 2023" and end date "30 October 2023"
    And LoanBalanceChangedBusinessEvent has pastDueDate "2023-10-16"

  @TestRailId:C4623
  Scenario: Verify that pastDueDate is null in LoanBalanceChangedBusinessEvent when loan has no overdue
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type   | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 October 2023   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       |  EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 335.28 EUR transaction amount
    When Admin sets the business date to "20 October 2023"
    And Create an interest pause period with start date "25 October 2023" and end date "30 October 2023"
    And LoanBalanceChangedBusinessEvent has pastDueDate "null"

  @TestRailId:C4624
  Scenario: Verify that pastDueDate is present in LoanBalanceChangedBusinessEvent and equals chargeback date when chargeback creates overdue
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type   | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 October 2023   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       |  EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 335.28 EUR transaction amount
    When Admin sets the business date to "20 October 2023"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 335.28 EUR transaction amount
    When Admin sets the business date to "25 October 2023"
    And Create an interest pause period with start date "25 October 2023" and end date "30 October 2023"
    Then LoanBalanceChangedBusinessEvent has pastDueDate "2023-10-20"

