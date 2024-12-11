@Repayment
Feature: LoanRepayment

  @TestRailId:C49
  Scenario Outline: Loan repayment functionality with business date setup
    When Admin sets the business date to <businessDate>
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on <approveDate> with "5000" amount and expected disbursement date on <expectedDisbursementDate>
    And Admin successfully disburse the loan on <disbursementDate> with "5000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on <repaymentDate> with <transactionAmount> EUR transaction amount
    Then Repayment transaction is created with 200 amount and "AUTOPAY" type
    Examples:
      | businessDate  | approveDate   | expectedDisbursementDate | disbursementDate | repaymentDate | transactionAmount |
      | "1 July 2022" | "1 July 2022" | "1 July 2022"            | "1 July 2022"    | "1 July 2022" | 200               |

  @TestRailId:C32
  Scenario: As a user I would like to check that the repayment transaction is failed when the repayment date is after the business date
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "2 July 2022" with 200 EUR transaction amount (and transaction fails because of wrong date)
    Then Repayment failed because the repayment date is after the business date

  @TestRailId:C44
  Scenario: As a user I would like to check that the repayment is successful if the repayment date is equal to the business date
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "1 July 2022" with 200 EUR transaction amount
    Then Repayment transaction is created with 200 amount and "AUTOPAY" type

  @TestRailId:C45
  Scenario: As a user I would like to increase the business day by the scheduled job and want to create a repayment transaction on that day
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount
    And Admin runs the Increase Business Date by 1 day job
    And Customer makes "AUTOPAY" repayment on "2 July 2022" with 200 EUR transaction amount
    Then Repayment transaction is created with 200 amount and "AUTOPAY" type

  @TestRailId:C2430
  Scenario: Verify that as a user I am able to make a repayment with AutoPay type
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount
    And Admin runs the Increase Business Date by 1 day job
    And Customer makes "AUTOPAY" repayment on "2 July 2022" with 200 EUR transaction amount
    Then Repayment transaction is created with 200 amount and "AUTOPAY" type

  @TestRailId:C2431
  Scenario: Verify that as a user I am able to make a repayment with Down payment type
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount
    And Admin runs the Increase Business Date by 1 day job
    And Customer makes "DOWN_PAYMENT" repayment on "2 July 2022" with 200 EUR transaction amount
    Then Repayment transaction is created with 200 amount and "DOWN_PAYMENT" type

  @TestRailId:C2432
  Scenario: Verify that as a user I am able to make a repayment with Real time type
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount
    And Admin runs the Increase Business Date by 1 day job
    And Customer makes "REAL_TIME" repayment on "2 July 2022" with 200 EUR transaction amount
    Then Repayment transaction is created with 200 amount and "REAL_TIME" type

  @TestRailId:C2433
  Scenario: Verify that as a user I am able to make a repayment with Scheduled type
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount
    And Admin runs the Increase Business Date by 1 day job
    And Customer makes "SCHEDULED" repayment on "2 July 2022" with 200 EUR transaction amount
    Then Repayment transaction is created with 200 amount and "SCHEDULED" type

  @TestRailId:C2434
  Scenario: Verify that as a user I am able to make a repayment with Check payment type
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount
    And Admin runs the Increase Business Date by 1 day job
    And Customer makes "CHECK_PAYMENT" repayment on "2 July 2022" with 200 EUR transaction amount
    Then Repayment transaction is created with 200 amount and "CHECK_PAYMENT" type

  @TestRailId:C2435
  Scenario: Verify that as a user I am able to make a repayment with Oca payment type
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount
    And Admin runs the Increase Business Date by 1 day job
    And Customer makes "OCA_PAYMENT" repayment on "2 July 2022" with 200 EUR transaction amount
    Then Repayment transaction is created with 200 amount and "OCA_PAYMENT" type

  @TestRailId:C2436
  Scenario: Verify that as a user I am able to make a repayment with Adjustment chargeback payment type
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount
    And Admin runs the Increase Business Date by 1 day job
    And Customer makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" repayment on "2 July 2022" with 200 EUR transaction amount
    Then Repayment transaction is created with 200 amount and "Repayment Adjustment Chargeback" type

  @TestRailId:C2437
  Scenario: Verify that as a user I am able to make a repayment with Adjustment refund payment type
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount
    And Admin runs the Increase Business Date by 1 day job
    And Customer makes "REPAYMENT_ADJUSTMENT_REFUND" repayment on "2 July 2022" with 200 EUR transaction amount
    Then Repayment transaction is created with 200 amount and "Repayment Adjustment Refund" type

  @TestRailId:C2464
  Scenario: As a user I would like to check the Autopay repayment undo and repayment after loan closed state
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "1 July 2022" with 5000 EUR transaction amount
    Then Repayment transaction is created with 5000 amount and "AUTOPAY" type
    And Customer makes a repayment undo on "1 July 2022"
    And Loan has 5000 outstanding amount
    And Customer makes "AUTOPAY" repayment on "1 July 2022" with 5000 EUR transaction amount
    Then Repayment transaction is created with 5000 amount and "AUTOPAY" type
    Then Loan has 0 outstanding amount
    When Customer makes "AUTOPAY" repayment on "1 July 2022" with 100 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount

  @TestRailId:C2465
  Scenario: As a user I would like to check the Down payment repayment undo and repayment after loan closed state
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount
    And Customer makes "DOWN_PAYMENT" repayment on "1 July 2022" with 5000 EUR transaction amount
    Then Repayment transaction is created with 5000 amount and "DOWN_PAYMENT" type
    And Customer makes a repayment undo on "1 July 2022"
    And Loan has 5000 outstanding amount
    And Customer makes "DOWN_PAYMENT" repayment on "1 July 2022" with 5000 EUR transaction amount
    Then Repayment transaction is created with 5000 amount and "DOWN_PAYMENT" type
    Then Loan has 0 outstanding amount
    When Customer makes "DOWN_PAYMENT" repayment on "1 July 2022" with 100 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount

  @TestRailId:C2466
  Scenario: As a user I would like to check the real time repayment undo and repayment after loan closed state
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount
    And Customer makes "REAL_TIME" repayment on "1 July 2022" with 5000 EUR transaction amount
    Then Repayment transaction is created with 5000 amount and "REAL_TIME" type
    And Customer makes a repayment undo on "1 July 2022"
    And Loan has 5000 outstanding amount
    And Customer makes "REAL_TIME" repayment on "1 July 2022" with 5000 EUR transaction amount
    Then Repayment transaction is created with 5000 amount and "REAL_TIME" type
    Then Loan has 0 outstanding amount
    When Customer makes "REAL_TIME" repayment on "1 July 2022" with 100 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount

  @TestRailId:C2467
  Scenario: As a user I would like to check the scheduled repayment undo and repayment after loan closed state
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount
    And Customer makes "SCHEDULED" repayment on "1 July 2022" with 5000 EUR transaction amount
    Then Repayment transaction is created with 5000 amount and "SCHEDULED" type
    And Customer makes a repayment undo on "1 July 2022"
    And Loan has 5000 outstanding amount
    And Customer makes "SCHEDULED" repayment on "1 July 2022" with 5000 EUR transaction amount
    Then Repayment transaction is created with 5000 amount and "SCHEDULED" type
    Then Loan has 0 outstanding amount
    When Customer makes "SCHEDULED" repayment on "1 July 2022" with 100 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount

  @TestRailId:C2468
  Scenario: As a user I would like to check the check payment repayment undo and repayment after loan closed state
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount
    And Customer makes "CHECK_PAYMENT" repayment on "1 July 2022" with 5000 EUR transaction amount
    Then Repayment transaction is created with 5000 amount and "CHECK_PAYMENT" type
    And Customer makes a repayment undo on "1 July 2022"
    And Loan has 5000 outstanding amount
    And Customer makes "CHECK_PAYMENT" repayment on "1 July 2022" with 5000 EUR transaction amount
    Then Repayment transaction is created with 5000 amount and "CHECK_PAYMENT" type
    Then Loan has 0 outstanding amount
    When Customer makes "CHECK_PAYMENT" repayment on "1 July 2022" with 100 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount

  @TestRailId:C2469
  Scenario: As a user I would like to check the oca payment repayment undo and repayment after loan closed state
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount
    And Customer makes "OCA_PAYMENT" repayment on "1 July 2022" with 5000 EUR transaction amount
    Then Repayment transaction is created with 5000 amount and "OCA_PAYMENT" type
    And Customer makes a repayment undo on "1 July 2022"
    And Loan has 5000 outstanding amount
    And Customer makes "OCA_PAYMENT" repayment on "1 July 2022" with 5000 EUR transaction amount
    Then Repayment transaction is created with 5000 amount and "OCA_PAYMENT" type
    Then Loan has 0 outstanding amount
    When Customer makes "OCA_PAYMENT" repayment on "1 July 2022" with 100 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount

  @TestRailId:C2470
  Scenario: As a user I would like to check the repayment adjustment chargeback repayment undo and repayment after loan closed state
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount
    And Customer makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" repayment on "1 July 2022" with 5000 EUR transaction amount
    Then Repayment transaction is created with 5000 amount and "Repayment Adjustment Chargeback" type
    And Customer makes a repayment undo on "1 July 2022"
    And Loan has 5000 outstanding amount
    And Customer makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" repayment on "1 July 2022" with 5000 EUR transaction amount
    Then Repayment transaction is created with 5000 amount and "Repayment Adjustment Chargeback" type
    Then Loan has 0 outstanding amount
    When Customer makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" repayment on "1 July 2022" with 100 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount

  @TestRailId:C2471
  Scenario: As a user I would like to check the repayment adjustment refund repayment undo and repayment after loan closed state
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount
    And Customer makes "REPAYMENT_ADJUSTMENT_REFUND" repayment on "1 July 2022" with 5000 EUR transaction amount
    Then Repayment transaction is created with 5000 amount and "Repayment Adjustment Refund" type
    And Customer makes a repayment undo on "1 July 2022"
    And Loan has 5000 outstanding amount
    And Customer makes "REPAYMENT_ADJUSTMENT_REFUND" repayment on "1 July 2022" with 5000 EUR transaction amount
    Then Repayment transaction is created with 5000 amount and "Repayment Adjustment Refund" type
    Then Loan has 0 outstanding amount
    When Customer makes "REPAYMENT_ADJUSTMENT_REFUND" repayment on "1 July 2022" with 100 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount

  @TestRailId:C2485
  Scenario: Verify that inlineCOB job creates two separate events for LoanRepaymentDueBusinessEvent and LoanRepaymentOverdueBusinessEvent: due and overdue days values from global config
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    And Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "31 January 2023"
    When Admin runs inline COB job for Loan
    Then Loan Repayment Due Business Event is created
    When Admin sets the business date to "03 February 2023"
    When Admin runs inline COB job for Loan
    Then Loan Repayment Overdue Business Event is created

  @TestRailId:C2689
  Scenario: Verify that inlineCOB job creates two separate events for LoanRepaymentDueBusinessEvent and LoanRepaymentOverdueBusinessEvent: due and overdue days values from Loan product
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct  | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_DUE_DATE | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    And Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "30 January 2023"
    When Admin runs inline COB job for Loan
    Then Loan Repayment Due Business Event is created
    When Admin sets the business date to "05 February 2023"
    When Admin runs inline COB job for Loan
    Then Loan Repayment Overdue Business Event is created

  @TestRailId:C2490
  Scenario: RS01 - Repayment Schedule with interest type: flat, interest period: Same as payment period, amortization type: Equal installments
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 1 November 2022   | 5000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 18                | MONTHS                | 1              | MONTHS                 | 18                 | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    Then Loan Repayment schedule has 18 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 November 2022  |           | 5000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 30   | 01 December 2022  |           | 4722.0          | 278.0         | 50.0     | 0.0  | 0.0       | 328.0 | 0.0  | 0.0        | 0.0  | 328.0       |
      | 2  | 31   | 01 January 2023   |           | 4444.0          | 278.0         | 50.0     | 0.0  | 0.0       | 328.0 | 0.0  | 0.0        | 0.0  | 328.0       |
      | 3  | 31   | 01 February 2023  |           | 4166.0          | 278.0         | 50.0     | 0.0  | 0.0       | 328.0 | 0.0  | 0.0        | 0.0  | 328.0       |
      | 4  | 28   | 01 March 2023     |           | 3888.0          | 278.0         | 50.0     | 0.0  | 0.0       | 328.0 | 0.0  | 0.0        | 0.0  | 328.0       |
      | 5  | 31   | 01 April 2023     |           | 3610.0          | 278.0         | 50.0     | 0.0  | 0.0       | 328.0 | 0.0  | 0.0        | 0.0  | 328.0       |
      | 6  | 30   | 01 May 2023       |           | 3332.0          | 278.0         | 50.0     | 0.0  | 0.0       | 328.0 | 0.0  | 0.0        | 0.0  | 328.0       |
      | 7  | 31   | 01 June 2023      |           | 3054.0          | 278.0         | 50.0     | 0.0  | 0.0       | 328.0 | 0.0  | 0.0        | 0.0  | 328.0       |
      | 8  | 30   | 01 July 2023      |           | 2776.0          | 278.0         | 50.0     | 0.0  | 0.0       | 328.0 | 0.0  | 0.0        | 0.0  | 328.0       |
      | 9  | 31   | 01 August 2023    |           | 2498.0          | 278.0         | 50.0     | 0.0  | 0.0       | 328.0 | 0.0  | 0.0        | 0.0  | 328.0       |
      | 10 | 31   | 01 September 2023 |           | 2220.0          | 278.0         | 50.0     | 0.0  | 0.0       | 328.0 | 0.0  | 0.0        | 0.0  | 328.0       |
      | 11 | 30   | 01 October 2023   |           | 1942.0          | 278.0         | 50.0     | 0.0  | 0.0       | 328.0 | 0.0  | 0.0        | 0.0  | 328.0       |
      | 12 | 31   | 01 November 2023  |           | 1664.0          | 278.0         | 50.0     | 0.0  | 0.0       | 328.0 | 0.0  | 0.0        | 0.0  | 328.0       |
      | 13 | 30   | 01 December 2023  |           | 1386.0          | 278.0         | 50.0     | 0.0  | 0.0       | 328.0 | 0.0  | 0.0        | 0.0  | 328.0       |
      | 14 | 31   | 01 January 2024   |           | 1108.0          | 278.0         | 50.0     | 0.0  | 0.0       | 328.0 | 0.0  | 0.0        | 0.0  | 328.0       |
      | 15 | 31   | 01 February 2024  |           | 830.0           | 278.0         | 50.0     | 0.0  | 0.0       | 328.0 | 0.0  | 0.0        | 0.0  | 328.0       |
      | 16 | 29   | 01 March 2024     |           | 552.0           | 278.0         | 50.0     | 0.0  | 0.0       | 328.0 | 0.0  | 0.0        | 0.0  | 328.0       |
      | 17 | 31   | 01 April 2024     |           | 274.0           | 278.0         | 50.0     | 0.0  | 0.0       | 328.0 | 0.0  | 0.0        | 0.0  | 328.0       |
      | 18 | 30   | 01 May 2024       |           | 0.0             | 274.0         | 50.0     | 0.0  | 0.0       | 324.0 | 0.0  | 0.0        | 0.0  | 324.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 5000          | 900      | 0    | 0         | 5900 | 0    | 0          | 0    | 5900        |

  @TestRailId:C2492
  Scenario: RS02 - Repayment Schedule with interest type: Declining Balance, interest period: Same as payment period, amortization type: Equal installments
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_SAME_AS_PAYMENT | 1 November 2022   | 5000           | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 18                | MONTHS                | 1              | MONTHS                 | 18                 | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    Then Loan Repayment schedule has 18 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 November 2022  |           | 5000.0          |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 30   | 01 December 2022  |           | 4745.0          | 255.0         | 50.0     | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 2  | 31   | 01 January 2023   |           | 4487.45         | 257.55        | 47.45    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 3  | 31   | 01 February 2023  |           | 4227.32         | 260.13        | 44.87    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 4  | 28   | 01 March 2023     |           | 3964.59         | 262.73        | 42.27    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 5  | 31   | 01 April 2023     |           | 3699.24         | 265.35        | 39.65    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 6  | 30   | 01 May 2023       |           | 3431.23         | 268.01        | 36.99    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 7  | 31   | 01 June 2023      |           | 3160.54         | 270.69        | 34.31    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 8  | 30   | 01 July 2023      |           | 2887.15         | 273.39        | 31.61    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 9  | 31   | 01 August 2023    |           | 2611.02         | 276.13        | 28.87    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 10 | 31   | 01 September 2023 |           | 2332.13         | 278.89        | 26.11    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 11 | 30   | 01 October 2023   |           | 2050.45         | 281.68        | 23.32    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 12 | 31   | 01 November 2023  |           | 1765.95         | 284.5         | 20.5     | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 13 | 30   | 01 December 2023  |           | 1478.61         | 287.34        | 17.66    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 14 | 31   | 01 January 2024   |           | 1188.4          | 290.21        | 14.79    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 15 | 31   | 01 February 2024  |           | 895.28          | 293.12        | 11.88    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 16 | 29   | 01 March 2024     |           | 599.23          | 296.05        | 8.95     | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 17 | 31   | 01 April 2024     |           | 300.22          | 299.01        | 5.99     | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 18 | 30   | 01 May 2024       |           | 0.0             | 300.22        | 3.0      | 0.0  | 0.0       | 303.22 | 0.0  | 0.0        | 0.0  | 303.22      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 5000          | 488.22   | 0    | 0         | 5488.22 | 0    | 0          | 0    | 5488.22     |

  @TestRailId:C2493
  Scenario: RS03 - Repayment Schedule with interest type: Declining Balance, interest period: Daily, amortization type: Equal installments
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY | 1 November 2022   | 5000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 18                | MONTHS                | 1              | MONTHS                 | 18                 | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    Then Loan Repayment schedule has 18 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 November 2022  |           | 5000.0          |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 30   | 01 December 2022  |           | 4744.32         | 255.68        | 49.32    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 2  | 31   | 01 January 2023   |           | 4487.67         | 256.65        | 48.35    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 3  | 31   | 01 February 2023  |           | 4228.41         | 259.26        | 45.74    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 4  | 28   | 01 March 2023     |           | 3962.33         | 266.08        | 38.92    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 5  | 31   | 01 April 2023     |           | 3697.71         | 264.62        | 40.38    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 6  | 30   | 01 May 2023       |           | 3429.18         | 268.53        | 36.47    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 7  | 31   | 01 June 2023      |           | 3159.13         | 270.05        | 34.95    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 8  | 30   | 01 July 2023      |           | 2885.29         | 273.84        | 31.16    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 9  | 31   | 01 August 2023    |           | 2609.7          | 275.59        | 29.41    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 10 | 31   | 01 September 2023 |           | 2331.3          | 278.4         | 26.6     | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 11 | 30   | 01 October 2023   |           | 2049.29         | 282.01        | 22.99    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 12 | 31   | 01 November 2023  |           | 1765.18         | 284.11        | 20.89    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 13 | 30   | 01 December 2023  |           | 1477.59         | 287.59        | 17.41    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 14 | 31   | 01 January 2024   |           | 1187.65         | 289.94        | 15.06    | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 15 | 31   | 01 February 2024  |           | 894.75          | 292.9         | 12.1     | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 16 | 29   | 01 March 2024     |           | 598.28          | 296.47        | 8.53     | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 17 | 31   | 01 April 2024     |           | 299.38          | 298.9         | 6.1      | 0.0  | 0.0       | 305.0  | 0.0  | 0.0        | 0.0  | 305.0       |
      | 18 | 30   | 01 May 2024       |           | 0.0             | 299.38        | 2.95     | 0.0  | 0.0       | 302.33 | 0.0  | 0.0        | 0.0  | 302.33      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 5000          | 487.33   | 0    | 0         | 5487.33 | 0    | 0          | 0    | 5487.33     |

  @TestRailId:C2494
  Scenario: RS04 - Repayment Schedule with interest type: Declining Balance, interest period: Same as payment period, amortization type: Equal principal payments
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type        | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_SAME_AS_PAYMENT | 1 November 2022   | 5000           | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_PRINCIPAL_PAYMENTS | 18                | MONTHS                | 1              | MONTHS                 | 18                 | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    Then Loan Repayment schedule has 18 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 November 2022  |           | 5000.0          |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 30   | 01 December 2022  |           | 4722.22         | 277.78        | 50.0     | 0.0  | 0.0       | 327.78 | 0.0  | 0.0        | 0.0  | 327.78      |
      | 2  | 31   | 01 January 2023   |           | 4444.44         | 277.78        | 47.22    | 0.0  | 0.0       | 325.0  | 0.0  | 0.0        | 0.0  | 325.0       |
      | 3  | 31   | 01 February 2023  |           | 4166.66         | 277.78        | 44.44    | 0.0  | 0.0       | 322.22 | 0.0  | 0.0        | 0.0  | 322.22      |
      | 4  | 28   | 01 March 2023     |           | 3888.88         | 277.78        | 41.67    | 0.0  | 0.0       | 319.45 | 0.0  | 0.0        | 0.0  | 319.45      |
      | 5  | 31   | 01 April 2023     |           | 3611.1          | 277.78        | 38.89    | 0.0  | 0.0       | 316.67 | 0.0  | 0.0        | 0.0  | 316.67      |
      | 6  | 30   | 01 May 2023       |           | 3333.32         | 277.78        | 36.11    | 0.0  | 0.0       | 313.89 | 0.0  | 0.0        | 0.0  | 313.89      |
      | 7  | 31   | 01 June 2023      |           | 3055.54         | 277.78        | 33.33    | 0.0  | 0.0       | 311.11 | 0.0  | 0.0        | 0.0  | 311.11      |
      | 8  | 30   | 01 July 2023      |           | 2777.76         | 277.78        | 30.56    | 0.0  | 0.0       | 308.34 | 0.0  | 0.0        | 0.0  | 308.34      |
      | 9  | 31   | 01 August 2023    |           | 2499.98         | 277.78        | 27.78    | 0.0  | 0.0       | 305.56 | 0.0  | 0.0        | 0.0  | 305.56      |
      | 10 | 31   | 01 September 2023 |           | 2222.2          | 277.78        | 25.0     | 0.0  | 0.0       | 302.78 | 0.0  | 0.0        | 0.0  | 302.78      |
      | 11 | 30   | 01 October 2023   |           | 1944.42         | 277.78        | 22.22    | 0.0  | 0.0       | 300.0  | 0.0  | 0.0        | 0.0  | 300.0       |
      | 12 | 31   | 01 November 2023  |           | 1666.64         | 277.78        | 19.44    | 0.0  | 0.0       | 297.22 | 0.0  | 0.0        | 0.0  | 297.22      |
      | 13 | 30   | 01 December 2023  |           | 1388.86         | 277.78        | 16.67    | 0.0  | 0.0       | 294.45 | 0.0  | 0.0        | 0.0  | 294.45      |
      | 14 | 31   | 01 January 2024   |           | 1111.08         | 277.78        | 13.89    | 0.0  | 0.0       | 291.67 | 0.0  | 0.0        | 0.0  | 291.67      |
      | 15 | 31   | 01 February 2024  |           | 833.3           | 277.78        | 11.11    | 0.0  | 0.0       | 288.89 | 0.0  | 0.0        | 0.0  | 288.89      |
      | 16 | 29   | 01 March 2024     |           | 555.52          | 277.78        | 8.33     | 0.0  | 0.0       | 286.11 | 0.0  | 0.0        | 0.0  | 286.11      |
      | 17 | 31   | 01 April 2024     |           | 277.74          | 277.78        | 5.56     | 0.0  | 0.0       | 283.34 | 0.0  | 0.0        | 0.0  | 283.34      |
      | 18 | 30   | 01 May 2024       |           | 0.0             | 277.74        | 2.78     | 0.0  | 0.0       | 280.52 | 0.0  | 0.0        | 0.0  | 280.52      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 5000          | 475.0    | 0    | 0         | 5475.0 | 0    | 0          | 0    | 5475.0      |

  @TestRailId:C2495
  Scenario: RS05 - Repayment Schedule with interest type: Declining Balance, interest period: Same as payment period, amortization type: Equal installments, Grace on principal payment
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_SAME_AS_PAYMENT | 1 November 2022   | 5000           | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 18                | MONTHS                | 1              | MONTHS                 | 18                 | 3                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    Then Loan Repayment schedule has 18 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 November 2022  |           | 5000.0          |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 30   | 01 December 2022  |           | 5000.0          | 0.0           | 50.0     | 0.0  | 0.0       | 50.0   | 0.0  | 0.0        | 0.0  | 50.0        |
      | 2  | 31   | 01 January 2023   |           | 5000.0          | 0.0           | 50.0     | 0.0  | 0.0       | 50.0   | 0.0  | 0.0        | 0.0  | 50.0        |
      | 3  | 31   | 01 February 2023  |           | 5000.0          | 0.0           | 50.0     | 0.0  | 0.0       | 50.0   | 0.0  | 0.0        | 0.0  | 50.0        |
      | 4  | 28   | 01 March 2023     |           | 4689.0          | 311.0         | 50.0     | 0.0  | 0.0       | 361.0  | 0.0  | 0.0        | 0.0  | 361.0       |
      | 5  | 31   | 01 April 2023     |           | 4374.89         | 314.11        | 46.89    | 0.0  | 0.0       | 361.0  | 0.0  | 0.0        | 0.0  | 361.0       |
      | 6  | 30   | 01 May 2023       |           | 4057.64         | 317.25        | 43.75    | 0.0  | 0.0       | 361.0  | 0.0  | 0.0        | 0.0  | 361.0       |
      | 7  | 31   | 01 June 2023      |           | 3737.22         | 320.42        | 40.58    | 0.0  | 0.0       | 361.0  | 0.0  | 0.0        | 0.0  | 361.0       |
      | 8  | 30   | 01 July 2023      |           | 3413.59         | 323.63        | 37.37    | 0.0  | 0.0       | 361.0  | 0.0  | 0.0        | 0.0  | 361.0       |
      | 9  | 31   | 01 August 2023    |           | 3086.73         | 326.86        | 34.14    | 0.0  | 0.0       | 361.0  | 0.0  | 0.0        | 0.0  | 361.0       |
      | 10 | 31   | 01 September 2023 |           | 2756.6          | 330.13        | 30.87    | 0.0  | 0.0       | 361.0  | 0.0  | 0.0        | 0.0  | 361.0       |
      | 11 | 30   | 01 October 2023   |           | 2423.17         | 333.43        | 27.57    | 0.0  | 0.0       | 361.0  | 0.0  | 0.0        | 0.0  | 361.0       |
      | 12 | 31   | 01 November 2023  |           | 2086.4          | 336.77        | 24.23    | 0.0  | 0.0       | 361.0  | 0.0  | 0.0        | 0.0  | 361.0       |
      | 13 | 30   | 01 December 2023  |           | 1746.26         | 340.14        | 20.86    | 0.0  | 0.0       | 361.0  | 0.0  | 0.0        | 0.0  | 361.0       |
      | 14 | 31   | 01 January 2024   |           | 1402.72         | 343.54        | 17.46    | 0.0  | 0.0       | 361.0  | 0.0  | 0.0        | 0.0  | 361.0       |
      | 15 | 31   | 01 February 2024  |           | 1055.75         | 346.97        | 14.03    | 0.0  | 0.0       | 361.0  | 0.0  | 0.0        | 0.0  | 361.0       |
      | 16 | 29   | 01 March 2024     |           | 705.31          | 350.44        | 10.56    | 0.0  | 0.0       | 361.0  | 0.0  | 0.0        | 0.0  | 361.0       |
      | 17 | 31   | 01 April 2024     |           | 351.36          | 353.95        | 7.05     | 0.0  | 0.0       | 361.0  | 0.0  | 0.0        | 0.0  | 361.0       |
      | 18 | 30   | 01 May 2024       |           | 0.0             | 351.36        | 3.51     | 0.0  | 0.0       | 354.87 | 0.0  | 0.0        | 0.0  | 354.87      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 5000          | 558.87   | 0    | 0         | 5558.87 | 0    | 0          | 0    | 5558.87     |

  @TestRailId:C2496
  Scenario: RS06 - Repayment Schedule with interest type: Declining Balance, interest period: Same as payment period, amortization type: Equal installments, Grace on principal payment and interest payment
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_SAME_AS_PAYMENT | 1 November 2022   | 5000           | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 18                | MONTHS                | 1              | MONTHS                 | 18                 | 6                       | 3                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    Then Loan Repayment schedule has 18 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 November 2022  |           | 5000.0          |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 30   | 01 December 2022  |           | 5000.0          | 0.0           | 50.0     | 0.0  | 0.0       | 50.0   | 0.0  | 0.0        | 0.0  | 50.0        |
      | 2  | 31   | 01 January 2023   |           | 5000.0          | 0.0           | 50.0     | 0.0  | 0.0       | 50.0   | 0.0  | 0.0        | 0.0  | 50.0        |
      | 3  | 31   | 01 February 2023  |           | 5000.0          | 0.0           | 50.0     | 0.0  | 0.0       | 50.0   | 0.0  | 0.0        | 0.0  | 50.0        |
      | 4  | 28   | 01 March 2023     |           | 5000.0          | 0.0           | 50.0     | 0.0  | 0.0       | 50.0   | 0.0  | 0.0        | 0.0  | 50.0        |
      | 5  | 31   | 01 April 2023     |           | 5000.0          | 0.0           | 50.0     | 0.0  | 0.0       | 50.0   | 0.0  | 0.0        | 0.0  | 50.0        |
      | 6  | 30   | 01 May 2023       |           | 5000.0          | 0.0           | 50.0     | 0.0  | 0.0       | 50.0   | 0.0  | 0.0        | 0.0  | 50.0        |
      | 7  | 31   | 01 June 2023      |           | 4606.0          | 394.0         | 50.0     | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 8  | 30   | 01 July 2023      |           | 4208.06         | 397.94        | 46.06    | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 9  | 31   | 01 August 2023    |           | 3806.14         | 401.92        | 42.08    | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 10 | 31   | 01 September 2023 |           | 3400.2          | 405.94        | 38.06    | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 11 | 30   | 01 October 2023   |           | 2990.2          | 410.0         | 34.0     | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 12 | 31   | 01 November 2023  |           | 2576.1          | 414.1         | 29.9     | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 13 | 30   | 01 December 2023  |           | 2157.86         | 418.24        | 25.76    | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 14 | 31   | 01 January 2024   |           | 1735.44         | 422.42        | 21.58    | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 15 | 31   | 01 February 2024  |           | 1308.79         | 426.65        | 17.35    | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 16 | 29   | 01 March 2024     |           | 877.88          | 430.91        | 13.09    | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 17 | 31   | 01 April 2024     |           | 442.66          | 435.22        | 8.78     | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 18 | 30   | 01 May 2024       |           | 0.0             | 442.66        | 4.43     | 0.0  | 0.0       | 447.09 | 0.0  | 0.0        | 0.0  | 447.09      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 5000          | 631.09   | 0    | 0         | 5631.09 | 0    | 0          | 0    | 5631.09     |

  @TestRailId:C2497
  Scenario: RS07 - Repayment Schedule with interest type: Declining Balance, interest period: Same as payment period, amortization type: Equal installments, Grace on principal payment and setting up interest free period
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_SAME_AS_PAYMENT | 1 November 2022   | 5000           | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 18                | MONTHS                | 1              | MONTHS                 | 18                 | 6                       | 0                      | 3                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    Then Loan Repayment schedule has 18 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 November 2022  |           | 5000.0          |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 30   | 01 December 2022  |           | 5000.0          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 January 2023   |           | 5000.0          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2023  |           | 5000.0          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 4  | 28   | 01 March 2023     |           | 5000.0          | 0.0           | 200.0    | 0.0  | 0.0       | 200.0  | 0.0  | 0.0        | 0.0  | 200.0       |
      | 5  | 31   | 01 April 2023     |           | 5000.0          | 0.0           | 50.0     | 0.0  | 0.0       | 50.0   | 0.0  | 0.0        | 0.0  | 50.0        |
      | 6  | 30   | 01 May 2023       |           | 5000.0          | 0.0           | 50.0     | 0.0  | 0.0       | 50.0   | 0.0  | 0.0        | 0.0  | 50.0        |
      | 7  | 31   | 01 June 2023      |           | 4606.0          | 394.0         | 50.0     | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 8  | 30   | 01 July 2023      |           | 4208.06         | 397.94        | 46.06    | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 9  | 31   | 01 August 2023    |           | 3806.14         | 401.92        | 42.08    | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 10 | 31   | 01 September 2023 |           | 3400.2          | 405.94        | 38.06    | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 11 | 30   | 01 October 2023   |           | 2990.2          | 410.0         | 34.0     | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 12 | 31   | 01 November 2023  |           | 2576.1          | 414.1         | 29.9     | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 13 | 30   | 01 December 2023  |           | 2157.86         | 418.24        | 25.76    | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 14 | 31   | 01 January 2024   |           | 1735.44         | 422.42        | 21.58    | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 15 | 31   | 01 February 2024  |           | 1308.79         | 426.65        | 17.35    | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 16 | 29   | 01 March 2024     |           | 877.88          | 430.91        | 13.09    | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 17 | 31   | 01 April 2024     |           | 442.66          | 435.22        | 8.78     | 0.0  | 0.0       | 444.0  | 0.0  | 0.0        | 0.0  | 444.0       |
      | 18 | 30   | 01 May 2024       |           | 0.0             | 442.66        | 4.43     | 0.0  | 0.0       | 447.09 | 0.0  | 0.0        | 0.0  | 447.09      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 5000          | 631.09   | 0    | 0         | 5631.09 | 0    | 0          | 0    | 5631.09     |

  @TestRailId:C2498
  Scenario: As admin I would like to be sure that Edit from Goodwill Credit of on loan transaction can not be done
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 November 2022"
    And Admin successfully approves the loan on "1 November 2022" with "1000" amount and expected disbursement date on "1 November 2022"
    When Admin successfully disburse the loan on "1 November 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "15 November 2022"
    And Customer makes "AUTOPAY" repayment on "15 November 2022" with 1000 EUR transaction amount
    And Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "15 November 2022" with 200 EUR transaction amount and self-generated Idempotency key
    Then Loan "loanPaymentTransactionResponse" transaction adjust amount 900 must return 403 code

  @TestRailId:C2499
  Scenario: As admin I would like to be sure that Edit from Payout Refund of on loan transaction can not be done
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 November 2022"
    And Admin successfully approves the loan on "1 November 2022" with "1000" amount and expected disbursement date on "1 November 2022"
    When Admin successfully disburse the loan on "1 November 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "15 November 2022"
    And Customer makes "AUTOPAY" repayment on "15 November 2022" with 1000 EUR transaction amount
    And Refund happens on "15 November 2022" with 100 EUR transaction amount
    Then Loan "loanRefundResponse" transaction adjust amount 90 must return 403 code

  @TestRailId:C2500
  Scenario: As admin I would like to be sure that Edit from Merchant Issued Refund of on loan transaction can not be done
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 November 2022"
    And Admin successfully approves the loan on "1 November 2022" with "1000" amount and expected disbursement date on "1 November 2022"
    When Admin successfully disburse the loan on "1 November 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "15 November 2022"
    And Customer makes "AUTOPAY" repayment on "15 November 2022" with 1000 EUR transaction amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 November 2022" with 200 EUR transaction amount and self-generated Idempotency key
    Then Loan "loanPaymentTransactionResponse" transaction adjust amount 190 must return 403 code

  @TestRailId:C2531
  Scenario: As admin I would like to check the last payment amount after a merchant issue refund
    When Admin sets the business date to "9 February 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "9 February 2023"
    And Admin successfully approves the loan on "9 February 2023" with "1000" amount and expected disbursement date on "9 February 2023"
    When Admin successfully disburse the loan on "9 February 2023" with "1000" EUR transaction amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "9 February 2023" with 50 EUR transaction amount and self-generated Idempotency key
    And Customer makes "AUTOPAY" repayment on "9 February 2023" with 200 EUR transaction amount
    Then Loan has 200 last payment amount

  @TestRailId:C2555
  Scenario: RP01 - Repayment Schedule with interest type: Declining Balance and Interest Recalculation with Interest compounding
    When Admin sets the business date to "1 September 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_1MONTH_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_MONTHLY | 1 September 2022  | 5000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 September 2022" with "5000" amount and expected disbursement date on "1 September 2022"
    When Admin successfully disburse the loan on "1 September 2022" with "5000" EUR transaction amount
    When Admin sets the business date to "4 December 2022"
    And Customer makes "AUTOPAY" repayment on "4 December 2022" with 862 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 September 2022 |                  | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 30   | 01 October 2022   | 04 December 2022 | 4187.32         | 812.68        | 49.32    | 0.0  | 0.0       | 862.0  | 862.0 | 0.0        | 862.0 | 0.0         |
      | 2  | 31   | 01 November 2022  |                  | 3368.0          | 819.32        | 42.68    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 3  | 30   | 01 December 2022  |                  | 2539.22         | 828.78        | 33.22    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 4  | 31   | 01 January 2023   |                  | 1705.65         | 833.57        | 28.43    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 5  | 31   | 01 February 2023  |                  | 861.03          | 844.62        | 17.38    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 6  | 28   | 01 March 2023     |                  | 0.0             | 861.03        | 7.93     | 0.0  | 0.0       | 868.96 | 0.0   | 0.0        | 0.0   | 868.96      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late  | Outstanding |
      | 5000.0        | 178.96   | 0.0  | 0.0       | 5178.96 | 862.0 | 0.0        | 862.0 | 4316.96     |

  @TestRailId:C2556
  Scenario: RP02 - Repayment Schedule with interest type: Declining Balance and Interest Recalculation with on time exact payment
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE | 1 November 2022   | 5000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 November 2022" with "5000" amount and expected disbursement date on "1 November 2023"
    When Admin successfully disburse the loan on "1 November 2022" with "5000" EUR transaction amount
    When Admin sets the business date to "1 December 2022"
    And Customer makes "AUTOPAY" repayment on "1 December 2022" with 863 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 November 2022 |                  | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 30   | 01 December 2022 | 01 December 2022 | 4186.32         | 813.68        | 49.32    | 0.0  | 0.0       | 863.0  | 863.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 January 2023  |                  | 3366.99         | 819.33        | 42.67    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0  | 862.0       |
      | 3  | 31   | 01 February 2023 |                  | 2539.31         | 827.68        | 34.32    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0  | 862.0       |
      | 4  | 28   | 01 March 2023    |                  | 1700.69         | 838.62        | 23.38    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0  | 862.0       |
      | 5  | 31   | 01 April 2023    |                  | 856.02          | 844.67        | 17.33    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0  | 862.0       |
      | 6  | 30   | 01 May 2023      |                  | 0.0             | 856.02        | 8.44     | 0.0  | 0.0       | 864.46 | 0.0   | 0.0        | 0.0  | 864.46      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 5000.0        | 175.46   | 0.0  | 0.0       | 5175.46 | 863.0 | 0.0        | 0.0  | 4312.46     |

  @TestRailId:C2557
  Scenario: RP03 - Repayment Schedule with interest type: Declining Balance and Interest Recalculation with early exact payment
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE | 1 November 2022   | 5000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 November 2022" with "5000" amount and expected disbursement date on "1 November 2023"
    When Admin successfully disburse the loan on "1 November 2022" with "5000" EUR transaction amount
    When Admin sets the business date to "27 November 2022"
    And Customer makes "AUTOPAY" repayment on "27 November 2022" with 863 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 November 2022 |           | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 30   | 01 December 2022 |           | 4137.0          | 863.0         | 48.18    | 0.0  | 0.0       | 911.18 | 863.0 | 863.0      | 0.0  | 48.18       |
      | 2  | 31   | 01 January 2023  |           | 3327.16         | 809.84        | 42.16    | 0.0  | 0.0       | 852.0  | 0.0   | 0.0        | 0.0  | 852.0       |
      | 3  | 31   | 01 February 2023 |           | 2509.07         | 818.09        | 33.91    | 0.0  | 0.0       | 852.0  | 0.0   | 0.0        | 0.0  | 852.0       |
      | 4  | 28   | 01 March 2023    |           | 1680.17         | 828.9         | 23.1     | 0.0  | 0.0       | 852.0  | 0.0   | 0.0        | 0.0  | 852.0       |
      | 5  | 31   | 01 April 2023    |           | 845.29          | 834.88        | 17.12    | 0.0  | 0.0       | 852.0  | 0.0   | 0.0        | 0.0  | 852.0       |
      | 6  | 30   | 01 May 2023      |           | 0.0             | 845.29        | 8.34     | 0.0  | 0.0       | 853.63 | 0.0   | 0.0        | 0.0  | 853.63      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 5000.0        | 172.81   | 0.0  | 0.0       | 5172.81 | 863.0 | 863.0      | 0.0  | 4309.81     |

  @TestRailId:C2558
  Scenario: RP04 - Repayment Schedule with interest type: Declining Balance and Interest Recalculation with late exact payment
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE | 1 November 2022   | 5000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 November 2022" with "5000" amount and expected disbursement date on "1 November 2023"
    When Admin successfully disburse the loan on "1 November 2022" with "5000" EUR transaction amount
    When Admin sets the business date to "5 December 2022"
    And Customer makes "AUTOPAY" repayment on "5 December 2022" with 862 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 November 2022 |                  | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 30   | 01 December 2022 | 05 December 2022 | 4187.32         | 812.68        | 49.32    | 0.0  | 0.0       | 862.0  | 862.0 | 0.0        | 862.0 | 0.0         |
      | 2  | 31   | 01 January 2023  |                  | 3369.07         | 818.25        | 43.75    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 3  | 31   | 01 February 2023 |                  | 2541.41         | 827.66        | 34.34    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 4  | 28   | 01 March 2023    |                  | 1702.8          | 838.61        | 23.39    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 5  | 31   | 01 April 2023    |                  | 858.15          | 844.65        | 17.35    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 6  | 30   | 01 May 2023      |                  | 0.0             | 858.15        | 8.46     | 0.0  | 0.0       | 866.61 | 0.0   | 0.0        | 0.0   | 866.61      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late  | Outstanding |
      | 5000.0        | 176.61   | 0.0  | 0.0       | 5176.61 | 862.0 | 0.0        | 862.0 | 4314.61     |

  @TestRailId:C2559
  Scenario: RP05 - Repayment Schedule with interest type: Declining Balance and Interest Recalculation with on time partial payment
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE | 1 November 2022   | 5000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 November 2022" with "5000" amount and expected disbursement date on "1 November 2023"
    When Admin successfully disburse the loan on "1 November 2022" with "5000" EUR transaction amount
    When Admin sets the business date to "1 December 2022"
    And Customer makes "AUTOPAY" repayment on "1 December 2022" with 200 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 November 2022 |           | 5000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 01 December 2022 |           | 4187.32         | 812.68        | 49.32    | 0.0  | 0.0       | 862.0 | 200.0 | 0.0        | 0.0  | 662.0       |
      | 2  | 31   | 01 January 2023  |           | 3368.0          | 819.32        | 42.68    | 0.0  | 0.0       | 862.0 | 0.0   | 0.0        | 0.0  | 862.0       |
      | 3  | 31   | 01 February 2023 |           | 2540.33         | 827.67        | 34.33    | 0.0  | 0.0       | 862.0 | 0.0   | 0.0        | 0.0  | 862.0       |
      | 4  | 28   | 01 March 2023    |           | 1701.71         | 838.62        | 23.38    | 0.0  | 0.0       | 862.0 | 0.0   | 0.0        | 0.0  | 862.0       |
      | 5  | 31   | 01 April 2023    |           | 857.05          | 844.66        | 17.34    | 0.0  | 0.0       | 862.0 | 0.0   | 0.0        | 0.0  | 862.0       |
      | 6  | 30   | 01 May 2023      |           | 0.0             | 857.05        | 8.45     | 0.0  | 0.0       | 865.5 | 0.0   | 0.0        | 0.0  | 865.5       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 5000.0        | 175.5    | 0.0  | 0.0       | 5175.5 | 200.0 | 0.0        | 0.0  | 4975.50     |

  @TestRailId:C2560
  Scenario: RP06 - Repayment Schedule with interest type: Declining Balance and Interest Recalculation with early partial payment
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE | 1 November 2022   | 5000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 November 2022" with "5000" amount and expected disbursement date on "1 November 2023"
    When Admin successfully disburse the loan on "1 November 2022" with "5000" EUR transaction amount
    When Admin sets the business date to "27 November 2022"
    And Customer makes "AUTOPAY" repayment on "27 November 2022" with 200 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 November 2022 |           | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 30   | 01 December 2022 |           | 4187.05         | 812.95        | 49.05    | 0.0  | 0.0       | 862.0  | 200.0 | 200.0      | 0.0  | 662.0       |
      | 2  | 31   | 01 January 2023  |           | 3367.72         | 819.33        | 42.67    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0  | 862.0       |
      | 3  | 31   | 01 February 2023 |           | 2540.04         | 827.68        | 34.32    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0  | 862.0       |
      | 4  | 28   | 01 March 2023    |           | 1701.42         | 838.62        | 23.38    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0  | 862.0       |
      | 5  | 31   | 01 April 2023    |           | 856.76          | 844.66        | 17.34    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0  | 862.0       |
      | 6  | 30   | 01 May 2023      |           | 0.0             | 856.76        | 8.45     | 0.0  | 0.0       | 865.21 | 0.0   | 0.0        | 0.0  | 865.21      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 5000.0        | 175.21   | 0.0  | 0.0       | 5175.21 | 200.0 | 200.0      | 0.0  | 4975.21     |

  @TestRailId:C2561
  Scenario: RP07 - Repayment Schedule with interest type: Declining Balance and Interest Recalculation with late partial payment
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE | 1 November 2022   | 5000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 November 2022" with "5000" amount and expected disbursement date on "1 November 2023"
    When Admin successfully disburse the loan on "1 November 2022" with "5000" EUR transaction amount
    When Admin sets the business date to "5 December 2022"
    And Customer makes "AUTOPAY" repayment on "5 December 2022" with 200 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 November 2022 |           | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 30   | 01 December 2022 |           | 4187.32         | 812.68        | 49.32    | 0.0  | 0.0       | 862.0  | 200.0 | 0.0        | 200.0 | 662.0       |
      | 2  | 31   | 01 January 2023  |           | 3369.07         | 818.25        | 43.75    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 3  | 31   | 01 February 2023 |           | 2541.41         | 827.66        | 34.34    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 4  | 28   | 01 March 2023    |           | 1702.8          | 838.61        | 23.39    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 5  | 31   | 01 April 2023    |           | 858.15          | 844.65        | 17.35    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 6  | 30   | 01 May 2023      |           | 0.0             | 858.15        | 8.46     | 0.0  | 0.0       | 866.61 | 0.0   | 0.0        | 0.0   | 866.61      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late  | Outstanding |
      | 5000.0        | 176.61   | 0.0  | 0.0       | 5176.61 | 200.0 | 0.0        | 200.0 | 4976.61     |

  @TestRailId:C2562
  Scenario: RP08 - Repayment Schedule with interest type: Declining Balance and Interest Recalculation with on time excess payment
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE | 1 November 2022   | 5000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 November 2022" with "5000" amount and expected disbursement date on "1 November 2023"
    When Admin successfully disburse the loan on "1 November 2022" with "5000" EUR transaction amount
    When Admin sets the business date to "1 December 2022"
    And Customer makes "AUTOPAY" repayment on "1 December 2022" with 1500 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 November 2022 |                  | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 December 2022 | 01 December 2022 | 3549.32         | 1450.68       | 49.32    | 0.0  | 0.0       | 1500.0 | 1500.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 January 2023  |                  | 2854.49         | 694.83        | 36.17    | 0.0  | 0.0       | 731.0  | 0.0    | 0.0        | 0.0  | 731.0       |
      | 3  | 31   | 01 February 2023 |                  | 2152.58         | 701.91        | 29.09    | 0.0  | 0.0       | 731.0  | 0.0    | 0.0        | 0.0  | 731.0       |
      | 4  | 28   | 01 March 2023    |                  | 1441.4          | 711.18        | 19.82    | 0.0  | 0.0       | 731.0  | 0.0    | 0.0        | 0.0  | 731.0       |
      | 5  | 31   | 01 April 2023    |                  | 725.09          | 716.31        | 14.69    | 0.0  | 0.0       | 731.0  | 0.0    | 0.0        | 0.0  | 731.0       |
      | 6  | 30   | 01 May 2023      |                  | 0.0             | 725.09        | 7.15     | 0.0  | 0.0       | 732.24 | 0.0    | 0.0        | 0.0  | 732.24      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 5000.0        | 156.24   | 0.0  | 0.0       | 5156.24 | 1500.0 | 0.0        | 0.0  | 3656.24     |

  @TestRailId:C2563
  Scenario: RP09 - Repayment Schedule with interest type: Declining Balance and Interest Recalculation with early excess payment
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE | 1 November 2022   | 5000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 November 2022" with "5000" amount and expected disbursement date on "1 November 2023"
    When Admin successfully disburse the loan on "1 November 2022" with "5000" EUR transaction amount
    When Admin sets the business date to "27 November 2022"
    And Customer makes "AUTOPAY" repayment on "27 November 2022" with 1500 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      |    |      | 01 November 2022 |           | 5000.0          |               |          | 0.0  |           | 0.0     | 0.0    |            |      |             |
      | 1  | 30   | 01 December 2022 |           | 3500.0          | 1500.0        | 47.34    | 0.0  | 0.0       | 1547.34 | 1500.0 | 1500.0     | 0.0  | 47.34       |
      | 2  | 31   | 01 January 2023  |           | 2814.67         | 685.33        | 35.67    | 0.0  | 0.0       | 721.0   | 0.0    | 0.0        | 0.0  | 721.0       |
      | 3  | 31   | 01 February 2023 |           | 2122.36         | 692.31        | 28.69    | 0.0  | 0.0       | 721.0   | 0.0    | 0.0        | 0.0  | 721.0       |
      | 4  | 28   | 01 March 2023    |           | 1420.9          | 701.46        | 19.54    | 0.0  | 0.0       | 721.0   | 0.0    | 0.0        | 0.0  | 721.0       |
      | 5  | 31   | 01 April 2023    |           | 714.38          | 706.52        | 14.48    | 0.0  | 0.0       | 721.0   | 0.0    | 0.0        | 0.0  | 721.0       |
      | 6  | 30   | 01 May 2023      |           | 0.0             | 714.38        | 7.05     | 0.0  | 0.0       | 721.43  | 0.0    | 0.0        | 0.0  | 721.43      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 5000.0        | 152.77   | 0.0  | 0.0       | 5152.77 | 1500.0 | 1500.0     | 0.0  | 3652.77     |

  @TestRailId:C2564
  Scenario: RP10 - Repayment Schedule with interest type: Declining Balance and Interest Recalculation with late excess payment
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE | 1 November 2022   | 5000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 November 2022" with "5000" amount and expected disbursement date on "1 November 2023"
    When Admin successfully disburse the loan on "1 November 2022" with "5000" EUR transaction amount
    When Admin sets the business date to "5 December 2022"
    And Customer makes "AUTOPAY" repayment on "5 December 2022" with 1500 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 November 2022 |                  | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 30   | 01 December 2022 | 05 December 2022 | 4187.32         | 812.68        | 49.32    | 0.0  | 0.0       | 862.0  | 862.0 | 0.0        | 862.0 | 0.0         |
      | 2  | 31   | 01 January 2023  |                  | 3363.41         | 823.91        | 38.09    | 0.0  | 0.0       | 862.0  | 638.0 | 638.0      | 0.0   | 224.0       |
      | 3  | 31   | 01 February 2023 |                  | 2535.69         | 827.72        | 34.28    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 4  | 28   | 01 March 2023    |                  | 1697.03         | 838.66        | 23.34    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 5  | 31   | 01 April 2023    |                  | 852.33          | 844.7         | 17.3     | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 6  | 30   | 01 May 2023      |                  | 0.0             | 852.33        | 8.41     | 0.0  | 0.0       | 860.74 | 0.0   | 0.0        | 0.0   | 860.74      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late  | Outstanding |
      | 5000.0        | 170.74   | 0.0  | 0.0       | 5170.74 | 1500.0 | 638.0      | 862.0 | 3670.74     |

  @TestRailId:C2625
  Scenario: Verify that the accounting treatment is correct for Goodwill Credit transaction
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 1 January 2023    | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "1 January 2023"
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 100 EUR transaction amount
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "10 January 2023" with 300 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 667.0           | 333.0         | 10.0     | 103.0 | 10.0      | 456.0 | 400.0 | 400.0      | 0.0  | 56.0        |
      | 2  | 28   | 01 March 2023    |           | 334.0           | 333.0         | 10.0     | 0.0   | 0.0       | 343.0 | 0.0   | 0.0        | 0.0  | 343.0       |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 334.0         | 10.0     | 0.0   | 0.0       | 344.0 | 0.0   | 0.0        | 0.0  | 344.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 30       | 103  | 10        | 1143 | 400  | 400        | 0    | 743         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 100.0 |        |
    Then Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "10 January 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 277.0  |
      | ASSET   | 112603       | Interest/Fee Receivable    |       | 23.0   |
      | EXPENSE | 744003       | Goodwill Expense Account   | 277.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 10.0  |        |
      | INCOME  | 404008       | Fee Charge Off             | 13.0  |        |

  @Skip @TestRailId:C2626 @chargeoffOnLoanWithInterest
  Scenario: Verify that the accounting treatment is correct for Goodwill Credit transaction after Charge-off
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 1 January 2023    | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "1 January 2023"
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 100 EUR transaction amount
    And Admin does charge-off the loan on "10 January 2023"
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "10 January 2023" with 300 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0   |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 667.0           | 333.0         | 10.0     | 103.0 | 10.0      | 456.0  | 400.0 | 400.0      | 0.0  | 56.0        |
      | 2  | 28   | 01 March 2023    |           | 334.0           | 333.0         | 10.0     | 0.0   | 0.0       | 343.0  | 0.0   | 0.0        | 0.0  | 343.0       |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 334.0         | 10.0     | 0.0   | 0.0       | 344.0  | 0.0   | 0.0        | 0.0  | 344.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 30       | 103  | 10        | 1143 | 400  | 400        | 0    | 743         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 100.0 |        |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "10 January 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 43.0   |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 1000.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 30.0   |        |
      | INCOME  | 404008       | Fee Charge Off             | 13.0   |        |
    Then Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "10 January 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | INCOME  | 744008       | Recoveries                 |       | 300.0  |
      | EXPENSE | 744003       | Goodwill Expense Account   | 277.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 10.0  |        |
      | INCOME  | 404008       | Fee Charge Off             | 13.0  |        |

  @TestRailId:C2627
  Scenario: Verify that the accounting treatment is correct for Goodwill Credit transaction when undo happened
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 1 January 2023    | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "1 January 2023"
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 100 EUR transaction amount
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "10 January 2023" with 300 EUR transaction amount and system-generated Idempotency key
    When Customer undo "2"th transaction made on "10 January 2023"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 667.0           | 333.0         | 10.0     | 103.0 | 10.0      | 456.0 | 100.0 | 100.0      | 0.0  | 356.0       |
      | 2  | 28   | 01 March 2023    |           | 334.0           | 333.0         | 10.0     | 0.0   | 0.0       | 343.0 | 0.0   | 0.0        | 0.0  | 343.0       |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 334.0         | 10.0     | 0.0   | 0.0       | 344.0 | 0.0   | 0.0        | 0.0  | 344.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 30       | 103  | 10        | 1143 | 100  | 100        | 0    | 1043        |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 100.0 |        |
    Then Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "10 January 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 277.0  |
      | ASSET   | 112603       | Interest/Fee Receivable    |       | 23.0   |
      | EXPENSE | 744003       | Goodwill Expense Account   | 277.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 10.0  |        |
      | INCOME  | 404008       | Fee Charge Off             | 13.0  |        |
      | ASSET   | 112601       | Loans Receivable           | 277.0 |        |
      | ASSET   | 112603       | Interest/Fee Receivable    | 23.0  |        |
      | EXPENSE | 744003       | Goodwill Expense Account   |       | 277.0  |
      | INCOME  | 404001       | Interest Income Charge Off |       | 10.0   |
      | INCOME  | 404008       | Fee Charge Off             |       | 13.0   |

  @Skip @TestRailId:C2628 @chargeoffOnLoanWithInterest
  Scenario: Verify that the accounting treatment is correct for Goodwill Credit transaction when the loan was Charged-off and undo happened for Goodwill
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 1 January 2023    | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "1 January 2023"
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 100 EUR transaction amount
    And Admin does charge-off the loan on "10 January 2023"
    When Admin sets the business date to "11 January 2023"
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "11 January 2023" with 300 EUR transaction amount and system-generated Idempotency key
    When Customer undo "1"th transaction made on "11 January 2023"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0   |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 666.67          | 333.33        | 10.0     | 103.0 | 10.0      | 456.33 | 100.0 | 100.0      | 0.0  | 356.33      |
      | 2  | 28   | 01 March 2023    |           | 333.34          | 333.33        | 10.0     | 0.0   | 0.0       | 343.33 | 0.0   | 0.0        | 0.0  | 343.33      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 333.34        | 10.0     | 0.0   | 0.0       | 343.34 | 0.0   | 0.0        | 0.0  | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 30       | 103  | 10        | 1143 | 100  | 100        | 0    | 1043        |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 100.0 |        |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "10 January 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 43.0   |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 1000.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 30.0   |        |
      | INCOME  | 404008       | Fee Charge Off             | 13.0   |        |
    Then Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "11 January 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | INCOME  | 744008       | Recoveries                 |       | 300.0  |
      | EXPENSE | 744003       | Goodwill Expense Account   | 277.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 10.0  |        |
      | INCOME  | 404008       | Fee Charge Off             | 13.0  |        |
      | INCOME  | 744008       | Recoveries                 | 300.0 |        |
      | EXPENSE | 744003       | Goodwill Expense Account   |       | 277.0  |
      | INCOME  | 404001       | Interest Income Charge Off |       | 10.0   |
      | INCOME  | 404008       | Fee Charge Off             |       | 13.0   |

  @TestRailId:C2629
  Scenario: RP11 - Repayment Schedule with interest type: Declining Balance - Prepayment - Reduce number of installments
    When Admin sets the business date to "01 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_RESCHEDULE_REDUCE_NR_INST | 01 November 2022  | 5000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 November 2022" with "5000" amount and expected disbursement date on "01 November 2023"
    When Admin successfully disburse the loan on "01 November 2022" with "5000" EUR transaction amount
    When Admin sets the business date to "01 December 2022"
    And Customer makes "AUTOPAY" repayment on "01 December 2022" with 1000 EUR transaction amount
    When Admin sets the business date to "01 January 2023"
    And Customer makes "AUTOPAY" repayment on "01 January 2023" with 2000 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 November 2022 |                  | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 December 2022 | 01 December 2022 | 4049.32         | 950.68        | 49.32    | 0.0  | 0.0       | 1000.0 | 1000.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 January 2023  | 01 January 2023  | 2090.59         | 1958.73       | 41.27    | 0.0  | 0.0       | 2000.0 | 2000.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2023 |                  | 1249.9          | 840.69        | 21.31    | 0.0  | 0.0       | 862.0  | 0.0    | 0.0        | 0.0  | 862.0       |
      | 4  | 28   | 01 March 2023    |                  | 399.41          | 850.49        | 11.51    | 0.0  | 0.0       | 862.0  | 0.0    | 0.0        | 0.0  | 862.0       |
      | 5  | 31   | 01 April 2023    |                  | 0.0             | 399.41        | 4.07     | 0.0  | 0.0       | 403.48 | 0.0    | 0.0        | 0.0  | 403.48      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 5000.0        | 127.48   | 0.0  | 0.0       | 5127.48 | 3000.0 | 0.0        | 0.0  | 2127.48     |

  @TestRailId:C2630
  Scenario: RP12 - Repayment Schedule with interest type: Declining Balance - Prepayment - Reduce Installment amount
    When Admin sets the business date to "01 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE | 01 November 2022  | 5000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 November 2022" with "5000" amount and expected disbursement date on "01 November 2023"
    When Admin successfully disburse the loan on "01 November 2022" with "5000" EUR transaction amount
    When Admin sets the business date to "01 December 2022"
    And Customer makes "AUTOPAY" repayment on "01 December 2022" with 1000 EUR transaction amount
    When Admin sets the business date to "01 January 2023"
    And Customer makes "AUTOPAY" repayment on "01 January 2023" with 2000 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 November 2022 |                  | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 December 2022 | 01 December 2022 | 4049.32         | 950.68        | 49.32    | 0.0  | 0.0       | 1000.0 | 1000.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 January 2023  | 01 January 2023  | 2090.59         | 1958.73       | 41.27    | 0.0  | 0.0       | 2000.0 | 2000.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2023 |                  | 1575.9          | 514.69        | 21.31    | 0.0  | 0.0       | 536.0  | 0.0    | 0.0        | 0.0  | 536.0       |
      | 4  | 28   | 01 March 2023    |                  | 1054.41         | 521.49        | 14.51    | 0.0  | 0.0       | 536.0  | 0.0    | 0.0        | 0.0  | 536.0       |
      | 5  | 31   | 01 April 2023    |                  | 529.16          | 525.25        | 10.75    | 0.0  | 0.0       | 536.0  | 0.0    | 0.0        | 0.0  | 536.0       |
      | 6  | 30   | 01 May 2023      |                  | 0.0             | 529.16        | 5.22     | 0.0  | 0.0       | 534.38 | 0.0    | 0.0        | 0.0  | 534.38      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 5000.0        | 142.38   | 0.0  | 0.0       | 5142.38 | 3000.0 | 0.0        | 0.0  | 2142.38     |

  @TestRailId:C2631
  Scenario: RP13 - Repayment Schedule with interest type: Declining Balance - Prepayment - Reschedule next repayments
    When Admin sets the business date to "01 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_RESCHEDULE_RESCH_NEXT_REP | 01 November 2022  | 5000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 November 2022" with "5000" amount and expected disbursement date on "01 November 2023"
    When Admin successfully disburse the loan on "01 November 2022" with "5000" EUR transaction amount
    When Admin sets the business date to "01 December 2022"
    And Customer makes "AUTOPAY" repayment on "01 December 2022" with 1000 EUR transaction amount
    When Admin sets the business date to "01 January 2023"
    And Customer makes "AUTOPAY" repayment on "01 January 2023" with 2000 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 November 2022 |                  | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 December 2022 | 01 December 2022 | 4049.32         | 950.68        | 49.32    | 0.0  | 0.0       | 1000.0 | 1000.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 January 2023  | 01 January 2023  | 2090.59         | 1958.73       | 41.27    | 0.0  | 0.0       | 2000.0 | 2000.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2023 |                  | 2090.59         | 0.0           | 21.31    | 0.0  | 0.0       | 21.31  | 0.0    | 0.0        | 0.0  | 21.31       |
      | 4  | 28   | 01 March 2023    |                  | 1683.14         | 407.45        | 19.24    | 0.0  | 0.0       | 426.69 | 0.0    | 0.0        | 0.0  | 426.69      |
      | 5  | 31   | 01 April 2023    |                  | 838.29          | 844.85        | 17.15    | 0.0  | 0.0       | 862.0  | 0.0    | 0.0        | 0.0  | 862.0       |
      | 6  | 30   | 01 May 2023      |                  | 0.0             | 838.29        | 8.27     | 0.0  | 0.0       | 846.56 | 0.0    | 0.0        | 0.0  | 846.56      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 5000.0        | 156.56   | 0.0  | 0.0       | 5156.56 | 3000.0 | 0.0        | 0.0  | 2156.56     |

  @TestRailId:C2632
  Scenario: RP14 - Repayment Schedule with interest type: Declining Balance - Interest Recalculation Frequency: Same as Repayment Period - Partial payment
    When Admin sets the business date to "01 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE | 01 November 2022  | 5000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 November 2022" with "5000" amount and expected disbursement date on "01 November 2023"
    When Admin successfully disburse the loan on "01 November 2022" with "5000" EUR transaction amount
    When Admin sets the business date to "20 November 2022"
    And Customer makes "AUTOPAY" repayment on "20 November 2022" with 200 EUR transaction amount
    When Admin sets the business date to "04 January 2023"
    And Customer makes "AUTOPAY" repayment on "04 January 2023" with 200 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 November 2022 |           | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 30   | 01 December 2022 |           | 4187.32         | 812.68        | 49.32    | 0.0  | 0.0       | 862.0  | 400.0 | 200.0      | 200.0 | 462.0       |
      | 2  | 31   | 01 January 2023  |           | 3368.0          | 819.32        | 42.68    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 3  | 31   | 01 February 2023 |           | 2555.42         | 812.58        | 49.42    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 4  | 28   | 01 March 2023    |           | 1716.94         | 838.48        | 23.52    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 5  | 31   | 01 April 2023    |           | 872.44          | 844.5         | 17.5     | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 6  | 30   | 01 May 2023      |           | 0.0             | 872.44        | 8.6      | 0.0  | 0.0       | 881.04 | 0.0   | 0.0        | 0.0   | 881.04      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late  | Outstanding |
      | 5000.0        | 191.04   | 0.0  | 0.0       | 5191.04 | 400.0 | 200.0      | 200.0 | 4791.04     |

  @TestRailId:C2633
  Scenario: RP15 - Repayment Schedule with interest type: Declining Balance - Interest Recalculation Frequency: Same as Repayment Period - Late payment
    When Admin sets the business date to "01 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE | 01 November 2022  | 5000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 November 2022" with "5000" amount and expected disbursement date on "01 November 2023"
    When Admin successfully disburse the loan on "01 November 2022" with "5000" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 862 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 November 2022 |                  | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 30   | 01 December 2022 | 01 February 2023 | 4187.32         | 812.68        | 49.32    | 0.0  | 0.0       | 862.0  | 862.0 | 0.0        | 862.0 | 0.0         |
      | 2  | 31   | 01 January 2023  |                  | 3368.0          | 819.32        | 42.68    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 3  | 31   | 01 February 2023 |                  | 2556.96         | 811.04        | 50.96    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 4  | 28   | 01 March 2023    |                  | 1718.5          | 838.46        | 23.54    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 5  | 31   | 01 April 2023    |                  | 874.01          | 844.49        | 17.51    | 0.0  | 0.0       | 862.0  | 0.0   | 0.0        | 0.0   | 862.0       |
      | 6  | 30   | 01 May 2023      |                  | 0.0             | 874.01        | 8.62     | 0.0  | 0.0       | 882.63 | 0.0   | 0.0        | 0.0   | 882.63      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late  | Outstanding |
      | 5000.0        | 192.63   | 0.0  | 0.0       | 5192.63 | 862.0 | 0.0        | 862.0 | 4330.63     |

  @TestRailId:C2634
  Scenario: RP16 - Repayment Schedule with interest type: Declining Balance - Interest Recalculation Frequency: Same as Repayment Period - Multi-disbursement
    When Admin sets the business date to "01 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_SAR_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE_MULTIDISB | 01 November 2022  | 10000          | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 November 2022" with "10000" amount and expected disbursement date on "01 November 2023"
    When Admin successfully disburse the loan on "01 November 2022" with "5000" EUR transaction amount
    When Admin sets the business date to "01 December 2022"
    And Customer makes "AUTOPAY" repayment on "01 December 2022" with 1725 EUR transaction amount
    When Admin sets the business date to "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2023" with 1725 EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 1725 EUR transaction amount
    When Admin sets the business date to "01 March 2023"
    When Admin successfully disburse the loan on "01 March 2023" with "2000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2023" with 1725 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      |    |      | 01 November 2022 |                  | 5000.0          |               |          | 0.0  |           | 0.0     | 0.0    |            |      |             |
      | 1  | 30   | 01 December 2022 | 01 December 2022 | 3325.0          | 1675.0        | 50.0     | 0.0  | 0.0       | 1725.0  | 1725.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 January 2023  |                  | 3000.0          |               |          | 0.0  |           | 0.0     | 0.0    |            |      |             |
      | 2  | 31   | 01 January 2023  | 01 January 2023  | 4633.25         | 1691.75       | 33.25    | 0.0  | 0.0       | 1725.0  | 1725.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2023 | 01 February 2023 | 2954.58         | 1678.67       | 46.33    | 0.0  | 0.0       | 1725.0  | 1725.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 March 2023    |                  | 2000.0          |               |          | 0.0  |           | 0.0     | 0.0    |            |      |             |
      | 4  | 28   | 01 March 2023    | 01 March 2023    | 3259.13         | 1695.45       | 29.55    | 0.0  | 0.0       | 1725.0  | 1725.0 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 April 2023    |                  | 1566.72         | 1692.41       | 32.59    | 0.0  | 0.0       | 1725.0  | 0.0    | 0.0        | 0.0  | 1725.0      |
      | 6  | 30   | 01 May 2023      |                  | 0.0             | 1566.72       | 15.67    | 0.0  | 0.0       | 1582.39 | 0.0    | 0.0        | 0.0  | 1582.39     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid   | In advance | Late | Outstanding |
      | 10000.0       | 207.39   | 0.0  | 0.0       | 10207.39 | 6900.0 | 0.0        | 0.0  | 3307.39     |


  @TestRailId:C2636 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - adding charge due in the future
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 1 January 2023    | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 500 EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 20 January 2023 | Flat             | 50.0 | 0.0  | 0.0    | 50.0        |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 50.0      | 1050.0 | 500.0 | 500.0      | 0.0  | 550.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 50        | 1050 | 500  | 500        | 0    | 550         |

  @TestRailId:C2637 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - adding charge due in the future, then repayments before and after charge due date
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 1 January 2023    | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 500 EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Admin sets the business date to "17 January 2023"
    And Customer makes "AUTOPAY" repayment on "17 January 2023" with 450 EUR transaction amount
    When Admin sets the business date to "21 January 2023"
    And Customer makes "AUTOPAY" repayment on "21 January 2023" with 50 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 17 January 2023  | Repayment        | 450.0  | 450.0     | 0.0      | 0.0  | 0.0       | 50.0         |
      | 21 January 2023  | Repayment        | 50.0   | 0.0       | 0.0      | 0.0  | 50.0      | 50.0         |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 20 January 2023 | Flat             | 50.0 | 50.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 50.0      | 1050.0 | 1000.0 | 1000.0     | 0.0  | 50.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 50        | 1050 | 1000 | 1000       | 0    | 50          |

  @TestRailId:C2638 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - adding charge due in the future, then repayment before due date, new charge with due date in future and repayment on first charge due date
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 1 January 2023    | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 500 EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Admin sets the business date to "17 January 2023"
    And Customer makes "AUTOPAY" repayment on "17 January 2023" with 100 EUR transaction amount
    When Admin sets the business date to "19 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "23 January 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "20 January 2023"
    And Customer makes "AUTOPAY" repayment on "20 January 2023" with 100 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 17 January 2023  | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 400.0        |
      | 20 January 2023  | Repayment        | 100.0  | 50.0      | 0.0      | 0.0  | 50.0      | 350.0        |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 20 January 2023 | Flat             | 50.0 | 50.0 | 0.0    | 0.0         |
      | NSF fee | true      | Specified due date | 23 January 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 60.0      | 1060.0 | 700.0 | 700.0      | 0.0  | 360.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 60        | 1060 | 700  | 700        | 0    | 360         |

  @Skip @TestRailId:C2639 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - adding charge due in the future, then repayment before due date with full amount
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 1 January 2023    | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 500 EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Admin sets the business date to "17 January 2023"
    And Customer makes "AUTOPAY" repayment on "17 January 2023" with 550 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 17 January 2023  | Repayment        | 550.0  | 500.0     | 0.0      | 0.0  | 50.0      | 0.0          |
      | 17 January 2023  | Accrual          | 50.0   | 0.0       | 0.0      | 0.0  | 50.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 20 January 2023 | Flat             | 50.0 | 50.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 17 January 2023 | 0.0             | 1000.0        | 0.0      | 0.0  | 50.0      | 1050.0 | 1050.0 | 1000.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 50        | 1050 | 1050 | 1000       | 0    | 0           |

  @TestRailId:C2655 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 3000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 1000 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 01 February 2023 | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 2000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                  | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0    | 0.0        | 0.0  | 1000.0      |
      | 3  | 31   | 01 April 2023    |                  | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0    | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 0    | 0         | 3000 | 1000 | 0          | 0    | 2000        |

  @TestRailId:C2656 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - inAdvance principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 3000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 1000 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 10 January 2023  | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 10 January 2023 | 2000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 1000.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0    | 0.0        | 0.0  | 1000.0      |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0    | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 0    | 0         | 3000 | 1000 | 1000       | 0    | 2000        |

  @TestRailId:C2657 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due + inAdvance principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 3000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 1500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 01 February 2023 | Repayment        | 1500.0 | 1500.0    | 0.0      | 0.0  | 0.0       | 1500.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 2000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                  | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 500.0  | 500.0      | 0.0  | 500.0       |
      | 3  | 31   | 01 April 2023    |                  | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0    | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 0    | 0         | 3000 | 1500 | 500        | 0    | 1500        |

  @TestRailId:C2658 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due penalty + due principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 3000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 February 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 01 February 2023 | Repayment        | 500.0  | 450.0     | 0.0      | 0.0  | 50.0      | 2550.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 0.0      | 0.0  | 50.0      | 1050.0 | 500.0 | 0.0        | 0.0  | 550.0       |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 0    | 50        | 3050 | 500  | 0          | 0    | 2550        |

  @TestRailId:C2659 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due fee + due principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 3000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 01 February 2023 | Repayment        | 500.0  | 450.0     | 0.0      | 50.0 | 0.0       | 2550.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 0.0      | 50.0 | 0.0       | 1050.0 | 500.0 | 0.0        | 0.0  | 550.0       |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 50   | 0         | 3050 | 500  | 0          | 0    | 2550        |

  @TestRailId:C2660 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due interest + due principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT | 01 January 2023   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 01 February 2023 | Repayment        | 500.0  | 470.0     | 30.0     | 0.0  | 0.0       | 2530.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 500.0 | 0.0        | 0.0  | 530.0       |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0   | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0   | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 0    | 0         | 3090 | 500  | 0          | 0    | 2590        |

  @TestRailId:C2661 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due penalty + due fee + due principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 3000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 February 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 01 February 2023 | Repayment        | 500.0  | 400.0     | 0.0      | 50.0 | 50.0      | 2600.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 0.0      | 50.0 | 50.0      | 1100.0 | 500.0 | 0.0        | 0.0  | 600.0       |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 50   | 50        | 3100 | 500  | 0          | 0    | 2600        |

  @TestRailId:C2662 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due penalty + inAdvance principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 3000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Repayment        | 500.0  | 450.0     | 0.0      | 0.0  | 50.0      | 2550.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 0.0      | 0.0  | 50.0      | 1050.0 | 500.0 | 500.0      | 0.0  | 550.0       |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 0    | 50        | 3050 | 500  | 500        | 0    | 2550        |

  @TestRailId:C2663 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due penalty + inAdvance principal + inAdvance penalty not effective because of partial payment
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 3000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Repayment        | 500.0  | 450.0     | 0.0      | 0.0  | 50.0      | 2550.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 0.0      | 0.0  | 100.0     | 1100.0 | 500.0 | 500.0      | 0.0  | 600.0       |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 0    | 100       | 3100 | 500  | 500        | 0    | 2600        |

  @TestRailId:C2664 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due penalty + inAdvance principal + inAdvance penalty
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 3000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 1100 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Repayment        | 1100.0 | 1000.0    | 0.0      | 0.0  | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 2000.0          | 1000.0        | 0.0      | 0.0  | 100.0     | 1100.0 | 1100.0 | 1100.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0    | 0.0        | 0.0  | 1000.0      |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0    | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 0    | 100       | 3100 | 1100 | 1100       | 0    | 2000        |

  @TestRailId:C2665 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due penalty + inAdvance principal + inAdvance penalty + inAdvance fee
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 3000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 1150 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Repayment        | 1150.0 | 1000.0    | 0.0      | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 2000.0          | 1000.0        | 0.0      | 50.0 | 100.0     | 1150.0 | 1150.0 | 1150.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0    | 0.0        | 0.0  | 1000.0      |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0    | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 50   | 100       | 3150 | 1150 | 1150       | 0    | 2000        |

  @TestRailId:C2666 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - due penalty + inAdvance principal + inAdvance penalty + inAdvance fee + inAdvance interest
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT | 01 January 2023   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 1180 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Repayment        | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 1180.0 | 1180.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 1180 | 1180       | 0    | 2060        |


  @TestRailId:C2667 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - repayment + reversal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT | 01 January 2023   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 1180 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Repayment        | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 1180.0 | 1180.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 1180 | 1180       | 0    | 2060        |
    When Customer undo "1"th transaction made on "15 January 2023"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Repayment        | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 0.0  | 0.0        | 0.0  | 1180.0      |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 0    | 0          | 0    | 3240        |

  @TestRailId:C2668 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - merchant issued refund + reversal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT | 01 January 2023   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2023" with 1180 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement           | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Merchant Issued Refund | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 1180.0 | 1180.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 1180 | 1180       | 0    | 2060        |
    When Customer undo "1"th transaction made on "15 January 2023"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement           | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Merchant Issued Refund | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 0.0  | 0.0        | 0.0  | 1180.0      |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 0    | 0          | 0    | 3240        |

  @TestRailId:C2669 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - payout refund + reversal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT | 01 January 2023   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "15 January 2023" with 1180 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Payout Refund    | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 1180.0 | 1180.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 1180 | 1180       | 0    | 2060        |
    When Customer undo "1"th transaction made on "15 January 2023"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Payout Refund    | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 0.0  | 0.0        | 0.0  | 1180.0      |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 0    | 0          | 0    | 3240        |

  @TestRailId:C2670 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - goodwill credit transaction + reversal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT | 01 January 2023   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "15 January 2023" with 1180 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Goodwill Credit  | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 1180.0 | 1180.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 1180 | 1180       | 0    | 2060        |
    When Customer undo "1"th transaction made on "15 January 2023"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Goodwill Credit  | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 0.0  | 0.0        | 0.0  | 1180.0      |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 0    | 0          | 0    | 3240        |

  @TestRailId:C2671 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - repayment + charge adjustment + charge adjustment reversal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT | 01 January 2023   | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 January 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 1180 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Repayment        | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 1180.0 | 1180.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 1180 | 1180       | 0    | 2060        |
    When Admin sets the business date to "27 January 2023"
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "20 January 2023" with 50 EUR transaction amount and externalId ""
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement      | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 15 January 2023  | Repayment         | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       |
      | 27 January 2023  | Charge Adjustment | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1950.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 1180.0 | 1180.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 50.0   | 50.0       | 0.0  | 980.0       |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 1230 | 1230       | 0    | 2010        |
    When Admin sets the business date to "30 January 2023"
    When Admin reverts the charge adjustment which was raised on "27 January 2023" with 50 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2023  | Disbursement      | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       | false    |
      | 15 January 2023  | Repayment         | 1180.0 | 1000.0    | 30.0     | 50.0 | 100.0     | 2000.0       | false    |
      | 27 January 2023  | Charge Adjustment | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1950.0       | true     |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 15 January 2023 | 2000.0          | 1000.0        | 30.0     | 50.0 | 100.0     | 1180.0 | 1180.0 | 1180.0     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    |                 | 1000.0          | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
      | 3  | 31   | 01 April 2023    |                 | 0.0             | 1000.0        | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0    | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 50   | 100       | 3240 | 1180 | 1180       | 0    | 2060        |


  @TestRailId:C2682 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy - fee - repayment - nsffee - chargeback - repayment
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 January 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "15 January 2023"
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 500 EUR transaction amount
    When Admin sets the business date to "18 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "18 January 2023" due date and 25 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 500 EUR transaction amount for Payment nr. 1
    When Admin sets the business date to "21 January 2023"
    And Customer makes "AUTOPAY" repayment on "21 January 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 January 2023  | Repayment        | 500.0  | 490.0     | 0.0      | 10.0 | 0.0       | 510.0        |
      | 18 January 2023  | Chargeback       | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 1010.0       |
      | 21 January 2023  | Repayment        | 500.0  | 475.0     | 0.0      | 0.0  | 25.0      | 535.0        |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1500.0        | 0.0      | 10.0 | 25.0      | 1535.0 | 1000.0 | 1000.0     | 0.0  | 535.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1500          | 0        | 10   | 25        | 1535 | 1000 | 1000       | 0    | 535         |

  @Skip @TestRailId:C2799 @PaymentStrategyDueInAdvance
  Scenario: Verify the due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment strategy: Same day transaction
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE | 1 January 2023    | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "04 January 2023"
    And Customer makes "AUTOPAY" repayment on "04 January 2023" with 200 EUR transaction amount
    When Admin sets the business date to "12 January 2023"
    And Customer makes "AUTOPAY" repayment on "12 January 2023" with 300 EUR transaction amount
    And Admin adds a 1 % Processing charge to the loan with "en" locale on date: "12 January 2023"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 04 January 2023  | Repayment        | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 800.0        |
      | 12 January 2023  | Repayment        | 300.0  | 290.0     | 0.0      | 10.0 | 0.0       | 510.0        |
    And Customer makes "AUTOPAY" repayment on "12 January 2023" with 510 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 04 January 2023  | Repayment        | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 800.0        |
      | 12 January 2023  | Repayment        | 300.0  | 290.0     | 0.0      | 10.0 | 0.0       | 510.0        |
      | 12 January 2023  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 12 January 2023  | Repayment        | 510.0  | 510.0     | 0.0      | 0.0  | 0.0       | 0.0          |

  @TestRailId:C2694 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC1 - no fees or penalties, due payment
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 1000 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 February 2023 | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 1000 | 0          | 0    | 0           |

  @TestRailId:C2695 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC2 - due principal, fee
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "25 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 1020 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 February 2023 | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 01 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 1020.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 1020 | 0          | 0    | 0           |

  @TestRailId:C2696 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC3 - in advance principal, reverted, due penalty, principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount

    When Admin sets the business date to "25 January 2023"
    And Customer makes "AUTOPAY" repayment on "25 January 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |

    When Admin sets the business date to "28 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "25 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "28 January 2023" due date and 20 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then On Loan Transactions tab the "Repayment" Transaction with date "25 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 28 January 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 1020 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "25 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
      | 01 February 2023 | Repayment        | 1020.0 | 1000.0    | 0.0      | 0.0  | 20.0      | 0.0          |
      | 01 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 28 January 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 0.0             | 1000.0        | 0.0      | 0.0  | 20.0      | 1020.0 | 1020.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 20        | 1020 | 1020 | 0          | 0    | 0           |

  @TestRailId:C2697 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC4 - in advance principal, fee, reverted, due penalty, principal, fee
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount

    When Admin sets the business date to "10 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount

    When Admin sets the business date to "25 January 2023"
    And Customer makes "AUTOPAY" repayment on "25 January 2023" with 1020 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 25 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 25 January 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 1020.0 | 1020.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 1020 | 1020       | 0    | 0           |

    When Admin sets the business date to "28 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "25 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "25 January 2023" is reverted
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 25 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 1040 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "25 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 25 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 01 February 2023 | Repayment        | 1040.0 | 1000.0    | 0.0      | 20.0 | 20.0      | 0.0          |
      | 01 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 20.0      | 1040.0 | 1040.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 20        | 1040 | 1040 | 0          | 0    | 0           |

  @TestRailId:C2698 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC5 - in advance principal, fee, reverted, multiple due penalty, principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount

    When Admin sets the business date to "10 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount

    When Admin sets the business date to "25 January 2023"
    And Customer makes "AUTOPAY" repayment on "25 January 2023" with 1020 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 25 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 25 January 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 1020.0 | 1020.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 1020 | 1020       | 0    | 0           |

    When Admin sets the business date to "28 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "25 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "25 January 2023" is reverted
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 25 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 1040 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "25 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 25 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 01 February 2023 | Repayment        | 1040.0 | 1000.0    | 0.0      | 20.0 | 20.0      | 0.0          |
      | 01 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 20.0      | 1040.0 | 1040.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 20        | 1040 | 1040 | 0          | 0    | 0           |

    When Admin sets the business date to "05 February 2023"
    When Customer undo "1"th "Repayment" transaction made on "01 February 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "08 February 2023" due date and 20 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "25 January 2023" is reverted
    Then On Loan Transactions tab the "Repayment" Transaction with date "01 February 2023" is reverted
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 25 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 01 February 2023 | Repayment        | 1040.0 | 1000.0    | 0.0      | 20.0 | 20.0      | 0.0          |
      | 01 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 08 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

    When Admin sets the business date to "08 February 2023"
    And Customer makes "AUTOPAY" repayment on "08 February 2023" with 1060 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "01 February 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 25 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 01 February 2023 | Repayment        | 1040.0 | 1000.0    | 0.0      | 20.0 | 20.0      | 0.0          |
      | 01 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
      | 08 February 2023 | Repayment        | 1060.0 | 1000.0    | 0.0      | 20.0 | 40.0      | 0.0          |
      | 08 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 08 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2023 | 08 February 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 20.0      | 1040.0 | 1040.0 | 0.0        | 1040.0 | 0.0         |
      | 2  | 7    | 08 February 2023 | 08 February 2023 | 0.0             | 0.0           | 0.0      | 0.0  | 20.0      | 20.0   | 20.0   | 0.0        | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 40        | 1060 | 1060 | 0          | 1040 | 0           |


  @TestRailId:C2699 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC6 - partial payment
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "25 January 2023"
    And Customer makes "AUTOPAY" repayment on "25 January 2023" with 500 EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 February 2023 | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 500.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 1000 | 500        | 0    | 0           |

  @TestRailId:C2700 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC7 - partial payment, in advance principal, due principal, fee
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount

    When Admin sets the business date to "10 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount

    When Admin sets the business date to "25 January 2023"
    And Customer makes "AUTOPAY" repayment on "25 January 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 500.0 | 500.0      | 0.0  | 520.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 500  | 500        | 0    | 520         |

    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 520 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 25 January 2023  | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 February 2023 | Repayment        | 520.0  | 500.0     | 0.0      | 20.0 | 0.0       | 0.0          |
      | 01 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 1020.0 | 500.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 1020 | 500        | 0    | 0           |

  @TestRailId:C2701 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC8 - partial payment, in advance principal, due penalty, principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount

    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"

    When Admin sets the business date to "25 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "10 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

    When Admin sets the business date to "28 January 2023"
    And Customer makes "AUTOPAY" repayment on "28 January 2023" with 520 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
      | 28 January 2023  | Repayment        | 520.0  | 500.0     | 0.0      | 0.0  | 20.0      | 500.0        |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 20.0      | 1020.0 | 520.0 | 520.0      | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 20        | 1020 | 520  | 520        | 0    | 500         |

    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
      | 28 January 2023  | Repayment        | 520.0  | 500.0     | 0.0      | 0.0  | 20.0      | 500.0        |
      | 01 February 2023 | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 01 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 0.0             | 1000.0        | 0.0      | 0.0  | 20.0      | 1020.0 | 1020.0 | 520.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 20        | 1020 | 1020 | 520        | 0    | 0           |

  @TestRailId:C2702 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC9 - partial payment, in advance principal, fee, due penalty, principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount

    When Admin sets the business date to "10 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount

    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 1020 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 10 January 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 1020.0 | 1020.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 1020 | 1020       | 0    | 0           |

    When Admin sets the business date to "25 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "10 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

    When Admin sets the business date to "28 January 2023"
    And Customer makes "AUTOPAY" repayment on "28 January 2023" with 520 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 28 January 2023  | Repayment        | 520.0  | 500.0     | 0.0      | 0.0  | 20.0      | 500.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 0.0      | 20.0 | 20.0      | 1040.0 | 520.0 | 520.0      | 0.0  | 520.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 20        | 1040 | 520  | 520        | 0    | 520         |

    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 500 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 28 January 2023  | Repayment        | 520.0  | 500.0     | 0.0      | 0.0  | 20.0      | 500.0        |
      | 01 February 2023 | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 0.0      | 20.0 | 20.0      | 1040.0 | 1020.0 | 520.0      | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 20        | 1040 | 1020 | 520        | 0    | 20          |

    When Admin sets the business date to "05 February 2023"
    And Customer makes "AUTOPAY" repayment on "05 February 2023" with 20 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 28 January 2023  | Repayment        | 520.0  | 500.0     | 0.0      | 0.0  | 20.0      | 500.0        |
      | 01 February 2023 | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 05 February 2023 | Repayment        | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 05 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 05 February 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 20.0      | 1040.0 | 1040.0 | 520.0      | 20.0 | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 20        | 1040 | 1040 | 520        | 20   | 0           |

  @TestRailId:C2703 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC10 - partial payment, in advance principal, fee, due penalty
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount

    When Admin sets the business date to "10 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount

    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 1020 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 10 January 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 1020.0 | 1020.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 1020 | 1020       | 0    | 0           |

    When Admin sets the business date to "25 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "10 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "28 January 2023" due date and 20 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 28 January 2023  | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

    When Admin sets the business date to "28 January 2023"
    And Customer makes "AUTOPAY" repayment on "28 January 2023" with 520 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 28 January 2023  | Repayment        | 520.0  | 500.0     | 0.0      | 0.0  | 20.0      | 500.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | NSF fee    | true      | Specified due date | 28 January 2023  | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 0.0      | 20.0 | 20.0      | 1040.0 | 520.0 | 520.0      | 0.0  | 520.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 20        | 1040 | 520  | 520        | 0    | 520         |

    When Admin sets the business date to "30 January 2023"
    And Customer makes "AUTOPAY" repayment on "30 January 2023" with 520 EUR transaction amount
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 28 January 2023  | Repayment        | 520.0  | 500.0     | 0.0      | 0.0  | 20.0      | 500.0        |
      | 30 January 2023  | Repayment        | 520.0  | 500.0     | 0.0      | 20.0 | 0.0       | 0.0          |
      | 30 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | NSF fee    | true      | Specified due date | 28 January 2023  | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 30 January 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 20.0      | 1040.0 | 1040.0 | 1040.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 20        | 1040 | 1040 | 1040       | 0    | 0           |

  @TestRailId:C2704 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC11 - partial payment, in advance principal, fee, due penalty, principal, fee
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount

    When Admin sets the business date to "05 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount

    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 1020 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 10 January 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 1020.0 | 1020.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 1020 | 1020       | 0    | 0           |

    When Admin sets the business date to "25 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "10 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "28 January 2023" due date and 20 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 28 January 2023  | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

    When Admin sets the business date to "28 January 2023"
    And Customer makes "AUTOPAY" repayment on "28 January 2023" with 1040 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 28 January 2023  | Repayment        | 1040.0 | 1000.0    | 0.0      | 20.0 | 20.0      | 0.0          |
      | 28 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 28 January 2023  | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 28 January 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 20.0      | 1040.0 | 1040.0 | 1040.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 20        | 1040 | 1040 | 1040       | 0    | 0           |

    When Admin sets the business date to "30 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "28 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then On Loan Transactions tab the "Repayment" Transaction with date "28 January 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 28 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
      | 28 January 2023  | Repayment        | 1040.0 | 1000.0    | 0.0      | 20.0 | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | NSF fee    | true      | Specified due date | 28 January 2023  | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 20 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 28 January 2023  | Repayment        | 1040.0 | 1000.0    | 0.0      | 20.0 | 20.0      | 0.0          |
      | 28 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
      | 01 February 2023 | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
      | NSF fee    | true      | Specified due date | 28 January 2023  | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 0.0      | 20.0 | 40.0      | 1060.0 | 20.0 | 0.0        | 0.0  | 1040.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 40        | 1060 | 20   | 0          | 0    | 1040        |

    When Admin sets the business date to "05 February 2023"
    And Customer makes "AUTOPAY" repayment on "05 February 2023" with 1040 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 28 January 2023  | Repayment        | 1040.0 | 1000.0    | 0.0      | 20.0 | 20.0      | 0.0          |
      | 28 January 2023  | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
      | 01 February 2023 | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
      | 05 February 2023 | Repayment        | 1040.0 | 1000.0    | 0.0      | 20.0 | 20.0      | 0.0          |
      | 05 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | NSF fee    | true      | Specified due date | 28 January 2023  | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2023 | 05 February 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 40.0      | 1060.0 | 1060.0 | 0.0        | 1040.0 | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 40        | 1060 | 1060 | 0          | 1040 | 0           |


  @TestRailId:C2705 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: UC12 - partial payment, in advance penalty, interest, principal, fee due penalty, interest, principal, fee
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_INTEREST_FLAT | 01 January 2023   | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount

    When Admin sets the business date to "05 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 February 2023" due date and 20 EUR transaction amount

    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 20 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 10.0     | 20.0 | 20.0      | 1050.0 | 20.0 | 20.0       | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 10       | 20   | 20        | 1050 | 20   | 20         | 0    | 1030        |

    When Admin sets the business date to "15 January 2023"
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 500 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
      | 15 January 2023  | Repayment        | 500.0  | 490.0     | 10.0     | 0.0  | 0.0       | 510.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 10.0     | 20.0 | 20.0      | 1050.0 | 520.0 | 520.0      | 0.0  | 530.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 10       | 20   | 20        | 1050 | 520  | 520        | 0    | 530         |

    When Admin sets the business date to "25 January 2023"
    And Customer makes "AUTOPAY" repayment on "25 January 2023" with 530 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
      | 15 January 2023  | Repayment        | 500.0  | 490.0     | 10.0     | 0.0  | 0.0       | 510.0        |
      | 25 January 2023  | Repayment        | 530.0  | 510.0     | 0.0      | 20.0 | 0.0       | 0.0          |
      | 25 January 2023  | Accrual          | 50.0   | 0.0       | 10.0     | 20.0 | 20.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 25 January 2023 | 0.0             | 1000.0        | 10.0     | 20.0 | 20.0      | 1050.0 | 1050.0 | 1050.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 10       | 20   | 20        | 1050 | 1050 | 1050       | 0    | 0           |

    When Admin sets the business date to "30 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "10 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "15 January 2023"
    When Customer undo "1"th "Repayment" transaction made on "25 January 2023"
    Then Loan status will be "ACTIVE"
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then On Loan Transactions tab the "Repayment" Transaction with date "15 January 2023" is reverted
    Then On Loan Transactions tab the "Repayment" Transaction with date "25 January 2023" is reverted

    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 20 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
      | 15 January 2023  | Repayment        | 500.0  | 470.0     | 10.0     | 0.0  | 20.0      | 530.0        |
      | 25 January 2023  | Accrual          | 50.0   |   0.0     | 10.0     | 20.0 | 20.0      | 0.0          |
      | 25 January 2023  | Repayment        | 530.0  | 500.0     | 10.0     | 0.0  | 20.0      | 500.0        |
      | 01 February 2023 | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 10.0     | 20.0 | 20.0      | 1050.0 | 20.0 | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 10       | 20   | 20        | 1050 | 20   | 0          | 0    | 1030        |
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 10 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
      | 15 January 2023  | Repayment        | 500.0  | 470.0     | 10.0     | 0.0  | 20.0      | 530.0        |
      | 25 January 2023  | Accrual          | 50.0   | 0.0       | 10.0     | 20.0 | 20.0      | 0.0          |
      | 25 January 2023  | Repayment        | 530.0  | 500.0     | 10.0     | 0.0  | 20.0      | 500.0        |
      | 01 February 2023 | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
      | 01 February 2023 | Repayment        | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 1000.0       |
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 1000 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
      | 15 January 2023  | Repayment        | 500.0  | 470.0     | 10.0     | 0.0  | 20.0      | 530.0        |
      | 25 January 2023  | Accrual          | 50.0   | 0.0       | 10.0     | 20.0 | 20.0      | 0.0          |
      | 25 January 2023  | Repayment        | 530.0  | 500.0     | 10.0     | 0.0  | 20.0      | 500.0        |
      | 01 February 2023 | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
      | 01 February 2023 | Repayment        | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 1000.0       |
      | 01 February 2023 | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 20 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2023  | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
      | 15 January 2023  | Repayment        | 500.0  | 470.0     | 10.0     | 0.0  | 20.0      | 530.0        |
      | 25 January 2023  | Accrual          | 50.0   | 0.0       | 10.0     | 20.0 | 20.0      | 0.0          |
      | 25 January 2023  | Repayment        | 530.0  | 500.0     | 10.0     | 0.0  | 20.0      | 500.0        |
      | 01 February 2023 | Repayment        | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 1000.0       |
      | 01 February 2023 | Repayment        | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 1000.0       |
      | 01 February 2023 | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
      | 01 February 2023 | Repayment        | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 January 2023" is reverted
    Then On Loan Transactions tab the "Repayment" Transaction with date "15 January 2023" is reverted
    Then On Loan Transactions tab the "Repayment" Transaction with date "25 January 2023" is reverted
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee    | true      | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
      | Snooze fee | false     | Specified due date | 01 February 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 0.0             | 1000.0        | 10.0     | 20.0 | 20.0      | 1050.0 | 1050.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 10       | 20   | 20        | 1050 | 1050 | 0          | 0    | 0           |

  @Skip @TestRailId:C2800 @PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee
  Scenario: Verify the due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment strategy: Same day transaction
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_INTEREST_FLAT | 01 January 2023   | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "04 January 2023"
    And Customer makes "AUTOPAY" repayment on "04 January 2023" with 200 EUR transaction amount
    When Admin sets the business date to "12 January 2023"
    And Customer makes "AUTOPAY" repayment on "12 January 2023" with 300 EUR transaction amount
    And Admin adds a 1 % Processing charge to the loan with "en" locale on date: "12 January 2023"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 04 January 2023  | Repayment        | 200.0  | 190.0     | 10.0     | 0.0  | 0.0       | 810.0        |
      | 12 January 2023  | Repayment        | 300.0  | 289.9     | 0.0      | 10.1 | 0.0       | 520.1        |
    And Customer makes "AUTOPAY" repayment on "12 January 2023" with 520.10 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 04 January 2023  | Repayment        | 200.0  | 190.0     | 10.0     | 0.0  | 0.0       | 810.0        |
      | 12 January 2023  | Repayment        | 300.0  | 289.9     | 0.0      | 10.1 | 0.0       | 520.1        |
      | 12 January 2023  | Repayment        | 520.1  | 520.1     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 12 January 2023  | Accrual          | 20.1   | 0.0       | 10.0     | 10.1 | 0.0       | 0.0          |

  @TestRailId:C2810
  Scenario: As a user I would like to adjust an existing repayment and validate the event
    When Admin sets the business date to "01 November 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1         | 01 November 2022  | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 November 2022" with "1000" amount and expected disbursement date on "01 November 2022"
    When Admin successfully disburse the loan on "01 November 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "02 November 2022"
    And Customer makes "AUTOPAY" repayment on "02 November 2022" with 9 EUR transaction amount
    Then Loan Transactions tab has a transaction with date: "02 November 2022", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Repayment        | 9.0    | 9.0       | 0.0      | 0.0  | 0.0       | 991.0        |
    When Customer adjust "1"th repayment on "02 November 2022" with amount "10"
    Then Loan Transactions tab has a transaction with date: "02 November 2022", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Repayment        | 9.0    | 9.0       | 0.0      | 0.0  | 0.0       | 991.0        |
      | Repayment        | 10.0   | 0.0       | 0.0      | 0.0  | 0.0       | 990.0        |

  @TestRailId:C2898
  Scenario: Verify that in case of non/disbursed loan LoanRepaymentDueBusinessEvent is not sent - LP1 product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 October 2023"
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 October 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 30   | 31 October 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 0    | 0          | 0    | 1000        |
    When Admin sets the business date to "31 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

  @TestRailId:C2899
  Scenario: Verify that in case of non/disbursed loan LoanRepaymentDueBusinessEvent is not sent - LP2 auto payment enabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 0    | 01 October 2023  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 15   | 16 October 2023  |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 October 2023  |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 November 2023 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0.0  | 0          | 0    | 1000        |
    When Admin sets the business date to "16 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

  @TestRailId:C2900
  Scenario: Verify that in case of non/disbursed loan LoanRepaymentDueBusinessEvent is not sent - LP2 auto payment disabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct     | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 0    | 01 October 2023  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 15   | 16 October 2023  |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 October 2023  |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 November 2023 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0.0  | 0          | 0    | 1000        |
    When Admin sets the business date to "16 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

  @TestRailId:C2901 @AdvancedPaymentAllocation
  Scenario: Verify that in case of non/disbursed loan LoanRepaymentDueBusinessEvent is not sent - LP2 advanced payment allocation product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 0    | 01 October 2023  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 15   | 16 October 2023  |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 October 2023  |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 November 2023 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0.0  | 0          | 0    | 1000        |
    When Admin sets the business date to "16 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

  @TestRailId:C2902
  Scenario: Verify that in case of pre-payed installment LoanRepaymentDueBusinessEvent is not sent - LP1 product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 October 2023"
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 October 2023" with 1000 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 October 2023 |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 31 October 2023 | 01 October 2023 | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 1000.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 1000 | 1000       | 0    | 0           |
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "31 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

  @TestRailId:C2903
  Scenario: Verify that in case of pre-payed installment LoanRepaymentDueBusinessEvent is not sent - LP2 auto payment enabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 October 2023" with 250 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 01 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 500.0 | 250        | 0    | 500         |
    When Admin sets the business date to "16 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

  @TestRailId:C2904
  Scenario: Verify that in case of pre-payed installment LoanRepaymentDueBusinessEvent is not sent - LP2 auto payment disabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct     | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 October 2023" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 01 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 500.0 | 250        | 0    | 500         |
    When Admin sets the business date to "16 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

  @TestRailId:C2905 @AdvancedPaymentAllocation
  Scenario: Verify that in case of pre-payed installment LoanRepaymentDueBusinessEvent is not sent - LP2 advanced payment allocation product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 October 2023" with 250 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 01 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 500.0 | 250        | 0    | 500         |
    When Admin sets the business date to "16 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

  @TestRailId:C2906
  Scenario: Verify that in case of pre-payed installments for total amount (loan balance is 0) LoanRepaymentDueBusinessEvent is not sent - LP2 auto payment enabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 October 2023" with 750 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 01 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 01 October 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 | 01 October 2023 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 1000.0 | 750        | 0    | 0           |
    When Admin sets the business date to "16 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan
    When Admin sets the business date to "31 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan
    When Admin sets the business date to "15 November 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

  @TestRailId:C2907
  Scenario: Verify that in case of pre-payed installments for total amount (loan balance is 0) LoanRepaymentDueBusinessEvent is not sent - LP2 auto payment disabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct     | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 October 2023" with 1000 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 01 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 01 October 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 | 01 October 2023 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 1000.0 | 750        | 0    | 0           |
    When Admin sets the business date to "16 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan
    When Admin sets the business date to "31 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan
    When Admin sets the business date to "15 November 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

  @TestRailId:C2908 @AdvancedPaymentAllocation
  Scenario: Verify that in case of pre-payed installments for total amount (loan balance is 0) LoanRepaymentDueBusinessEvent is not sent - LP2 advanced payment allocation product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 October 2023" with 750 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 01 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 01 October 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 | 01 October 2023 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 1000.0 | 750        | 0    | 0           |
    When Admin sets the business date to "16 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan
    When Admin sets the business date to "31 October 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan
    When Admin sets the business date to "15 November 2023"
    When Admin runs inline COB job for Loan
    Then No new event with type "LoanRepaymentDueEvent" has been raised for the loan
    Then No new event with type "LoanRepaymentOverdueEvent" has been raised for the loan

  @TestRailId:C2961
  Scenario: Verify that outstanding amounts are rounded correctly in case of: installmentAmountInMultiplesOf=1, interestType: FLAT, amortizationType: EQUAL_INSTALLMENTS
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_INTEREST_FLAT | 01 September 2023 | 1250           | 15                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 September 2023" with "1250" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "1250" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |           | 1250.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 01 October 2023   |           | 937.62          | 312.38        | 15.62    | 0.0  | 0.0       | 328.0 | 0.0  | 0.0        | 0.0  | 328.0       |
      | 2  | 31   | 01 November 2023  |           | 625.24          | 312.38        | 15.62    | 0.0  | 0.0       | 328.0 | 0.0  | 0.0        | 0.0  | 328.0       |
      | 3  | 30   | 01 December 2023  |           | 312.86          | 312.38        | 15.62    | 0.0  | 0.0       | 328.0 | 0.0  | 0.0        | 0.0  | 328.0       |
      | 4  | 31   | 01 January 2024   |           | 0.0             | 312.86        | 15.64    | 0.0  | 0.0       | 328.5 | 0.0  | 0.0        | 0.0  | 328.5       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1250.0        | 62.50    | 0.0  | 0.0       | 1312.50 | 0.0  | 0.0        | 0.0  | 1312.50     |

  @TestRailId:C2962
  Scenario: Verify that outstanding amounts are rounded correctly in case of: installmentAmountInMultiplesOf=1, interestType: DECLINING_BALANCE, amortizationType: EQUAL_INSTALLMENTS
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_SAME_AS_PAYMENT | 01 September 2023 | 1250           | 15                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 September 2023" with "1250" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "1250" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |           | 1250.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 October 2023   |           | 943.62          | 306.38        | 15.62    | 0.0  | 0.0       | 322.0  | 0.0  | 0.0        | 0.0  | 322.0       |
      | 2  | 31   | 01 November 2023  |           | 633.42          | 310.2         | 11.8     | 0.0  | 0.0       | 322.0  | 0.0  | 0.0        | 0.0  | 322.0       |
      | 3  | 30   | 01 December 2023  |           | 319.34          | 314.08        | 7.92     | 0.0  | 0.0       | 322.0  | 0.0  | 0.0        | 0.0  | 322.0       |
      | 4  | 31   | 01 January 2024   |           | 0.0             | 319.34        | 3.99     | 0.0  | 0.0       | 323.33 | 0.0  | 0.0        | 0.0  | 323.33      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1250.0        | 39.33    | 0.0  | 0.0       | 1289.33 | 0.0  | 0.0        | 0.0  | 1289.33     |

  @TestRailId:C3106
  Scenario: Verify that there is not auto downpayment if disburseWithoutAutoDownPayment command is used
    When Admin sets the business date to "02 April 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 02 April 2023     | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "02 April 2024" with "1000" amount and expected disbursement date on "02 April 2024"
    And Admin successfully disburse the loan without auto downpayment on "02 April 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 02 April 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 02 April 2024 |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 15   | 17 April 2024 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 02 May 2024   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 17 May 2024   |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0.0  | 0          | 0    | 1000        |

  @TestRailId:C3129 @AdvancedPaymentAllocation
  Scenario: Verify installment due date logic for monthly installments - last day of the month
    When Admin sets the business date to "31 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 31 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 0    | 31 January 2024  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 29   | 29 February 2024 |           | 562.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 3  | 31   | 31 March 2024    |           | 374.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 4  | 30   | 30 April 2024    |           | 186.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 5  | 31   | 31 May 2024      |           | 0.0             | 186.0         | 0.0      | 0.0  | 0.0       | 186.0 | 0.0  | 0.0        | 0.0  | 186.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    And Admin successfully approves the loan on "31 January 2024" with "1000" amount and expected disbursement date on "31 January 2024"
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 0    | 31 January 2024  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 29   | 29 February 2024 |           | 562.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 3  | 31   | 31 March 2024    |           | 374.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 4  | 30   | 30 April 2024    |           | 186.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 5  | 31   | 31 May 2024      |           | 0.0             | 186.0         | 0.0      | 0.0  | 0.0       | 186.0 | 0.0  | 0.0        | 0.0  | 186.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    When Admin successfully disburse the loan on "31 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 31 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 31 January 2024  | 31 January 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 29 February 2024 |                 | 562.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0   | 0.0        | 0.0  | 188.0       |
      | 3  | 31   | 31 March 2024    |                 | 374.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0   | 0.0        | 0.0  | 188.0       |
      | 4  | 30   | 30 April 2024    |                 | 186.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0   | 0.0        | 0.0  | 188.0       |
      | 5  | 31   | 31 May 2024      |                 | 0.0             | 186.0         | 0.0      | 0.0  | 0.0       | 186.0 | 0.0   | 0.0        | 0.0  | 186.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 250.0 | 0.0        | 0.0  | 750.0       |

  @TestRailId:C3130 @AdvancedPaymentAllocation
  Scenario: Verify installment due date logic for monthly installments - last day of the month, expected and real disbursement dates are different
    When Admin sets the business date to "30 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 30 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 30 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 0    | 30 January 2024  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 30   | 29 February 2024 |           | 562.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 3  | 30   | 30 March 2024    |           | 374.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 4  | 31   | 30 April 2024    |           | 186.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 5  | 30   | 30 May 2024      |           | 0.0             | 186.0         | 0.0      | 0.0  | 0.0       | 186.0 | 0.0  | 0.0        | 0.0  | 186.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    And Admin successfully approves the loan on "30 January 2024" with "1000" amount and expected disbursement date on "30 January 2024"
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 30 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 0    | 30 January 2024  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 30   | 29 February 2024 |           | 562.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 3  | 30   | 30 March 2024    |           | 374.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 4  | 31   | 30 April 2024    |           | 186.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 5  | 30   | 30 May 2024      |           | 0.0             | 186.0         | 0.0      | 0.0  | 0.0       | 186.0 | 0.0  | 0.0        | 0.0  | 186.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    When Admin sets the business date to "31 January 2024"
    When Admin successfully disburse the loan on "31 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 31 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 31 January 2024  | 31 January 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 29 February 2024 |                 | 562.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0   | 0.0        | 0.0  | 188.0       |
      | 3  | 31   | 31 March 2024    |                 | 374.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0   | 0.0        | 0.0  | 188.0       |
      | 4  | 30   | 30 April 2024    |                 | 186.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0   | 0.0        | 0.0  | 188.0       |
      | 5  | 31   | 31 May 2024      |                 | 0.0             | 186.0         | 0.0      | 0.0  | 0.0       | 186.0 | 0.0   | 0.0        | 0.0  | 186.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 250.0 | 0.0        | 0.0  | 750.0       |

  @TestRailId:C3131 @AdvancedPaymentAllocation
  Scenario: Verify installment due date logic for monthly installments - last day of the month, repayment start calculated from Submitted on date - submit, approve, disburse on same date
    When Admin sets the business date to "31 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_REPAYMENT_START_SUBMITTED | 31 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 0    | 31 January 2024  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 29   | 29 February 2024 |           | 562.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 3  | 31   | 31 March 2024    |           | 374.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 4  | 30   | 30 April 2024    |           | 186.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 5  | 31   | 31 May 2024      |           | 0.0             | 186.0         | 0.0      | 0.0  | 0.0       | 186.0 | 0.0  | 0.0        | 0.0  | 186.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    And Admin successfully approves the loan on "31 January 2024" with "1000" amount and expected disbursement date on "31 January 2024"
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 0    | 31 January 2024  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 29   | 29 February 2024 |           | 562.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 3  | 31   | 31 March 2024    |           | 374.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 4  | 30   | 30 April 2024    |           | 186.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 5  | 31   | 31 May 2024      |           | 0.0             | 186.0         | 0.0      | 0.0  | 0.0       | 186.0 | 0.0  | 0.0        | 0.0  | 186.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    When Admin successfully disburse the loan on "31 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 31 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 31 January 2024  | 31 January 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 29 February 2024 |                 | 562.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0   | 0.0        | 0.0  | 188.0       |
      | 3  | 31   | 31 March 2024    |                 | 374.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0   | 0.0        | 0.0  | 188.0       |
      | 4  | 30   | 30 April 2024    |                 | 186.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0   | 0.0        | 0.0  | 188.0       |
      | 5  | 31   | 31 May 2024      |                 | 0.0             | 186.0         | 0.0      | 0.0  | 0.0       | 186.0 | 0.0   | 0.0        | 0.0  | 186.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 250.0 | 0.0        | 0.0  | 750.0       |

  @TestRailId:C3132 @AdvancedPaymentAllocation
  Scenario: Verify installment due date logic for monthly installments - last day of the month, repayment start calculated from Submitted on date - submit and approve on same date, disburse on next day
    When Admin sets the business date to "31 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_REPAYMENT_START_SUBMITTED | 31 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 0    | 31 January 2024  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 29   | 29 February 2024 |           | 562.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 3  | 31   | 31 March 2024    |           | 374.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 4  | 30   | 30 April 2024    |           | 186.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 5  | 31   | 31 May 2024      |           | 0.0             | 186.0         | 0.0      | 0.0  | 0.0       | 186.0 | 0.0  | 0.0        | 0.0  | 186.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    And Admin successfully approves the loan on "31 January 2024" with "1000" amount and expected disbursement date on "31 January 2024"
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 0    | 31 January 2024  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 29   | 29 February 2024 |           | 562.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 3  | 31   | 31 March 2024    |           | 374.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 4  | 30   | 30 April 2024    |           | 186.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 5  | 31   | 31 May 2024      |           | 0.0             | 186.0         | 0.0      | 0.0  | 0.0       | 186.0 | 0.0  | 0.0        | 0.0  | 186.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    When Admin sets the business date to "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 February 2024 |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 February 2024 | 01 February 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 29 February 2024 |                  | 562.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0   | 0.0        | 0.0  | 188.0       |
      | 3  | 31   | 31 March 2024    |                  | 374.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0   | 0.0        | 0.0  | 188.0       |
      | 4  | 30   | 30 April 2024    |                  | 186.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0   | 0.0        | 0.0  | 188.0       |
      | 5  | 31   | 31 May 2024      |                  | 0.0             | 186.0         | 0.0      | 0.0  | 0.0       | 186.0 | 0.0   | 0.0        | 0.0  | 186.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 250.0 | 0.0        | 0.0  | 750.0       |

  @TestRailId:C3133 @AdvancedPaymentAllocation
  Scenario: Verify installment due date logic for monthly installments - last day of the month, repayment start calculated from Submitted on date - submit and approve on same date, expected disbursement date on next day
    When Admin sets the business date to "31 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_REPAYMENT_START_SUBMITTED | 31 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 0    | 31 January 2024  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 29   | 29 February 2024 |           | 562.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 3  | 31   | 31 March 2024    |           | 374.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 4  | 30   | 30 April 2024    |           | 186.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 5  | 31   | 31 May 2024      |           | 0.0             | 186.0         | 0.0      | 0.0  | 0.0       | 186.0 | 0.0  | 0.0        | 0.0  | 186.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    And Admin successfully approves the loan on "31 January 2024" with "1000" amount and expected disbursement date on "01 February 2024"
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 February 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 0    | 01 February 2024 |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 29   | 29 February 2024 |           | 562.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 3  | 31   | 31 March 2024    |           | 374.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 4  | 30   | 30 April 2024    |           | 186.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 5  | 31   | 31 May 2024      |           | 0.0             | 186.0         | 0.0      | 0.0  | 0.0       | 186.0 | 0.0  | 0.0        | 0.0  | 186.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    When Admin sets the business date to "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 February 2024 |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 February 2024 | 01 February 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 29 February 2024 |                  | 562.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0   | 0.0        | 0.0  | 188.0       |
      | 3  | 31   | 31 March 2024    |                  | 374.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0   | 0.0        | 0.0  | 188.0       |
      | 4  | 30   | 30 April 2024    |                  | 186.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0   | 0.0        | 0.0  | 188.0       |
      | 5  | 31   | 31 May 2024      |                  | 0.0             | 186.0         | 0.0      | 0.0  | 0.0       | 186.0 | 0.0   | 0.0        | 0.0  | 186.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 250.0 | 0.0        | 0.0  | 750.0       |

  @TestRailId:C3223 @AdvancedPaymentAllocation @ProgressiveLoanSchedule
  Scenario: Verify AdvancedPaymentAllocation behaviour: loanScheduleProcessingType-horizontal, charge after maturity, in advanced repayment (future installment type: NEXT_INSTALLMENT)
    When Global config "charge-accrual-date" value set to "submitted-date"
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 September 2023 | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "1000" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 September 2023 |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 15   | 16 September 2023 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 01 October 2023   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 16 October 2023   |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
#    Add charge after maturity
    When Admin adds "LOAN_NSF_FEE" due date charge with "17 October 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 September 2023 |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 15   | 16 September 2023 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 01 October 2023   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 16 October 2023   |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 5  | 1    | 17 October 2023   |           | 0.0             | 0.0           | 0.0      | 0.0  | 20.0      | 20.0  | 0.0  | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 20.0      | 1020.0 | 0.0  | 0.0        | 0.0  | 1020.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 17 October 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
