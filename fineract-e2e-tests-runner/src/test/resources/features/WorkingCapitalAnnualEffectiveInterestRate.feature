@WorkingCapital @WorkingCapitalAnnualEirFeature
Feature: Working Capital annual effective interest rate

  @TestRailId:C102371
  Scenario: Verify that loan details expose the six-decimal annual rate and the schedule runs on the daily rate derived from it
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan details has the following exact decimal field values:
      | calculatedAnnualEir | 0.0 |
    When Admin successfully add discount with "1000" amount on Working Capital loan account
    Then Working capital loan details has the following exact decimal field values:
      | calculatedAnnualEir | 0.468451 |
    And Working capital loan details has the following field values:
      | numberOfRepayments  | 200  |
      | periodPaymentAmount | 50.0 |
    And Working capital loan details raw JSON serialises "calculatedAnnualEir" as "0.468451"
    When Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields with positive effectiveInterestRate value:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber | effectiveInterestRate |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 360         | 50.00                 | 200                   | 0.001067814440864707  |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C102372
  Scenario: Verify that loan details API and the WC account event carry the annual rate only - dailyEir is gone from both contracts
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Verify Working Capital loan disbursement was successful
    When Admin successfully add discount with "1000" amount on Working Capital loan account
    Then Working capital loan details raw JSON serialises "calculatedAnnualEir" as "0.468451"
    And Working capital loan details raw JSON does not contain field "dailyEir"
    And Working Capital loan account event schema contains field "calculatedAnnualEir"
    And Working Capital loan account event schema does not contain field "dailyEir"
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C102373
  Scenario: Verify that every WC account event carries the same annual rate as the loan details across the loan lifecycle
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And a Working Capital Loan Balance Changed business event carries the same annual effective interest rate as loan details
    And a Working Capital Loan Status Changed business event carries the same annual effective interest rate as loan details
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Verify Working Capital loan disbursement was successful
    And a Working Capital Loan Status Changed business event carries the same annual effective interest rate as loan details
    And a Working Capital Loan Balance Changed business event carries the same annual effective interest rate as loan details
    When Admin successfully add discount with "1000" amount on Working Capital loan account
    Then Working capital loan details has the following exact decimal field values:
      | calculatedAnnualEir | 0.468451 |
    And a Working Capital Loan Balance Changed business event carries the same annual effective interest rate as loan details
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 200.0 transaction amount on Working Capital loan
    Then Working capital loan details has the following exact decimal field values:
      | calculatedAnnualEir | 0.468451 |
    And a Working Capital Loan Balance Changed business event carries the same annual effective interest rate as loan details
    When Admin closes the Working Capital loan with a full repayment on "05 January 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And a Working Capital Loan Balance Changed business event carries the same annual effective interest rate as loan details
    And a Working Capital Loan Status Changed business event carries the same annual effective interest rate as loan details

  @TestRailId:C102374
  Scenario: Verify that annual rate is compounded over the product NPV day count - 365-day product
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_365    | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Verify Working Capital loan disbursement was successful
    When Admin successfully add discount with "1000" amount on Working Capital loan account
    Then Working capital loan details has the following exact decimal field values:
      | calculatedAnnualEir | 0.468545 |
    And Working capital loan details has the following field values:
      | numberOfRepayments  | 203   |
      | periodPaymentAmount | 49.32 |
    When Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields with positive effectiveInterestRate value:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber | effectiveInterestRate |
      | 1000.00           | 9000.00               | 100000.00          | 18                | 365         | 49.32                 | 203                   | 0.001053354703036168  |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C102375
  Scenario: Verify that a discount fee adjustment re-prices the loan and the annual rate follows
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And a Working Capital Loan Balance Changed business event is raised on approval
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Verify Working Capital loan disbursement was successful
    And a Working Capital Loan Balance Changed business event is raised
    When Admin successfully add discount with "1000" amount on Working Capital loan account
    Then a Working Capital Loan Balance Changed business event is raised with annual effective interest rate "0.468451"
    And Working capital loan details has the following exact decimal field values:
      | calculatedAnnualEir | 0.468451 |
    When Admin adds Discount fee adjustment with "500" amount on Working Capital loan account for last discount
    Then Working capital loan details has the following exact decimal field values:
      | calculatedAnnualEir | 0.228340 |
    And Working capital loan details has the following field values:
      | numberOfRepayments | 190 |
    And a Working Capital Loan Balance Changed business event is raised with annual effective interest rate "0.228340"
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C102376
  Scenario: Verify that undo disbursal drops the rate and a re-disbursement with a different discount re-prices it
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Verify Working Capital loan disbursement was successful
    When Admin successfully add discount with "1000" amount on Working Capital loan account
    Then Working capital loan details has the following exact decimal field values:
      | calculatedAnnualEir | 0.468451 |
    When Admin successfully undo Working Capital disbursal
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan details has the following exact decimal field values:
      | calculatedAnnualEir | 0.0 |
    When Admin successfully add discount with "2000" amount on Working Capital loan account
    Then Working capital loan details has the following exact decimal field values:
      | calculatedAnnualEir | 0.967903 |
    And Working capital loan details has the following field values:
      | numberOfRepayments | 220 |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C102377
  Scenario: Verify that backdated repayment does not move the annual rate
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Verify Working Capital loan disbursement was successful
    When Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "05 January 2026" with 300.0 transaction amount on Working Capital loan
    Then Working capital loan details has the following exact decimal field values:
      | calculatedAnnualEir | 0.468451 |
    And Working capital loan details raw JSON serialises "calculatedAnnualEir" as "0.468451"
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C102378
  Scenario: Verify that ten-period loan reports a large annual rate at six decimals
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 450             | 100000             | 18                |          |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "450" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "450" EUR transaction amount
    Then Verify Working Capital loan disbursement was successful
    When Admin successfully add discount with "50" amount on Working Capital loan account
    Then Working capital loan details has the following exact decimal field values:
      | calculatedAnnualEir | 1093.799765 |
    And Working capital loan details has the following field values:
      | numberOfRepayments | 10 |
    And Working capital loan details raw JSON serialises "calculatedAnnualEir" as "1093.799765"
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C102379
  Scenario: Verify that the annual rate is persisted at six decimals and a model written before the field existed still reports it
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Verify Working Capital loan disbursement was successful
    When Admin successfully add discount with "1000" amount on Working Capital loan account
    Then The persisted amortization model of the Working Capital loan stores annual effective interest rate literal "0.468451"
    When Admin removes the stored annual effective interest rate from the persisted amortization model of the Working Capital loan
    Then The persisted amortization model of the Working Capital loan does not store the annual effective interest rate
    And Working capital loan details has the following exact decimal field values:
      | calculatedAnnualEir | 0.468451 |
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 200.0 transaction amount on Working Capital loan
    Then Working capital loan details has the following exact decimal field values:
      | calculatedAnnualEir | 0.468451 |
    Then Admin closes the Working Capital loan with a full repayment on "05 January 2026"

  @TestRailId:C102380
  Scenario: Verify that a period payment rate change keeps a six-decimal annual rate on the details
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Verify Working Capital loan disbursement was successful
    When Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "12.5" value
    Then Working Capital Loan period payment rate in effect is "12.5"
    And Working capital loan details has the following field values:
      | calculatedAnnualEir | present |
      | numberOfRepayments  | 302     |
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  @TestRailId:C102381
  Scenario: Verify that the published annual rate does not depend on the tenant money rounding mode
    When Global config "rounding-mode" value set to "2" through DefaultApi
    And Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Verify Working Capital loan disbursement was successful
    When Admin successfully add discount with "1000" amount on Working Capital loan account
    Then Working capital loan details has the following exact decimal field values:
      | calculatedAnnualEir | 0.468451 |
    And The persisted amortization model of the Working Capital loan stores annual effective interest rate literal "0.468451"
    When Global config "rounding-mode" value set to "6" through DefaultApi
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C102382
  Scenario: Verify after a period payment rate change the details expose annual effective rate the schedule runs on
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Verify Working Capital loan disbursement was successful
    When Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "12.5" value
    Then Working Capital Loan period payment rate in effect is "12.5"
    And Working capital loan details has the following field values:
      | numberOfRepayments | 302 |
    And Working capital loan details has the following exact decimal field values:
      | calculatedAnnualEir | 0.306322 |
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  @TestRailId:C102383
  Scenario: Verify that a model with eir written before the field existed is upgraded on its next write
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Verify Working Capital loan disbursement was successful
    When Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin removes the stored annual effective interest rate from the persisted amortization model of the Working Capital loan
    And Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 200.0 transaction amount on Working Capital loan
    Then The persisted amortization model of the Working Capital loan stores annual effective interest rate literal "0.468451"
    Then Admin closes the Working Capital loan with a full repayment on "05 January 2026"

  @TestRailId:C102390
  Scenario: Loan details GET returns the annual effective interest rate normalised to six decimal places
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Verify Working Capital loan disbursement was successful
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    Then Working capital loan details has the following exact decimal field values:
      | calculatedAnnualEir | 0.468451 |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"