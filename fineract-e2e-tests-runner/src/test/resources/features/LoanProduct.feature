@Product
Feature: LoanProduct

  @Scenario1
  Scenario: As a user I would like to fully repay the loan in time
    When Admin sets the business date to "12 December 2021"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "12 December 2021", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "12 December 2021" with "1000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "12 December 2021" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "30 December 2021"
    And Customer makes "AUTOPAY" repayment on "30 December 2021" with 1000 EUR transaction amount
    Then Repayment transaction is created with 1000 amount and "AUTOPAY" type
    Then Loan has 0 outstanding amount

  @Scenario2
  Scenario: As a user I would like to fully repay a loan which was disbursed 2 times
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 June 2022", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 June 2022" with "1000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 June 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "5 June 2022"
    And Admin successfully disburse the loan on "5 June 2022" with "500" EUR transaction amount
    Then Loan has 1500 outstanding amount
    When Admin sets the business date to "1 July 2022"
    And Customer makes "AUTOPAY" repayment on "1 July 2022" with 1500 EUR transaction amount
    Then Repayment transaction is created with 1500 amount and "AUTOPAY" type
    Then Loan has 0 outstanding amount

  @Scenario3
  Scenario: As a user I would like to fully repay a multi disbursed loan with 2 repayments
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 June 2022", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 June 2022" with "1000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 June 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "15 June 2022"
    And Customer makes "AUTOPAY" repayment on "15 June 2022" with 500 EUR transaction amount
    Then Repayment transaction is created with 500 amount and "AUTOPAY" type
    Then Loan has 500 outstanding amount
    When Admin runs the Increase Business Date by 1 day job
    And Admin successfully disburse the loan on "16 June 2022" with "500" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "15 July 2022"
    And Customer makes "AUTOPAY" repayment on "15 July 2022" with 1000 EUR transaction amount
    Then Loan has 0 outstanding amount

  @Scenario4
  Scenario: As a user I would like to multi disburse a loan which was previously fully paid
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 June 2022", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 June 2022" with "1000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 June 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "15 June 2022"
    And Customer makes "AUTOPAY" repayment on "15 June 2022" with 1000 EUR transaction amount
    Then Repayment transaction is created with 1000 amount and "AUTOPAY" type
    Then Loan has 0 outstanding amount
    When Admin runs the Increase Business Date by 1 day job
    And Admin successfully disburse the loan on "16 June 2022" with "500" EUR transaction amount
    Then Loan has 500 outstanding amount
    When Admin sets the business date to "15 July 2022"
    And Customer makes "AUTOPAY" repayment on "15 July 2022" with 500 EUR transaction amount
    Then Loan has 0 outstanding amount

  @Scenario5
  Scenario: As a user I would like to fully repay a loan and check a repayment reversal with NSF fee
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 June 2022", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 June 2022" with "1000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 June 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "15 June 2022"
    And Customer makes "AUTOPAY" repayment on "15 June 2022" with 1000 EUR transaction amount
    Then Repayment transaction is created with 1000 amount and "AUTOPAY" type
    Then Loan has 0 outstanding amount
    When Customer makes a repayment undo on "15 June 2022"
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "18 June 2022"
    And Admin adds an NSF fee because of payment bounce with "18 June 2022" transaction date
    Then Loan has 1010 outstanding amount
    And Customer makes "AUTOPAY" repayment on "18 June 2022" with 1010 EUR transaction amount
    Then Loan has 0 outstanding amount

  @Scenario6
  Scenario: As a user I would like to repay the half amount of the loan and check a repayment reversal with NSF fee
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 June 2022", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 June 2022" with "1000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 June 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "15 June 2022"
    And Customer makes "AUTOPAY" repayment on "15 June 2022" with 500 EUR transaction amount
    Then Repayment transaction is created with 500 amount and "AUTOPAY" type
    Then Loan has 500 outstanding amount
    When Customer makes a repayment undo on "15 June 2022"
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "18 June 2022"
    And Admin adds an NSF fee because of payment bounce with "18 June 2022" transaction date
    Then Loan has 1010 outstanding amount
    And Customer makes "AUTOPAY" repayment on "18 June 2022" with 1010 EUR transaction amount
    Then Loan has 0 outstanding amount

  @Scenario7
  Scenario: As a user I would like to fully repay a loan then reverse the repayment + add an NSF fee after the 1 month period
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 June 2022", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 June 2022" with "1000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 June 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "1 July 2022"
    And Customer makes "AUTOPAY" repayment on "1 July 2022" with 1000 EUR transaction amount
    Then Repayment transaction is created with 1000 amount and "AUTOPAY" type
    Then Loan has 0 outstanding amount
    When Customer makes a repayment undo on "1 July 2022"
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "5 July 2022"
    And Admin adds an NSF fee because of payment bounce with "5 July 2022" transaction date
    Then Loan has 1010 outstanding amount
    And Customer makes "AUTOPAY" repayment on "5 July 2022" with 1010 EUR transaction amount
    Then Loan has 0 outstanding amount

  @Scenario8
  Scenario: As a user I would like to repay the half amount of the loan and do a refund
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 June 2022", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 June 2022" with "1000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 June 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "25 June 2022"
    And Customer makes "AUTOPAY" repayment on "25 June 2022" with 500 EUR transaction amount
    Then Repayment transaction is created with 500 amount and "AUTOPAY" type
    Then Loan has 500 outstanding amount
    When Admin sets the business date to "1 July 2022"
    When Refund happens on "1 July 2022" with 500 EUR transaction amount
    Then Loan has 0 outstanding amount

  @Scenario9
  Scenario: As a user I would like to repay the half amount of the loan and do a refund + repayment reversal on the repayment
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 June 2022", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 June 2022" with "1000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 June 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    And Admin sets the business date to "25 June 2022"
    And Customer makes "AUTOPAY" repayment on "25 June 2022" with 500 EUR transaction amount
    Then Repayment transaction is created with 500 amount and "AUTOPAY" type
    Then Loan has 500 outstanding amount
    When Admin sets the business date to "1 July 2022"
    When Refund happens on "1 July 2022" with 500 EUR transaction amount
    Then Loan has 0 outstanding amount
    When Customer makes a repayment undo on "25 June 2022"
    Then Loan has 500 outstanding amount

  @Scenario10
  Scenario: As a user I would like to repay the half amount of the loan and do a refund + repayment reversal + refund reversal
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 June 2022", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 June 2022" with "1000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 June 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "25 June 2022"
    And Customer makes "AUTOPAY" repayment on "25 June 2022" with 500 EUR transaction amount
    Then Repayment transaction is created with 500 amount and "AUTOPAY" type
    Then Loan has 500 outstanding amount
    When Admin sets the business date to "1 July 2022"
    When Refund happens on "1 July 2022" with 500 EUR transaction amount
    Then Loan has 0 outstanding amount
    When Customer makes a repayment undo on "25 June 2022"
    Then Loan has 500 outstanding amount
    When Refund undo happens on "1 July 2022"
    Then Loan has 1000 outstanding amount
