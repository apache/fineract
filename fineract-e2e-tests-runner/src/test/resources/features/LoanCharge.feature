@ChargeFeature
Feature: LoanCharge


  Scenario: Charge creation functionality with locale EN
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "6000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "10 July 2022"
    Then Charge is successfully added to the loan with 600 EUR


  Scenario: Charge creation functionality with locale DE
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "6000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin adds a 10 % Processing charge to the loan with "de_DE" locale on date: "10 Juli 2022"
    Then Charge is successfully added to the loan with 600 EUR


  Scenario: Due date charge can be successfully applied when it is added on the loan account after the maturity date (NSF scenario of last installment)
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 January 2022", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2022" with "750" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2022"
    And Customer makes "AUTOPAY" repayment on "1 February 2022" with 250 EUR transaction amount
    When Admin sets the business date to "1 March 2022"
    And Customer makes "AUTOPAY" repayment on "1 March 2022" with 250 EUR transaction amount
    When Admin sets the business date to "1 April 2022"
    And Customer makes "AUTOPAY" repayment on "1 April 2022" with 250 EUR transaction amount
    When Customer makes a repayment undo on "1 April 2022"
    When Admin sets the business date to "5 April 2022"
    And Admin adds an NSF fee because of payment bounce with "5 April 2022" transaction date
    And Customer makes "AUTOPAY" repayment on "1 April 2022" with 260 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount


  Scenario: Due date charge can be successfully applied when it is added on the loan account which already has a N+1 scenario (by chargeback)
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 January 2022", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2022" with "750" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2022"
    And Customer makes "AUTOPAY" repayment on "1 February 2022" with 250 EUR transaction amount
    When Admin sets the business date to "1 March 2022"
    And Customer makes "AUTOPAY" repayment on "1 March 2022" with 250 EUR transaction amount
    When Admin sets the business date to "1 April 2022"
    And Customer makes "AUTOPAY" repayment on "1 April 2022" with 250 EUR transaction amount
    When Admin sets the business date to "1 May 2022"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "1 May 2022" with 250 EUR transaction amount
    When Customer makes a repayment undo on "1 May 2022"
    When Admin sets the business date to "5 May 2022"
    And Admin adds an NSF fee because of payment bounce with "5 May 2022" transaction date
    When Admin sets the business date to "10 May 2022"
    And Customer makes "AUTOPAY" repayment on "10 May 2022" with 260 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount


  Scenario: Due date charge can be successfully applied, then waived when it is added on the loan account after the maturity date (NSF scenario of last installment)
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 January 2022", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2022" with "750" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2022"
    And Customer makes "AUTOPAY" repayment on "1 February 2022" with 250 EUR transaction amount
    When Admin sets the business date to "1 March 2022"
    And Customer makes "AUTOPAY" repayment on "1 March 2022" with 250 EUR transaction amount
    When Admin sets the business date to "1 April 2022"
    And Customer makes "AUTOPAY" repayment on "1 April 2022" with 250 EUR transaction amount
    When Customer makes a repayment undo on "1 April 2022"
    Then Loan status will be "ACTIVE"
    Then Loan has 250 outstanding amount
    When Admin sets the business date to "5 April 2022"
    And Admin adds an NSF fee because of payment bounce with "5 April 2022" transaction date
    Then Loan status will be "ACTIVE"
    Then Loan has 260 outstanding amount
    When Admin sets the business date to "7 April 2022"
    And Admin waives charge
    Then Loan status will be "ACTIVE"
    Then Loan has 250 outstanding amount



  Scenario: Due date charge can be successfully applied, waived, then waive reversed when it is added on the loan account after the maturity date (NSF scenario of last installment)
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 January 2022", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2022" with "750" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2022"
    And Customer makes "AUTOPAY" repayment on "1 February 2022" with 250 EUR transaction amount
    When Admin sets the business date to "1 March 2022"
    And Customer makes "AUTOPAY" repayment on "1 March 2022" with 250 EUR transaction amount
    When Admin sets the business date to "1 April 2022"
    And Customer makes "AUTOPAY" repayment on "1 April 2022" with 250 EUR transaction amount
    When Customer makes a repayment undo on "1 April 2022"
    When Admin sets the business date to "5 April 2022"
    And Admin adds an NSF fee because of payment bounce with "5 April 2022" transaction date
    When Admin sets the business date to "7 April 2022"
    And Admin waives charge
    When Admin sets the business date to "8 April 2022"
    And Admin makes waive undone for charge
    Then Loan status will be "ACTIVE"
    Then Loan has 260 outstanding amount

#    TODO clear, make it work properly
  @Skip
  Scenario: Charge adjustment works properly
    When Admin sets the business date to "22 October 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "22 October 2022", with Principal: "1000", a loanTermFrequency: 2 months, and numberOfRepayments: 2
    And Admin successfully approves the loan on "22 October 2022" with "1000" amount and expected disbursement date on "22 October 2022"
    When Admin successfully disburse the loan on "22 October 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "23 October 2022"
    And Admin adds an NSF fee because of payment bounce with "23 October 2022" transaction date
    Then Loan has 1010 outstanding amount
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 10        | 1010 | 0    | 0          | 0    | 1010        |
##  check transactions last transaction ID type:accrual amount:10, fee:10 depends on NSF penalty or fee / nekunk
#    TODO Accrual job 500 Error
#    And Admin runs the Add Periodic Accrual Transactions job
#    Then Loan Transactions tab has a transaction with date: "04 November 2022", and with the following data:
#      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
#      | Repayment        | 10.0  | 0.0     | 0.0      | 0.0  | 10.0       | 600.0        |


##  chek Journal entries for transaction ID --> type/account name/debit-credit amount
    #    TODO can be done when journal entry paging is fixed
#    Then The transaction type "Accrual" with date "23 October 2022" has the following journal entries data: Type="ASSET", Account name="Interest/Fee Receivable", Type="INCOME", Account name="Fee Income", Type="", Account name=""

##  check charges --> due/paid/waived/outstanding
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 23 October 2022 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |

    When Admin sets the business date to "04 November 2022"