#    Make due date repayments
    And Customer makes "AUTOPAY" repayment on "01 September 2023" with 250 EUR transaction amount
    When Admin sets the business date to "02 September 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "02 September 2023"
    And Customer makes "AUTOPAY" repayment on "02 September 2023" with 250 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |                   | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 September 2023 | 02 September 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 15   | 01 October 2023   |                   | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 16 October 2023   |                   | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 5  | 1    | 17 October 2023   |                   | 0.0             | 0.0           | 0.0      | 0.0  | 20.0      | 20.0  | 0.0   | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 20.0      | 1020.0 | 500.0 | 250.0      | 0.0  | 520.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 September 2023 | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 September 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
      | 02 September 2023 | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 17 October 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
    When Admin sets the business date to "03 September 2023"
    When Admin runs inline COB job for Loan
    #Make backdated repayment to trigger loan transaction reprocessing
    And Customer makes "AUTOPAY" repayment on "01 September 2023" with 250 EUR transaction amount
    When Admin sets the business date to "04 September 2023"
    #Run COB to check there is no accounting meltdown and accrual is handled properly
    When Admin runs inline COB job for Loan
    When Global config "charge-accrual-date" value set to "due-date"

  @TestRailId:C3224
  Scenario: Verify that interest recalculation works properly when triggered by COB
    When Admin sets the business date to "01 April 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_SAR_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE_MULTIDISB | 01 April 2024     | 1000           | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 April 2024" with "1000" amount and expected disbursement date on "05 April 2024"
    When Admin sets the business date to "05 April 2024"
    When Admin successfully disburse the loan on "05 April 2024" with "500" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 05 April 2024 |           | 500.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 15   | 20 April 2024 |           | 334.47          | 165.53        | 2.47     | 0.0  | 0.0       | 168.0  | 0.0  | 0.0        | 0.0  | 168.0       |
      | 2  | 15   | 05 May 2024   |           | 168.12          | 166.35        | 1.65     | 0.0  | 0.0       | 168.0  | 0.0  | 0.0        | 0.0  | 168.0       |
      | 3  | 15   | 20 May 2024   |           | 0.0             | 168.12        | 0.83     | 0.0  | 0.0       | 168.95 | 0.0  | 0.0        | 0.0  | 168.95      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 05 April 2024    | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 500.0         | 4.95     | 0.0  | 0.0       | 504.95 | 0.0  | 0.0        | 0.0  | 504.95      |
    When Admin sets the business date to "21 April 2024"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 05 April 2024 |           | 500.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 15   | 20 April 2024 |           | 334.47          | 165.53        | 2.47     | 0.0  | 0.0       | 168.0  | 0.0  | 0.0        | 0.0  | 168.0       |
      | 2  | 15   | 05 May 2024   |           | 168.12          | 166.35        | 1.65     | 0.0  | 0.0       | 168.0  | 0.0  | 0.0        | 0.0  | 168.0       |
      | 3  | 15   | 20 May 2024   |           | 0.0             | 168.12        | 0.83     | 0.0  | 0.0       | 168.95 | 0.0  | 0.0        | 0.0  | 168.95      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 05 April 2024    | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 500.0         | 4.95     | 0.0  | 0.0       | 504.95 | 0.0  | 0.0        | 0.0  | 504.95      |
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 05 April 2024 |           | 500.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 15   | 20 April 2024 |           | 334.47          | 165.53        | 2.47     | 0.0  | 0.0       | 168.0  | 0.0  | 0.0        | 0.0  | 168.0       |
      | 2  | 15   | 05 May 2024   |           | 168.94          | 165.53        | 2.47     | 0.0  | 0.0       | 168.0  | 0.0  | 0.0        | 0.0  | 168.0       |
      | 3  | 15   | 20 May 2024   |           | 0.0             | 168.94        | 0.83     | 0.0  | 0.0       | 169.77 | 0.0  | 0.0        | 0.0  | 169.77      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 500.0         | 5.77     | 0.0  | 0.0       | 505.77 | 0.0  | 0.0        | 0.0  | 505.77      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 05 April 2024    | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 20 April 2024    | Accrual          | 2.47   | 0.0       | 2.47     | 0.0  | 0.0       | 0.0          |

  @TestRailId:C3225
  Scenario: Verify that payment allocation is correct in case of fee charged on an OVERPAID Loan and payment is backdated