##    charge adjustment for nsf fee with 3
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "23 October 2022" with 3 EUR transaction amount and externalId ""
    Then Loan has 1007 outstanding amount
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 10        | 1010 | 3    | 3          | 0    | 1007        |
    Then Loan Transactions tab has a transaction with date: "23 October 2022", and with the following data:
      | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Charge Adjustment | 3.0    | 0.0       | 0.0      | 0.0  | 3.0       | 1000.0       |

##  chek Journal entries for transaction ID --> type/account name/debit-credit amount
#    TODO can be done when journal entry paging is fixed
#    Then The transaction type "Charge Adjustment" with date "04 November 2022" has the following journal entries data: Type="ASSET", Account name="Interest/Fee Receivable", Type="INCOME", Account name="Fee Income", Type="", Account name=""
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 23 October 2022 | Flat             | 10.0 | 3.0  | 0.0    | 7.0         |
#
    And Customer makes "AUTOPAY" repayment on "25 October 2022" with 8 EUR transaction amount
    Then Loan has 999 outstanding amount
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 10        | 1010 | 11   | 11         | 0    | 999         |
    Then Loan Transactions tab has a transaction with date: "25 October 2022", and with the following data:
      | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Charge Adjustment | 3.0    | 1.0       | 0.0      | 0.0  | 2.0       | 999.0        |

#    TODO can be done when journal entry paging is fixed
#    Then The transaction type "Charge Adjustment" with date "04 November 2022" has the following journal entries data: Type="ASSET", Account name="Interest/Fee Receivable", Type="INCOME", Account name="Fee Income", Type="ASSET", Account name="Loans Receivable"
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 23 October 2022 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
#
##  charge adjustment with 8 will fail
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "23 October 2022" with transaction amount higher than the available charge amount

##  revert last charge adjustment (was amount 3)
    When Admin reverts the charge adjustment which was raised on "04 November 2022" with 3 EUR transaction amount
    Then Loan has 1002 outstanding amount
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 10        | 1010 | 8    | 8          | 0    | 1002        |
    Then In Loan Transactions the latest Transaction has Transaction type="Charge Adjustment" and is reverted
#    TODO can be done when journal entry paging is fixed
#    Then The transaction type "Charge Adjustment" with date "04 November 2022" has the following journal entries data: Type="ASSET", Account name="Interest/Fee Receivable", Type="INCOME", Account name="Fee Income", Type="ASSET", Account name="Loans Receivable"
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 23 October 2022 | Flat             | 10.0 | 8.0  | 0.0    | 2.0         |
#
##  Add snooze fee on 10/27/2022 with amount 9 (az eloyo legzen penaltz ez meg fee(
    And Admin adds "LOAN_SNOOZE_FEE" due date charge with "27 October 2022" due date and 9 EUR transaction amount
    Then Loan has 1011 outstanding amount
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 9    | 10        | 1019 | 8    | 8          | 0    | 1011        |
    Then In Loan Transactions the latest Transaction has Transaction type="Charge Adjustment" and is reverted
#    TODO can be done when journal entry paging is fixed
#    Then The transaction type "Charge Adjustment" with date "04 November 2022" has the following journal entries data: Type="ASSET", Account name="Interest/Fee Receivable", Type="INCOME", Account name="Fee Income", Type="ASSET", Account name="Loans Receivable"
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 27 October 2022 | Flat             | 9.0 | 0.0  | 0.0    | 9.0         |
#    Then In Loan Charges the given Charge has the following data:
#      | nth Charge | Name       | isPenalty | Due as of       | Due | Paid | Waived | Outstanding |
#      |            | Snooze fee | false     | 27 October 2022 | 9   | 0    | 0      | 9           |
#
##    charge adjustment for snooze fee with 4
    When Admin makes a charge adjustment for the last "LOAN_SNOOZE_FEE" type charge which is due on "27 October 2022" with 4 EUR transaction amount and externalId ""
    Then Loan has 1007 outstanding amount
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 9    | 10        | 1019 | 12   | 12         | 0    | 1007        |
    Then Loan Transactions tab has a transaction with date: "04 November 2022", and with the following data:
      | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Charge Adjustment | 4.0    | 0.0       | 0.0      | 2.0  | 2.0       | 1000.0       |

#    TODO can be done when journal entry paging is fixed
#    Then The transaction type "Charge Adjustment" with date "04 November 2022" has the following journal entries data: Type="ASSET", Account name="Interest/Fee Receivable", Type="INCOME", Account name="Fee Income", Type="", Account name=""
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 27 October 2022 | Flat             | 9.0 | 2.0  | 0.0    | 7.0         |
#   Then In Loan Charges the given Charge has the following data:
#      | nth Charge | Name       | isPenalty | Due as of       | Due | Paid | Waived | Outstanding |
#      |            | Snooze fee | false     | 27 October 2022 | 9   | 2    | 0      | 7           |
#
    And Customer makes "AUTOPAY" repayment on "31 October 2022" with 507 EUR transaction amount
    Then Loan has 500 outstanding amount
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 9    | 10        | 1019 | 519  | 519        | 0    | 500         |
    Then Loan Transactions tab has a transaction with date: "04 November 2022", and with the following data:
      | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Charge Adjustment | 4.0    | 4.0       | 0.0      | 0.0  | 0.0       | 500.0        |

#    TODO can be done when journal entry paging is fixed
#    Then The transaction type "Charge Adjustment" with date "04 November 2022" has the following journal entries data: Type="ASSET", Account name="Loans Receivable", Type="INCOME", Account name="Fee Income", Type="", Account name=""
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 27 October 2022 | Flat             | 9.0 | 9.0  | 0.0    | 0.0         |
#    Then In Loan Charges the given Charge has the following data:
#      | nth Charge | Name       | isPenalty | Due as of       | Due | Paid | Waived | Outstanding |
#      |            | Snooze fee | false     | 27 October 2022 | 9   | 9    | 0      | 0           |
#
##      charge adjustment for nsf fee with 5 / hol jegyzi meg az ID-t?
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "23 October 2022" with 5 EUR transaction amount and externalId ""
    Then Loan has 495 outstanding amount
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 9    | 10        | 1019 | 524  | 524        | 0    | 495         |
    Then Loan Transactions tab has a transaction with date: "04 November 2022", and with the following data:
      | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Charge Adjustment | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 495.0        |

#    TODO can be done when journal entry paging is fixed
#    Then The transaction type "Charge Adjustment" with date "04 November 2022" has the following journal entries data: Type="ASSET", Account name="Loans Receivable", Type="INCOME", Account name="Fee Income", Type="", Account name=""
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 27 October 2022 | Flat             | 9.0 | 9.0  | 0.0    | 0.0         |
#    Then In Loan Charges the given Charge has the following data:
#      | nth Charge | Name       | isPenalty | Due as of       | Due | Paid | Waived | Outstanding |
#      |            | Snooze fee | false     | 27 October 2022 | 9   | 9    | 0      | 0           |

    And Customer makes "AUTOPAY" repayment on "1 November 2022" with 494 EUR transaction amount
    Then Loan has 1 outstanding amount
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 9    | 10        | 1019 | 1018 | 1018       | 0    | 1           |
    Then Loan Transactions tab has a transaction with date: "04 November 2022", and with the following data:
      | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Charge Adjustment | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 1.0          |

#    TODO can be done when journal entry paging is fixed
#    Then The transaction type "Charge Adjustment" with date "04 November 2022" has the following journal entries data: Type="ASSET", Account name="Loans Receivable", Type="INCOME", Account name="Fee Income", Type="", Account name=""
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 27 October 2022 | Flat             | 9.0 | 9.0  | 0.0    | 0.0         |
#    Then In Loan Charges the given Charge has the following data:
#      | nth Charge | Name       | isPenalty | Due as of       | Due | Paid | Waived | Outstanding |
#      |            | Snooze fee | false     | 27 October 2022 | 9   | 9    | 0      | 0           |
#
#  #    charge adjustment for snooze fee with 1
    When Admin makes a charge adjustment for the last "LOAN_SNOOZE_FEE" type charge which is due on "27 October 2022" with 1 EUR transaction amount and externalId ""
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 9    | 10        | 1019 | 1019 | 1019       | 0    | 0           |
    Then Loan Transactions tab has a transaction with date: "04 November 2022", and with the following data:
      | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Charge Adjustment | 1.0    | 1.0       | 0.0      | 0.0  | 0.0       | 0.0          |

#    TODO can be done when journal entry paging is fixed
#    Then The transaction type "Charge Adjustment" with date "04 November 2022" has the following journal entries data: Type="ASSET", Account name="Loans Receivable", Type="INCOME", Account name="Fee Income", Type="", Account name=""
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 27 October 2022 | Flat             | 9.0 | 9.0  | 0.0    | 0.0         |
#    Then In Loan Charges the given Charge has the following data:
#      | nth Charge | Name       | isPenalty | Due as of       | Due | Paid | Waived | Outstanding |
#      |            | Snooze fee | false     | 27 October 2022 | 9   | 9    | 0      | 0           |
#
#  #  revert last charge adjustment (was amount 1)
    When Admin reverts the charge adjustment which was raised on "04 November 2022" with 1 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 1 outstanding amount
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 9    | 10        | 1019 | 1018 | 1018       | 0    | 1           |
#    TODO do it with nth transaction and not latest, because accrual is working now, so the last transaction will be accrual and the one before will be the reverted Charge Adjustment
#    Then In Loan Transactions the latest Transaction has Transaction type="Charge Adjustment" and is reverted
#    TODO can be done when journal entry paging is fixed
#    Then The transaction type "Charge Adjustment" with date "04 November 2022" has the following journal entries data: Type="ASSET", Account name="Loans Receivable", Type="INCOME", Account name="Fee Income", Type="", Account name=""
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 27 October 2022 | Flat             | 9.0 | 9.0  | 0.0    | 0.0         |
#    Then In Loan Charges the given Charge has the following data:
#      | nth Charge | Name       | isPenalty | Due as of       | Due | Paid | Waived | Outstanding |
#      |            | Snooze fee | false     | 27 October 2022 | 9   | 9    | 0      | 0           |
#
#    #    charge adjustment for nsf fee with 1
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "23 October 2022" with 1 EUR transaction amount and externalId ""
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 9    | 10        | 1019 | 1019 | 1019       | 0    | 0           |
    Then Loan Transactions tab has a transaction with date: "04 November 2022", and with the following data:
      | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Charge Adjustment | 1.0    | 1.0       | 0.0      | 0.0  | 0.0       | 0.0          |

#    TODO can be done when journal entry paging is fixed
#    Then The transaction type "Charge Adjustment" with date "04 November 2022" has the following journal entries data: Type="ASSET", Account name="Loans Receivable", Type="INCOME", Account name="Fee Income", Type="", Account name=""
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 27 October 2022 | Flat             | 9.0 | 9.0  | 0.0    | 0.0         |
#    Then In Loan Charges the given Charge has the following data:
#      | nth Charge | Name       | isPenalty | Due as of       | Due | Paid | Waived | Outstanding |
#      |            | Snooze fee | false     | 27 October 2022 | 9   | 9    | 0      | 0           |
#
#   #    charge adjustment for nsf fee with 2
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "23 October 2022" with 2 EUR transaction amount and externalId ""
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 9    | 10        | 1019 | 1019 | 1019       | 0    | 0           |
    Then Loan Transactions tab has a transaction with date: "04 November 2022", and with the following data:
      | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Charge Adjustment | 2.0    | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan has 2 overpaid amount

#    TODO can be done when journal entry paging is fixed
#    Then The transaction type "Charge Adjustment" with date "04 November 2022" has the following journal entries data: Type="LIABILITY", Account name="Overpayment account", Type="INCOME", Account name="Fee Income", Type="", Account name=""
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 27 October 2022 | Flat             | 9.0 | 9.0  | 0.0    | 0.0         |
#    Then In Loan Charges the given Charge has the following data:
#      | nth Charge | Name       | isPenalty | Due as of       | Due | Paid | Waived | Outstanding |
#      |            | Snooze fee | false     | 27 October 2022 | 9   | 9    | 0      | 0           |
#
#  #  revert last charge adjustment (was amount 2)
    When Admin reverts the charge adjustment which was raised on "04 November 2022" with 2 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 9    | 10        | 1019 | 1019 | 1019       | 0    | 0           |
    Then In Loan Transactions the latest Transaction has Transaction type="Charge Adjustment" and is reverted