#    --- 7/23 - Loan Created & Approved ---
    When Admin sets the business date to "23 July 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 23 July 2024      | 150            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
#   --- 7/23 - Disbursement for 111.92 EUR ---
    And Admin successfully approves the loan on "23 July 2024" with "111.92" amount and expected disbursement date on "23 July 2024"
    When Admin successfully disburse the loan on "23 July 2024" with "111.92" EUR transaction amount
#    --- 8/8 - Partial merchant issued refund - 76.48 Eur ---
    When Admin sets the business date to "08 August 2024"
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "08 August 2024" with 76.48 EUR transaction amount and system-generated Idempotency key
#    --- 8/13 - Manual Repayment - 35.44 Eur (Account closed) ---
    When Admin sets the business date to "13 August 2024"
    And Customer makes "MONEY_TRANSFER" repayment on "13 August 2024" with 35.44 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
#    --- 8/15 - Repayment reversed (Account reopened) ---
    When Admin sets the business date to "15 August 2024"
    When Customer undo "1"th "Repayment" transaction made on "13 August 2024"
    Then Loan status will be "ACTIVE"
    Then Loan has 35.44 outstanding amount
#    --- 8/22 - Autopay posted for 35.44 Eur ---
    When Admin sets the business date to "22 August 2024"
    And Customer makes "AUTOPAY" repayment on "22 August 2024" with 35.44 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