#    TODO can be done when journal entry paging is fixed
#    Then The transaction type "Charge Adjustment" with date "04 November 2022" has the following journal entries data: Type="LIABILITY", Account name="Overpayment account", Type="INCOME", Account name="Fee Income", Type="", Account name=""
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 27 October 2022 | Flat             | 9.0 | 9.0  | 0.0    | 0.0         |
#    Then In Loan Charges the given Charge has the following data:
#      | nth Charge | Name       | isPenalty | Due as of       | Due | Paid | Waived | Outstanding |
#      |            | Snooze fee | false     | 27 October 2022 | 9   | 9    | 0      | 0           |


  Scenario: Verify that charge can be added to loan on disbursement date (loan status is 'active')
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 January 2023" due date and 10 EUR transaction amount
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 January 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan's actualMaturityDate is "31 January 2023"


  Scenario: Verify that charge can be added to loan after disbursement date (loan status is 'active')
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "10 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 January 2023" due date and 10 EUR transaction amount
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 January 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan's actualMaturityDate is "31 January 2023"


  Scenario: Verify that charge can be added to loan after partial repayment (loan status is 'active')
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 500 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 January 2023" due date and 10 EUR transaction amount
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 January 2023 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan's actualMaturityDate is "31 January 2023"


  Scenario: Verify that charge can be added to loan which is reopened by chargeback transaction after got overpaid by repayment  (loan status is 'active')
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 1200 EUR transaction amount
    Then Loan status will be "OVERPAID"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 300 EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 January 2023" due date and 10 EUR transaction amount
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 January 2023 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan's actualMaturityDate is "31 January 2023"


  Scenario: Verify that charge can be added to loan which is reopened by payment undo transaction after got overpaid by repayment  (loan status is 'active')
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "05 January 2023"
    And Customer makes "AUTOPAY" repayment on "05 January 2023" with 700 EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 500 EUR transaction amount
    Then Loan status will be "OVERPAID"
    When Customer undo "1"th transaction made on "05 January 2023"
    Then Loan status will be "ACTIVE"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 January 2023" due date and 10 EUR transaction amount
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 January 2023 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan's actualMaturityDate is "31 January 2023"


  Scenario: Verify that charge can be added to loan which is reopened by undo goodwill credit transaction after got overpaid by goodwill credit transaction  (loan status is 'active')
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 900 EUR transaction amount
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "10 January 2023" with 300 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    When Customer undo "2"th transaction made on "10 January 2023"
    Then Loan status will be "ACTIVE"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 January 2023" due date and 10 EUR transaction amount
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 January 2023 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan's actualMaturityDate is "31 January 2023"


  Scenario: Verify that charge can be added to loan which is reopened by undo repayment after got overpaid by goodwill credit transaction  (loan status is 'active')
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 900 EUR transaction amount
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "10 January 2023" with 300 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    When Customer undo "1"th transaction made on "10 January 2023"
    Then Loan status will be "ACTIVE"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 January 2023" due date and 10 EUR transaction amount
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 January 2023 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan's actualMaturityDate is "31 January 2023"


  Scenario: Verify that loanChargePaidByList section has the correct data in loanDetails and in LoanTransactionMakeRepaymentPostBusinessEvent
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "03 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "03 January 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "04 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "04 January 2023" due date and 20 EUR transaction amount
    When Admin sets the business date to "05 January 2023"
    And Customer makes "AUTOPAY" repayment on "05 January 2023" with 200 EUR transaction amount
    Then Loan details and LoanTransactionMakeRepaymentPostBusinessEvent has the following data in loanChargePaidByList section:
      | amount | name       |
      | 10.0   | Snooze fee |
      | 20.0   | NSF fee    |


  Scenario: Verify that after COB job Accrual entry is made when loan has a fee-charge on disbursal date
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 January 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "02 January 2023"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has a transaction with date: "01 January 2023", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |


  Scenario: Verify that after COB job Accrual entry is made when loan has a penalty-charge on disbursal date
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 January 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "02 January 2023"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has a transaction with date: "01 January 2023", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Accrual          | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          |


  Scenario: Verify that charge can be added to loan which is paid off and overpaid by refund
    When Admin sets the business date to "10 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "10 January 2023"
    And Admin successfully approves the loan on "10 January 2023" with "1000" amount and expected disbursement date on "10 January 2023"
    When Admin successfully disburse the loan on "10 January 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "10 January 2023" with 50 EUR transaction amount
    Then Loan status will be "OVERPAID"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 January 2023" due date and 10 EUR transaction amount
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 January 2023 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |


  Scenario: FEE01 - Verify the loan creation with charge: disbursement percentage fee
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    When Admin adds "LOAN_DISBURSEMENT_PERCENTAGE_FEE" charge with 1.5 % of transaction amount
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Repayment (at time of disbursement) | 15.0   | 0.0       | 0.0      | 15.0 | 0.0       | 1000.0       |
      | 01 January 2023  | Disbursement                        | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2023  | Accrual                             | 15.0   | 0.0       | 0.0      | 15.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name                        | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement percentage fee | false     | Disbursement   |           | % Amount         | 15.0 | 15.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 15.0 |           | 15.0   | 15.0 |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 15   | 0         | 1015 | 15   | 0          | 0    | 1000        |



  Scenario: FEE02 - Verify the loan creation with charge: flat fee
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "10 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 January 2023" due date and 15 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 January 2023 | Flat             | 15.0 | 0.0  | 0.0    | 15.0        |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 15.0 | 0.0       | 1015.0 | 0.0  | 0.0        | 0.0  | 1015.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 15   | 0         | 1015 | 0    | 0          | 0    | 1015        |


  Scenario: FEE03 - Verify the loan creation with charge: installment percentage fee
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 1 January 2023    | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "10 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_PERCENTAGE_FEE" charge with 1.5 % of transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
    Then Loan Charges tab has the following data:
      | Name                       | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment percentage fee | false     | Installment Fee |           | % Loan Amount + Interest | 46.35 | 0.0  | 0.0    | 46.35       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0   |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 30.0     | 15.45 | 0.0       | 1045.45 | 0.0  | 0.0        | 0.0  | 1045.45     |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 30.0     | 15.45 | 0.0       | 1045.45 | 0.0  | 0.0        | 0.0  | 1045.45     |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 30.0     | 15.45 | 0.0       | 1045.45 | 0.0  | 0.0        | 0.0  | 1045.45     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 46.35 | 0         | 3136.35 | 0    | 0          | 0    | 3136.35     |


  Scenario: FEE04 - Verify the loan creation with charge: overdue fee on principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT_OVERDUE_FROM_AMOUNT | 1 January 2023    | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "10 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "01 May 2023"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 01 February 2023 | Accrual          | 45.0   | 0.0       | 30.0     | 0.0  | 15.0      | 0.0          |
      | 01 March 2023    | Accrual          | 45.0   | 0.0       | 30.0     | 0.0  | 15.0      | 0.0          |
      | 01 April 2023    | Accrual          | 45.0   | 0.0       | 30.0     | 0.0  | 15.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | % Late fee | true      | Overdue Fees   | 01 February 2023 | % Amount         | 15.0 | 0.0  | 0.0    | 15.0        |
      | % Late fee | true      | Overdue Fees   | 01 March 2023    | % Amount         | 15.0 | 0.0  | 0.0    | 15.0        |
      | % Late fee | true      | Overdue Fees   | 01 April 2023    | % Amount         | 15.0 | 0.0  | 0.0    | 15.0        |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 30.0     | 0.0  | 15.0      | 1045.0 | 0.0  | 0.0        | 0.0  | 1045.0      |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 30.0     | 0.0  | 15.0      | 1045.0 | 0.0  | 0.0        | 0.0  | 1045.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 30.0     | 0.0  | 15.0      | 1045.0 | 0.0  | 0.0        | 0.0  | 1045.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 0.0  | 45        | 3135.0 | 0    | 0          | 0    | 3135.0      |


  Scenario: FEE05 - Verify the loan creation with charge: overdue fee on principal+interest
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT_OVERDUE_FROM_AMOUNT_INTEREST | 1 January 2023    | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "10 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "01 May 2023"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 01 February 2023 | Accrual          | 45.45  | 0.0       | 30.0     | 0.0  | 15.45     | 0.0          |
      | 01 March 2023    | Accrual          | 45.45  | 0.0       | 30.0     | 0.0  | 15.45     | 0.0          |
      | 01 April 2023    | Accrual          | 45.45  | 0.0       | 30.0     | 0.0  | 15.45     | 0.0          |
    Then Loan Charges tab has the following data:
      | Name                       | isPenalty | Payment due at | Due as of        | Calculation type         | Due   | Paid | Waived | Outstanding |
      | % Late fee amount+interest | true      | Overdue Fees   | 01 February 2023 | % Loan Amount + Interest | 15.45 | 0.0  | 0.0    | 15.45       |
      | % Late fee amount+interest | true      | Overdue Fees   | 01 March 2023    | % Loan Amount + Interest | 15.45 | 0.0  | 0.0    | 15.45       |
      | % Late fee amount+interest | true      | Overdue Fees   | 01 April 2023    | % Loan Amount + Interest | 15.45 | 0.0  | 0.0    | 15.45       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 30.0     | 0.0  | 15.45     | 1045.45 | 0.0  | 0.0        | 0.0  | 1045.45     |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 30.0     | 0.0  | 15.45     | 1045.45 | 0.0  | 0.0        | 0.0  | 1045.45     |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 30.0     | 0.0  | 15.45     | 1045.45 | 0.0  | 0.0        | 0.0  | 1045.45     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 0.0  | 46.35     | 3136.35 | 0    | 0          | 0    | 3136.35     |