#    --- 8/24 - Autopay reversed ---
    When Admin sets the business date to "24 August 2024"
    When Customer undo "1"th "Repayment" transaction made on "22 August 2024"
    Then Loan status will be "ACTIVE"
    Then Loan has 35.44 outstanding amount
#    --- 8/24 - Loan Charge created for 2.80 Eur ---
    When Admin adds "LOAN_NSF_FEE" due date charge with "24 August 2024" due date and 2.80 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 38.24 outstanding amount
#    --- 8/24 (after COB) - Accrual created ---
    When Admin sets the business date to "25 August 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 23 July 2024   |           | 111.92          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 23 August 2024 |           | 0.0             | 111.92        | 0.0      | 0.0  | 0.0       | 111.92 | 76.48 | 76.48      | 0.0  | 35.44       |
      | 2  | 1    | 24 August 2024 |           | 0.0             | 0.0           | 0.0      | 0.0  | 2.8       | 2.8    | 0.0   | 0.0        | 0.0  | 2.8         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 111.92        | 0.0      | 0.0  | 2.8       | 114.72 | 76.48 | 76.48      | 0.0  | 38.24       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 23 July 2024     | Disbursement           | 111.92 | 0.0       | 0.0      | 0.0  | 0.0       | 111.92       | false    | false    |
      | 08 August 2024   | Merchant Issued Refund | 76.48  | 76.48     | 0.0      | 0.0  | 0.0       | 35.44        | false    | false    |
      | 13 August 2024   | Repayment              | 35.44  | 35.44     | 0.0      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 22 August 2024   | Repayment              | 35.44  | 35.44     | 0.0      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 24 August 2024   | Accrual                | 2.8    | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          | false    | false    |