# TODO Create loan product suitable for LOAN_TRANCHE_DISBURSEMENT_PERCENTAGE_FEE, replace, fix expected results
  @Skip
  Scenario: FEE06 - Verify the loan creation with charge: tranche disbursement percentage fee, multi disbursement
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_SAR_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE_MULTI_DISBURSEMENT | 1 January 2023    | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin adds "LOAN_TRANCHE_DISBURSEMENT_PERCENTAGE_FEE" charge with 1.5 % of transaction amount
    When Admin sets the business date to "01 February 2023"
    When Admin successfully disburse the loan on "01 February 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "01 March 2023"
    When Admin successfully disburse the loan on "01 March 2023" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Repayment (at time of disbursement) | 15.0   | 0.0       | 0.0      | 15.0 | 0.0       | 0.0          |
      | 01 January 2023  | Disbursement                        | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan Charges tab has the following data:
      | Name                        | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement percentage fee | false     | Disbursement   |           | % Amount         | 15.0 | 15.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 15.0 |           | 15.0   | 15.0 |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 15   | 0         | 1015 | 15   | 0          | 0    | 1000        |


  Scenario: Verify that partially waived installment fee applied correctly in reverse-replay logic
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin adds "LOAN_INSTALLMENT_PERCENTAGE_FEE" charge with 10 % of transaction amount
    When Admin sets the business date to "15 January 2023"
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 5 EUR transaction amount
    When Admin sets the business date to "20 January 2023"
    And Admin waives due date charge
    And Customer makes "AUTOPAY" repayment on "18 January 2023" with 15 EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0   |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 100.0 | 0.0       | 1100.0 | 20.0 | 20.0       | 0.0  | 985.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 100.0 | 0         | 1100.0 | 20   | 20         | 0    | 985.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement       | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 January 2023  | Repayment          | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 1000.0       |
      | 18 January 2023  | Repayment          | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 985.0        |
      | 20 January 2023  | Waive loan charges | 95.0   | 0.0       | 0.0      | 0.0  | 0.0       | 985.0        |
    Then Loan Charges tab has the following data:
      | Name                       | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment percentage fee | false     | Installment Fee |           | % Loan Amount + Interest | 100.0 | 5.0  | 95.0   | 0.0         |


  Scenario: Verify that adding charge on a closed loan after maturity date is creating an N+1 installment - LP1 product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 October 2023"
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "31 October 2023"
    And Customer makes "AUTOPAY" repayment on "31 October 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "01 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 31 October 2023  | 31 October 2023 | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 1    | 01 November 2023 |                 | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0   | 0.0    | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20.0 | 0         | 1020.0 | 1000 | 0          | 0    | 20.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 31 October 2023  | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |


  Scenario: Verify that adding charge on a closed loan after maturity date is creating an N+1 installment - LP2 auto payment enabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "31 October 2023"
    And Customer makes "AUTOPAY" repayment on "31 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "15 November 2023"
    And Customer makes "AUTOPAY" repayment on "15 November 2023" with 250 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "16 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 16 October 2023  | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 31 October 2023  | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 | 15 November 2023 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 5  | 1    | 16 November 2023 |                  | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0  | 0.0   | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0        | 20   | 0         | 1020.0 | 1000.0 | 0          | 0    | 20          |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 31 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 15 November 2023 | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |


  Scenario: Verify that adding charge on a closed loan after maturity date is creating an N+1 installment - LP2 auto payment disabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "31 October 2023"
    And Customer makes "AUTOPAY" repayment on "31 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "15 November 2023"
    And Customer makes "AUTOPAY" repayment on "15 November 2023" with 250 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "16 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 16 October 2023  | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 31 October 2023  | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 | 15 November 2023 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 5  | 1    | 16 November 2023 |                  | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0  | 0.0   | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0        | 20   | 0         | 1020.0 | 1000.0 | 0          | 0    | 20          |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 31 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 15 November 2023 | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

   @AdvancedPaymentAllocation
  Scenario: Verify that adding charge on a closed loan after maturity date is creating an N+1 installment - LP2 advanced payment allocation product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "31 October 2023"
    And Customer makes "AUTOPAY" repayment on "31 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "15 November 2023"
    And Customer makes "AUTOPAY" repayment on "15 November 2023" with 250 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "16 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 16 October 2023  | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 31 October 2023  | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 | 15 November 2023 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 5  | 1    | 16 November 2023 |                  | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0  | 0.0   | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0        | 20   | 0         | 1020.0 | 1000.0 | 0          | 0    | 20          |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 31 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 15 November 2023 | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |


  Scenario: Verify that adding charge on a active loan / partial repayment after maturity date is creating an N+1 installment - LP1 product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 October 2023"
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "31 October 2023"
    And Customer makes "AUTOPAY" repayment on "31 October 2023" with 800 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 200 outstanding amount
    When Admin sets the business date to "01 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 30   | 31 October 2023  |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 800.0 | 0.0        | 0.0  | 200.0       |
      | 2  | 1    | 01 November 2023 |           | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0   | 0.0   | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20.0 | 0         | 1020.0 | 800  | 0          | 0    | 220.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 31 October 2023  | Repayment        | 800.0  | 800.0     | 0.0      | 0.0  | 0.0       | 200.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |


  Scenario: Verify that adding charge on a active loan / partial repayment after maturity date is creating an N+1 installment - LP2 auto payment enabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "31 October 2023"
    And Customer makes "AUTOPAY" repayment on "31 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "15 November 2023"
    And Customer makes "AUTOPAY" repayment on "15 November 2023" with 100 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 150 outstanding amount
    When Admin sets the business date to "16 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 16 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 31 October 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 100.0 | 0.0        | 0.0  | 150.0       |
      | 5  | 1    | 16 November 2023 |                 | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0  | 0.0   | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 20   | 0         | 1020.0 | 850.0 | 0          | 0    | 170         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 31 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 15 November 2023 | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 150.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |


  Scenario: Verify that adding charge on a active loan / partial repayment after maturity date is creating an N+1 installment - LP2 auto payment disabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "31 October 2023"
    And Customer makes "AUTOPAY" repayment on "31 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "15 November 2023"
    And Customer makes "AUTOPAY" repayment on "15 November 2023" with 100 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 150 outstanding amount
    When Admin sets the business date to "16 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 16 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 31 October 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 100.0 | 0.0        | 0.0  | 150.0       |
      | 5  | 1    | 16 November 2023 |                 | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0  | 0.0   | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 20   | 0         | 1020.0 | 850.0 | 0          | 0    | 170         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 31 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 15 November 2023 | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 150.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

   @AdvancedPaymentAllocation
  Scenario: Verify that adding charge on an active loan / partial repayment after maturity date is creating an N+1 installment - LP2 advanced payment allocation product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "31 October 2023"
    And Customer makes "AUTOPAY" repayment on "31 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "15 November 2023"
    And Customer makes "AUTOPAY" repayment on "15 November 2023" with 100 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 150 outstanding amount
    When Admin sets the business date to "16 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 16 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 31 October 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 100.0 | 0.0        | 0.0  | 150.0       |
      | 5  | 1    | 16 November 2023 |                 | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0  | 0.0   | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 20   | 0         | 1020.0 | 850.0 | 0          | 0    | 170         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 31 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 15 November 2023 | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 150.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |


  Scenario: Verify that adding charge on a active loan / no repayment made, after maturity date is creating an N+1 installment - LP1 product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 October 2023"
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "01 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 October 2023  |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 2  | 1    | 01 November 2023 |           | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0   | 0.0  | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20.0 | 0         | 1020.0 | 0    | 0          | 0    | 1020.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |


  Scenario: Verify that adding charge on a active loan / no repayment made, after maturity date is creating an N+1 installment - LP2 auto payment enabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 750 outstanding amount
    When Admin sets the business date to "16 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 5  | 1    | 16 November 2023 |                 | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0  | 0.0   | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 20   | 0         | 1020.0 | 250.0 | 0          | 0    | 770         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |


  Scenario: Verify that adding charge on a active loan / no repayment made, after maturity date is creating an N+1 installment - LP2 auto payment disabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "16 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 October 2023  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 15   | 16 October 2023  |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 October 2023  |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 November 2023 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 5  | 1    | 16 November 2023 |           | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0  | 0.0  | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 20   | 0         | 1020.0 | 0.0  | 0          | 0    | 1020        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

   @AdvancedPaymentAllocation
  Scenario: Verify that adding charge on an active loan / no repayment made, after maturity date is creating an N+1 installment - LP2 advanced payment allocation product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 750 outstanding amount
    When Admin sets the business date to "16 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 5  | 1    | 16 November 2023 |                 | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0  | 0.0   | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 20   | 0         | 1020.0 | 250.0 | 0          | 0    | 770         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

   @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC1
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 October 2023"
    And Customer makes "AUTOPAY" repayment on "10 October 2023" with 300 EUR transaction amount
    When Admin sets the business date to "20 October 2023"
    And Customer makes "AUTOPAY" repayment on "20 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "10 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "09 October 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 10 October 2023 | 500.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 270.0 | 270.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 20 October 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 30.0  | 30.0       | 0.0  | 220.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 800.0 | 550.0      | 0.0  | 220.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 10 October 2023  | Repayment        | 300.0  | 280.0     | 0.0      | 20.0 | 0.0       | 470.0        |
      | 20 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 220.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 09 October 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |

   @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC2
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 October 2023"
    And Customer makes "AUTOPAY" repayment on "10 October 2023" with 300 EUR transaction amount
    When Admin sets the business date to "20 October 2023"
    And Customer makes "AUTOPAY" repayment on "20 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "10 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 October 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 20 October 2023 | 500.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 270.0 | 250.0      | 20.0 | 0.0         |
      | 3  | 15   | 31 October 2023  | 20 October 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 30.0  | 30.0       | 0.0  | 220.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 800.0 | 530.0      | 20.0 | 220.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 10 October 2023  | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 450.0        |
      | 20 October 2023  | Repayment        | 250.0  | 230.0     | 0.0      | 20.0 | 0.0       | 220.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 October 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |

   @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC3
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 October 2023"
    And Customer makes "AUTOPAY" repayment on "10 October 2023" with 300 EUR transaction amount
    When Admin sets the business date to "20 October 2023"
    And Customer makes "AUTOPAY" repayment on "20 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "10 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "11 October 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 20 October 2023 | 500.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 270.0 | 250.0      | 20.0 | 0.0         |
      | 3  | 15   | 31 October 2023  | 20 October 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 30.0  | 30.0       | 0.0  | 220.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 800.0 | 530.0      | 20.0 | 220.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 10 October 2023  | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 450.0        |
      | 20 October 2023  | Repayment        | 250.0  | 230.0     | 0.0      | 20.0 | 0.0       | 220.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 11 October 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |

   @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC4
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 October 2023"
    And Customer makes "AUTOPAY" repayment on "10 October 2023" with 300 EUR transaction amount
    When Admin sets the business date to "20 October 2023"
    And Customer makes "AUTOPAY" repayment on "20 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "10 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 October 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 20 October 2023 | 500.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 270.0 | 250.0      | 20.0 | 0.0         |
      | 3  | 15   | 31 October 2023  | 20 October 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 30.0  | 30.0       | 0.0  | 220.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 800.0 | 530.0      | 20.0 | 220.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 10 October 2023  | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 450.0        |
      | 20 October 2023  | Repayment        | 250.0  | 230.0     | 0.0      | 20.0 | 0.0       | 220.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 October 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |

   @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC5
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 October 2023"
    And Customer makes "AUTOPAY" repayment on "10 October 2023" with 300 EUR transaction amount
    When Admin sets the business date to "20 October 2023"
    And Customer makes "AUTOPAY" repayment on "20 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "10 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "17 October 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 10 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 20 October 2023 | 250.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 270.0 | 270.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 30.0  | 30.0       | 0.0  | 220.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 800.0 | 550.0      | 0.0  | 220.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 10 October 2023  | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 450.0        |
      | 20 October 2023  | Repayment        | 250.0  | 230.0     | 0.0      | 20.0 | 0.0       | 220.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 17 October 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |

   @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC6
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 300 EUR transaction amount
    When Admin sets the business date to "10 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 October 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 16 October 2023 | 500.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 270.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 30.0  | 30.0       | 0.0  | 220.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 550.0 | 30.0       | 0.0  | 470.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 300.0  | 280.0     | 0.0      | 20.0 | 0.0       | 470.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 October 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |

   @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC7
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 300 EUR transaction amount
    When Admin sets the business date to "10 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 October 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  |                 | 500.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 250.0 | 0.0        | 0.0  | 20.0        |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 50.0  | 50.0       | 0.0  | 200.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 550.0 | 50.0       | 0.0  | 470.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 450.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 October 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

   @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC8
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 300 EUR transaction amount
    When Admin sets the business date to "10 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "17 October 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 16 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 50.0  | 50.0       | 0.0  | 220.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 550.0 | 50.0       | 0.0  | 470.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 450.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 17 October 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

   @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC9
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 300 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 October 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  |                 | 500.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 250.0 | 0.0        | 0.0  | 20.0        |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 50.0  | 50.0       | 0.0  | 200.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 550.0 | 50.0       | 0.0  | 470.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 450.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 October 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

   @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC10
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 October 2023" due date and 20 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 300 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 16 October 2023 | 500.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 270.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 30.0  | 30.0       | 0.0  | 220.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 550.0 | 30.0       | 0.0  | 470.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 300.0  | 280.0     | 0.0      | 20.0 | 0.0       | 470.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 October 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |

   @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC11
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "15 October 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 October 2023" due date and 20 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 300 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 16 October 2023 | 500.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 270.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 30.0  | 30.0       | 0.0  | 220.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 550.0 | 30.0       | 0.0  | 470.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 300.0  | 280.0     | 0.0      | 20.0 | 0.0       | 470.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 15 October 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |


  Scenario: Waive charge on LP2 cumulative loan product
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2023   | 750            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2023" with "750" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2023"
    And Customer makes "AUTOPAY" repayment on "01 March 2023" with 250 EUR transaction amount
    When Admin sets the business date to "01 April 2023"
    And Customer makes "AUTOPAY" repayment on "01 April 2023" with 250 EUR transaction amount
    When Customer makes a repayment undo on "01 April 2023"
    When Admin sets the business date to "05 April 2023"
    And Admin adds an NSF fee because of payment bounce with "05 April 2023" transaction date
    When Admin sets the business date to "07 April 2023"
    And Admin waives charge
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of     | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 05 April 2023 | Flat             | 10.0 | 0.0  | 10.0   | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement       | 750.0  | 0.0       | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 January 2023  | Down Payment       | 188.0  | 188.0     | 0.0      | 0.0  | 0.0       | 562.0        |
      | 01 February 2023 | Repayment          | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 312.0        |
      | 01 March 2023    | Repayment          | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 62.0         |
      | 01 April 2023    | Repayment          | 250.0  | 62.0      | 0.0      | 0.0  | 0.0       | 0.0          |
      | 05 April 2023    | Waive loan charges | 10.0   | 0.0       | 0.0      | 0.0  | 0.0       | 62.0         |
    Then On Loan Transactions tab the "Repayment" Transaction with date "01 April 2023" is reverted
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Waived | Outstanding |
      |    |      | 01 January 2023  |                  | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |        |             |
      | 1  | 0    | 01 January 2023  | 01 January 2023  | 562.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 188.0 | 0.0        | 0.0   | 0.0    | 0.0         |
      | 2  | 15   | 16 January 2023  | 01 February 2023 | 375.0           | 187.0         | 0.0      | 0.0  | 0.0       | 187.0 | 187.0 | 0.0        | 187.0 | 0.0    | 0.0         |
      | 3  | 15   | 31 January 2023  | 01 March 2023    | 188.0           | 187.0         | 0.0      | 0.0  | 0.0       | 187.0 | 187.0 | 0.0        | 187.0 | 0.0    | 0.0         |
      | 4  | 15   | 15 February 2023 |                  | 0.0             | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 126.0 | 0.0        | 126.0 | 0.0    | 62.0        |
      | 5  | 49   | 05 April 2023    | 05 April 2023    | 0.0             | 0.0           | 0.0      | 0.0  | 10.0      | 10.0  | 0.0   | 0.0        | 0.0   | 10.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due | Paid  | In advance | Late | Waived | Outstanding |
      | 750           | 0        | 0    | 10        | 760 | 688.0 | 0          | 500  | 10     | 62.0        |


  Scenario: Waive charge on LP2 progressive loan
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2023   | 750            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2023" with "750" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2023"
    And Customer makes "AUTOPAY" repayment on "01 March 2023" with 250 EUR transaction amount
    When Admin sets the business date to "01 April 2023"
    And Customer makes "AUTOPAY" repayment on "01 April 2023" with 250 EUR transaction amount
    When Customer makes a repayment undo on "01 April 2023"
    When Admin sets the business date to "05 April 2023"
    And Admin adds an NSF fee because of payment bounce with "05 April 2023" transaction date
    When Admin sets the business date to "07 April 2023"
    And Admin waives charge
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of     | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 05 April 2023 | Flat             | 10.0 | 0.0  | 10.0   | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement       | 750.0  | 0.0       | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 February 2023 | Repayment          | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 March 2023    | Repayment          | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 01 April 2023    | Repayment          | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 05 April 2023    | Waive loan charges | 10.0   | 0.0       | 0.0      | 0.0  | 0.0       | 250.0        |
    Then On Loan Transactions tab the "Repayment" Transaction with date "01 April 2023" is reverted
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Waived | Outstanding |
      |    |      | 01 January 2023  |                  | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |        |             |
      | 1  | 0    | 01 January 2023  | 01 February 2023 | 562.5           | 187.5         | 0.0      | 0.0  | 0.0       | 187.5 | 187.5 | 0.0        | 187.5 | 0.0    | 0.0         |
      | 2  | 15   | 16 January 2023  | 01 March 2023    | 375.0           | 187.5         | 0.0      | 0.0  | 0.0       | 187.5 | 187.5 | 0.0        | 187.5 | 0.0    | 0.0         |
      | 3  | 15   | 31 January 2023  |                  | 187.5           | 187.5         | 0.0      | 0.0  | 0.0       | 187.5 | 125.0 | 0.0        | 125.0 | 0.0    | 62.5         |
      | 4  | 15   | 15 February 2023 |                  | 0.0             | 187.5         | 0.0      | 0.0  | 0.0       | 187.5 | 0.0   | 0.0        | 0.0   | 0.0    | 187.5       |
      | 5  | 49   | 05 April 2023    | 05 April 2023    | 0.0             | 0.0           | 0.0      | 0.0  | 10.0      | 10.0  | 0.0   | 0.0        | 0.0   | 10.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Waived | Outstanding |
      | 750           | 0        | 0    | 10        | 760 | 500  | 0          | 500  | 10     | 250         |


  Scenario: Verify that when a charge added after maturity had been waived the added N+1 installment will be paid with a paid by date (obligations met date) of the transaction date of the waive charge transaction
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "22 February 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "22 February 2023" due date and 100 EUR transaction amount
    When Admin sets the business date to "31 March 2023"
    And Admin waives due date charge
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Waived | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |        |             |
      | 1  | 0    | 01 January 2023  |                  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 0.0    | 250.0       |
      | 2  | 15   | 16 January 2023  |                  | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 0.0    | 250.0       |
      | 3  | 15   | 31 January 2023  |                  | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 0.0    | 250.0       |
      | 4  | 15   | 15 February 2023 |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 0.0    | 250.0       |
      | 5  | 7    | 22 February 2023 | 22 February 2023 | 0.0             | 0.0           | 0.0      | 0.0  | 100.0     | 100.0 | 0.0  | 0.0        | 0.0  | 100.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Waived | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 100.0     | 1100.0 | 0.0  | 0.0        | 0.0  | 100.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement       | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 22 February 2023 | Waive loan charges | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due   | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 22 February 2023 | Flat             | 100.0 | 0.0  | 100.0  | 0.0         |