#    --- 8/28 - Backdated Autopay posted for 38.24 Eur (35.44 principal + 2.80 penalty) with transactionDate 8/22 ---
    When Admin sets the business date to "28 August 2024"
    And Customer makes "AUTOPAY" repayment on "22 August 2024" with 38.24 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 23 July 2024   |                | 111.92          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 23 August 2024 | 22 August 2024 | 0.0             | 111.92        | 0.0      | 0.0  | 0.0       | 111.92 | 111.92 | 111.92     | 0.0  | 0.0         |
      | 2  | 1    | 24 August 2024 | 22 August 2024 | 0.0             | 0.0           | 0.0      | 0.0  | 2.8       | 2.8    | 2.8    | 2.8        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 111.92        | 0.0      | 0.0  | 2.8       | 114.72 | 114.72 | 114.72     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 23 July 2024     | Disbursement           | 111.92 | 0.0       | 0.0      | 0.0  | 0.0       | 111.92       | false    | false    |
      | 08 August 2024   | Merchant Issued Refund | 76.48  | 76.48     | 0.0      | 0.0  | 0.0       | 35.44        | false    | false    |
      | 13 August 2024   | Repayment              | 35.44  | 35.44     | 0.0      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 22 August 2024   | Repayment              | 35.44  | 35.44     | 0.0      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 22 August 2024   | Repayment              | 38.24  | 35.44     | 0.0      | 0.0  | 2.8       | 0.0          | false    | false    |
      | 24 August 2024   | Accrual                | 2.8    | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          | false    | false    |
    When Customer makes "AUTOPAY" repayment on "23 August 2024" with 10 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 23 July 2024     | Disbursement           | 111.92 | 0.0       | 0.0      | 0.0  | 0.0       | 111.92       | false    |
      | 08 August 2024   | Merchant Issued Refund | 76.48  | 76.48     | 0.0      | 0.0  | 0.0       | 35.44        | false    |
      | 13 August 2024   | Repayment              | 35.44  | 35.44     | 0.0      | 0.0  | 0.0       | 0.0          | true     |
      | 22 August 2024   | Repayment              | 35.44  | 35.44     | 0.0      | 0.0  | 0.0       | 0.0          | true     |
      | 22 August 2024   | Repayment              | 38.24  | 35.44     | 0.0      | 0.0  | 2.8       | 0.0          | false    |
      | 23 August 2024   | Repayment              | 10.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 24 August 2024   | Accrual                | 2.8    | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          | false    |
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 23 July 2024   |                | 111.92          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 23 August 2024 | 22 August 2024 | 0.0             | 111.92        | 0.0      | 0.0  | 0.0       | 111.92 | 111.92 | 111.92     | 0.0  | 0.0         |
      | 2  | 1    | 24 August 2024 | 22 August 2024 | 0.0             | 0.0           | 0.0      | 0.0  | 2.8       | 2.8    | 2.8    | 2.8        | 0.0  | 0.0         |
    When Admin sets the business date to "29 August 2024"
    When Admin runs inline COB job for Loan
    When Admin adds "LOAN_NSF_FEE" due date charge with "22 August 2024" due date and 5 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 23 July 2024     | Disbursement           | 111.92 | 0.0       | 0.0      | 0.0  | 0.0       | 111.92       | false    |
      | 08 August 2024   | Merchant Issued Refund | 76.48  | 76.48     | 0.0      | 0.0  | 0.0       | 35.44        | false    |
      | 13 August 2024   | Repayment              | 35.44  | 35.44     | 0.0      | 0.0  | 0.0       | 0.0          | true     |
      | 22 August 2024   | Repayment              | 35.44  | 35.44     | 0.0      | 0.0  | 0.0       | 0.0          | true     |
      | 22 August 2024   | Repayment              | 38.24  | 35.44     | 0.0      | 0.0  | 2.8       | 0.0          | false    |
      | 23 August 2024   | Repayment              | 10.0   | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | false    |
      | 24 August 2024   | Accrual                | 2.8    | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          | false    |
      | 24 August 2024   | Accrual                | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | false    |
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 23 July 2024   |                | 111.92          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 23 August 2024 | 23 August 2024 | 0.0             | 111.92        | 0.0      | 0.0  | 5.0       | 116.92 | 116.92 | 114.72     | 0.0  | 0.0         |
      | 2  | 1    | 24 August 2024 | 23 August 2024 | 0.0             | 0.0           | 0.0      | 0.0  | 2.8       | 2.8    | 2.8    | 2.8        | 0.0  | 0.0         |
    When Admin adds "LOAN_NSF_FEE" due date charge with "25 August 2024" due date and 5 EUR transaction amount
    When Admin sets the business date to "30 August 2024"
    When Admin runs inline COB job for Loan
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 23 July 2024     | Disbursement           | 111.92 | 0.0       | 0.0      | 0.0  | 0.0       | 111.92       | false    |
      | 08 August 2024   | Merchant Issued Refund | 76.48  | 76.48     | 0.0      | 0.0  | 0.0       | 35.44        | false    |
      | 13 August 2024   | Repayment              | 35.44  | 35.44     | 0.0      | 0.0  | 0.0       | 0.0          | true     |
      | 22 August 2024   | Repayment              | 35.44  | 35.44     | 0.0      | 0.0  | 0.0       | 0.0          | true     |
      | 22 August 2024   | Repayment              | 38.24  | 35.44     | 0.0      | 0.0  | 2.8       | 0.0          | false    |
      | 23 August 2024   | Repayment              | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          | false    |
      | 24 August 2024   | Accrual                | 2.8    | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          | false    |
      | 24 August 2024   | Accrual                | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | false    |
      | 25 August 2024   | Accrual                | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | false    |
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 23 July 2024   |                | 111.92          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 23 August 2024 | 23 August 2024 | 0.0             | 111.92        | 0.0      | 0.0  | 5.0       | 116.92 | 116.92 | 114.72     | 0.0  | 0.0         |
      | 2  | 2    | 25 August 2024 | 23 August 2024 | 0.0             | 0.0           | 0.0      | 0.0  | 7.8       | 7.8    | 7.8    | 7.8        | 0.0  | 0.0         |

  @TestRailId:C3247
  Scenario: Verify that repayment reversal is created as the result of backdated repayment transaction for interest bearing product
    When Admin sets the business date to "23 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 23 Jun 2024       | 400            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "23 June 2024" with "400" amount and expected disbursement date on "23 June 2024"
    And Admin successfully disburse the loan on "23 June 2024" with "400" EUR transaction amount
    When Admin sets the business date to "10 September 2024"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 September 2024" with 200 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 23 June 2024 |           | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 30   | 23 July 2024 |           | 0.0             | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 200.0 | 0.0        | 200.0 | 200.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 200.0 | 0.0        | 200.0 | 200.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 23 June 2024      | Disbursement           | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 10 September 2024 | Merchant Issued Refund | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 200.0        |
    When Customer makes "AUTOPAY" repayment on "24 June 2024" with 100 EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 23 June 2024 |           | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 30   | 23 July 2024 |           | 0.0             | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 300.0 | 100.0      | 200.0 | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 300.0 | 100.0      | 200.0 | 100.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 23 June 2024      | Disbursement           | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 24 June 2024      | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 300.0        |
      | 10 September 2024 | Merchant Issued Refund | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 100.0        |

  @TestRailId:C3261
  Scenario: Verify that repayment reversal is created as the result of backdated goodwill credit transaction for interest bearing product
    When Admin sets the business date to "23 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 23 Jun 2024       | 400            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "23 June 2024" with "400" amount and expected disbursement date on "23 June 2024"
    And Admin successfully disburse the loan on "23 June 2024" with "400" EUR transaction amount
    When Admin sets the business date to "10 September 2024"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 September 2024" with 200 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 23 June 2024 |           | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 30   | 23 July 2024 |           | 0.0             | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 200.0 | 0.0        | 200.0 | 200.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 200.0 | 0.0        | 200.0 | 200.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 23 June 2024      | Disbursement           | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 10 September 2024 | Merchant Issued Refund | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 200.0        |
    When Admin makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "24 June 2024" with 100 EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 23 June 2024 |           | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 30   | 23 July 2024 |           | 0.0             | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 300.0 | 100.0      | 200.0 | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 300.0 | 100.0      | 200.0 | 100.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 23 June 2024      | Disbursement           | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 24 June 2024      | Goodwill Credit        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 300.0        |
      | 10 September 2024 | Merchant Issued Refund | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 100.0        |

  @TestRailId:C3262
  Scenario: Verify that repayment reversal is created as the result of backdated interest payment waiver transaction for interest bearing product
    When Admin sets the business date to "23 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 23 Jun 2024       | 400            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "23 June 2024" with "400" amount and expected disbursement date on "23 June 2024"
    And Admin successfully disburse the loan on "23 June 2024" with "400" EUR transaction amount
    When Admin sets the business date to "10 September 2024"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 September 2024" with 200 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 23 June 2024 |           | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 30   | 23 July 2024 |           | 0.0             | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 200.0 | 0.0        | 200.0 | 200.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 200.0 | 0.0        | 200.0 | 200.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 23 June 2024      | Disbursement           | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 10 September 2024 | Merchant Issued Refund | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 200.0        |
    When Admin makes "INTEREST_PAYMENT_WAIVER" transaction with "AUTOPAY" payment type on "24 June 2024" with 100 EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 23 June 2024 |           | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 30   | 23 July 2024 |           | 0.0             | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 300.0 | 100.0      | 200.0 | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 300.0 | 100.0      | 200.0 | 100.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 23 June 2024      | Disbursement            | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 24 June 2024      | Interest Payment Waiver | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 300.0        |
      | 10 September 2024 | Merchant Issued Refund  | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 100.0        |

  @TestRailId:C3263
  Scenario: Verify that repayment reversal is created as the result of backdated merchant issued refund transaction for interest bearing product
    When Admin sets the business date to "23 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 23 Jun 2024       | 400            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "23 June 2024" with "400" amount and expected disbursement date on "23 June 2024"
    And Admin successfully disburse the loan on "23 June 2024" with "400" EUR transaction amount
    When Admin sets the business date to "10 September 2024"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 September 2024" with 200 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 23 June 2024 |           | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 30   | 23 July 2024 |           | 0.0             | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 200.0 | 0.0        | 200.0 | 200.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 200.0 | 0.0        | 200.0 | 200.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 23 June 2024      | Disbursement           | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 10 September 2024 | Merchant Issued Refund | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 200.0        |
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "24 June 2024" with 100 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 23 June 2024 |           | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 30   | 23 July 2024 |           | 0.0             | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 300.0 | 100.0      | 200.0 | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 300.0 | 100.0      | 200.0 | 100.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 23 June 2024      | Disbursement           | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 24 June 2024      | Merchant Issued Refund | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 300.0        |
      | 10 September 2024 | Merchant Issued Refund | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 100.0        |

  @TestRailId:C3264
  Scenario: Verify that repayment reversal is created as the result of backdated payout refund transaction for interest bearing product
    When Admin sets the business date to "23 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 23 Jun 2024       | 400            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "23 June 2024" with "400" amount and expected disbursement date on "23 June 2024"
    And Admin successfully disburse the loan on "23 June 2024" with "400" EUR transaction amount
    When Admin sets the business date to "10 September 2024"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 September 2024" with 200 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 23 June 2024 |           | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 30   | 23 July 2024 |           | 0.0             | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 200.0 | 0.0        | 200.0 | 200.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 200.0 | 0.0        | 200.0 | 200.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 23 June 2024      | Disbursement           | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 10 September 2024 | Merchant Issued Refund | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 200.0        |
    When Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "24 June 2024" with 100 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 23 June 2024 |           | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 30   | 23 July 2024 |           | 0.0             | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 300.0 | 100.0      | 200.0 | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 300.0 | 100.0      | 200.0 | 100.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 23 June 2024      | Disbursement           | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 24 June 2024      | Payout Refund          | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 300.0        |
      | 10 September 2024 | Merchant Issued Refund | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 100.0        |

  @TestRailId:C3265
  Scenario: Verify that repayment reversal is created as the result of backdated charge adjustment transaction for interest bearing product
    When Admin sets the business date to "23 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 23 Jun 2024       | 400            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "23 June 2024" with "400" amount and expected disbursement date on "23 June 2024"
    And Admin successfully disburse the loan on "23 June 2024" with "400" EUR transaction amount
    When Admin sets the business date to "10 September 2024"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 September 2024" with 200 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 23 June 2024 |           | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 30   | 23 July 2024 |           | 0.0             | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 200.0 | 0.0        | 200.0 | 200.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 200.0 | 0.0        | 200.0 | 200.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 23 June 2024      | Disbursement           | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 10 September 2024 | Merchant Issued Refund | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 200.0        |
    When Admin sets the business date to "24 June 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "24 June 2024" due date and 100 EUR transaction amount
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "24 June 2024" with 100 EUR transaction amount and externalId ""
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 23 June 2024 |           | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 30   | 23 July 2024 |           | 0.0             | 400.0         | 0.0      | 0.0  | 100.0     | 500.0 | 300.0 | 100.0      | 200.0 | 200.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 400.0         | 0.0      | 0.0  | 100.0     | 500.0 | 300.0 | 100.0      | 200.0 | 200.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 23 June 2024      | Disbursement           | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 24 June 2024      | Charge Adjustment      | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 300.0        |
      | 10 September 2024 | Merchant Issued Refund | 200.0  | 100.0     | 0.0      | 0.0  | 100.0     | 200.0        |

  @TestRailId:C3266
  Scenario: Verify that repayment reversal is created as the result of backdated disbursal transaction for interest bearing product
    When Admin sets the business date to "23 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_MULTIDISBURSE | 23 Jun 2024       | 500            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "23 June 2024" with "500" amount and expected disbursement date on "23 June 2024"
    And Admin successfully disburse the loan on "23 June 2024" with "400" EUR transaction amount
    When Admin sets the business date to "10 September 2024"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "10 September 2024" with 200 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 23 June 2024 |           | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 30   | 23 July 2024 |           | 0.0             | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 200.0 | 0.0        | 200.0 | 200.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 200.0 | 0.0        | 200.0 | 200.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 23 June 2024      | Disbursement           | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 10 September 2024 | Merchant Issued Refund | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 200.0        |
    When Admin successfully disburse the loan on "24 June 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 23 June 2024 |           | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      |    |      | 24 June 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 30   | 23 July 2024 |           | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 200.0 | 0.0        | 200.0 | 300.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 200.0 | 0.0        | 200.0 | 300.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 23 June 2024      | Disbursement           | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 24 June 2024      | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 10 September 2024 | Merchant Issued Refund | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 300.0        |

  @TestRailId:C3296
  Scenario: Verify the relationship for Interest Refund transaction after repayment by reverting related transaction
    When Admin sets the business date to "30 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_INTEREST_RECALCULATION | 01 January 2024   | 200            | 15                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "200" EUR transaction amount
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 January 2024" with 50 EUR transaction amount and self-generated Idempotency key
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "16 January 2024" with 50 EUR transaction amount and self-generated Idempotency key
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement           | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    |
      | 15 January 2024  | Merchant Issued Refund | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 15 January 2024  | Interest Refund        | 0.29   | 0.29      | 0.0      | 0.0  | 0.0       | 149.71       | false    |
      | 16 January 2024  | Payout Refund          | 50.0   | 48.79     | 1.21     | 0.0  | 0.0       | 100.92       | false    |
      | 16 January 2024  | Interest Refund        | 0.31   | 0.31      | 0.0      | 0.0  | 0.0       | 100.61       | false    |
    Then In Loan Transactions the "3"th Transaction has relationship type=RELATED with the "2"th Transaction
    Then In Loan Transactions the "5"th Transaction has relationship type=RELATED with the "4"th Transaction
    When Customer makes "AUTOPAY" repayment on "10 January 2024" with 25 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement           | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    |
      | 10 January 2024  | Repayment              | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 175.0        | false    |
      | 15 January 2024  | Merchant Issued Refund | 50.0   | 48.9      | 1.1      | 0.0  | 0.0       | 126.1        | false    |
      | 15 January 2024  | Interest Refund        | 0.29   | 0.29      | 0.0      | 0.0  | 0.0       | 125.81       | false    |
      | 16 January 2024  | Payout Refund          | 50.0   | 49.95     | 0.05     | 0.0  | 0.0       | 75.86        | false    |
      | 16 January 2024  | Interest Refund        | 0.3    | 0.3       | 0.0      | 0.0  | 0.0       | 75.56        | false    |
    Then In Loan Transactions the "4"th Transaction has relationship type=RELATED with the "3"th Transaction
    Then In Loan Transactions the "6"th Transaction has relationship type=RELATED with the "5"th Transaction
    When Customer undo "1"th "Merchant Issued Refund" transaction made on "15 January 2024"
    When Customer undo "1"th "Payout Refund" transaction made on "16 January 2024"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement           | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    |
      | 10 January 2024  | Repayment              | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 175.0        | false    |
      | 15 January 2024  | Merchant Issued Refund | 50.0   | 48.9      | 1.1      | 0.0  | 0.0       | 126.1        | true     |
      | 15 January 2024  | Interest Refund        | 0.29   | 0.29      | 0.0      | 0.0  | 0.0       | 125.81       | true     |
      | 16 January 2024  | Payout Refund          | 50.0   | 48.83     | 1.17     | 0.0  | 0.0       | 126.17       | true     |
      | 16 January 2024  | Interest Refund        | 0.31   | 0.31      | 0.0      | 0.0  | 0.0       | 125.86       | true     |

  @TestRailId:C3293
  Scenario: Verify that repayment made during downpayment period should not call payInterest or payPrincipal methods on the EmiCalculator for interest bearing progressive product with interest recalculation
    When Admin sets the business date to "23 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_DOWNPAYMENT | 23 Jun 2024       | 1000           | 25                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "23 June 2024" with "400" amount and expected disbursement date on "23 June 2024"
    And Admin successfully disburse the loan on "23 June 2024" with "400" EUR transaction amount
    When Customer makes "AUTOPAY" repayment on "23 June 2024" with 100 EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 23 June 2024 |              | 400.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 0    | 23 June 2024 | 23 June 2024 | 300.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0  | 100.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 30   | 23 July 2024 |              | 0.0             | 300.0         | 6.25     | 0.0  | 0.0       | 306.25 | 0.0   | 0.0        | 0.0   | 306.25      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 400.0         | 6.25     | 0.0  | 0.0       | 406.25 | 100.0 | 0.0        | 0.0   | 306.25      |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 23 June 2024      | Disbursement           | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 23 June 2024      | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 300.0        |

  @TestRailId:C3294
  Scenario: Verify that payment transactions made during downpayment period should not call payInterest or payPrincipal methods on the EmiCalculator for interest bearing progressive product with interest recalculation
    When Admin sets the business date to "23 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_DOWNPAYMENT | 23 Jun 2024       | 1000           | 25                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "23 June 2024" with "400" amount and expected disbursement date on "23 June 2024"
    And Admin successfully disburse the loan on "23 June 2024" with "400" EUR transaction amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "23 June 2024" with 25 EUR transaction amount and self-generated Idempotency key
    And Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "23 June 2024" with 25 EUR transaction amount and self-generated Idempotency key
    And Customer makes "INTEREST_PAYMENT_WAIVER" transaction with "AUTOPAY" payment type on "23 June 2024" with 25 EUR transaction amount and self-generated Idempotency key
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "23 June 2024" with 25 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 23 June 2024 |              | 400.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 0    | 23 June 2024 | 23 June 2024 | 300.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0  | 100.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 30   | 23 July 2024 |              | 0.0             | 300.0         | 6.25     | 0.0  | 0.0       | 306.25 | 0.0   | 0.0        | 0.0   | 306.25      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 400.0         | 6.25     | 0.0  | 0.0       | 406.25 | 100.0 | 0.0        | 0.0   | 306.25      |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 23 June 2024      | Disbursement            | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 23 June 2024      | Merchant Issued Refund  | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 375.0        |
      | 23 June 2024      | Goodwill Credit         | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 350.0        |
      | 23 June 2024      | Interest Payment Waiver | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 325.0        |
      | 23 June 2024      | Payout Refund           | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 300.0        |

  @TestRailId:C3295
  Scenario: Verify that backdated repayment made after loan maturity date should not call payInterest or payPrincipal methods on the EmiCalculator for interest bearing progressive product with interest recalculation
    When Admin sets the business date to "23 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_DOWNPAYMENT | 23 Jun 2024       | 1000           | 25                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "23 June 2024" with "400" amount and expected disbursement date on "23 June 2024"
    And Admin successfully disburse the loan on "23 June 2024" with "400" EUR transaction amount
    When Admin sets the business date to "24 July 2024"
    When Customer makes "AUTOPAY" repayment on "23 June 2024" with 100 EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 23 June 2024 |              | 400.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 0    | 23 June 2024 | 23 June 2024 | 300.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0  | 100.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 30   | 23 July 2024 |              | 0.0             | 300.0         | 6.25     | 0.0  | 0.0       | 306.25 | 0.0   | 0.0        | 0.0   | 306.25      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 400.0         | 6.25     | 0.0  | 0.0       | 406.25 | 100.0 | 0.0        | 0.0   | 306.25      |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 23 June 2024      | Disbursement           | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 23 June 2024      | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 300.0        |
