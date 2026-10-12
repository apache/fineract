@WorkingCapital
@WorkingCapitalLoanChargesFeature
Feature: WorkingCapitalLoanChargesFeature

  @TestRailId:C80954
  Scenario: Verify Working Capital Charge product - UC1: charge can be created modified and deleted
    When Admin creates working capital loan charge
    When Admin updates working capital loan charge
    When Admin deletes working capital loan charge

  @TestRailId:C80955
  Scenario: Verify Working Capital Charge product - UC2: template API returns filtered options
    Then Admin retrieves the charge template for Working Capital Loan
    Then The charge template chargeTimeTypeOptions contains only Specified due date
    Then The charge template chargeCalculationTypeOptions contains only Flat
    Then The charge template chargePaymentModeOptions contains only Regular

  @TestRailId:C80956
  Scenario: Verify Working Capital Charge product - UC3: template API with Specified due date returns Flat calculation type only
    Then Admin retrieves the charge template for Working Capital Loan with charge time type "SPECIFIED_DUE_DATE"
    Then The charge template chargeCalculationTypeOptions contains only Flat

  @TestRailId:C80957
  Scenario: Verify Working Capital Charge product - UC4: charge can be created as penalty
    When Admin creates working capital loan charge as penalty
    Then Admin retrieves working capital loan charge and verifies it is a penalty
    When Admin deletes working capital loan charge

  @TestRailId:C80958
  Scenario: Verify Working Capital Charge product - UC5: creating charge without payment mode will result defaults to payment mode Regular
    When Admin creates working capital loan charge without payment mode
    Then Admin retrieves working capital loan charge and verifies payment mode is Regular
    When Admin deletes working capital loan charge

  @TestRailId:C80959
  Scenario: Verify Working Capital Charge product - UC6: invalid chargeTimeType Disbursement fails (Negative)
    Then Creating working capital loan charge with "DISBURSEMENT" chargeTimeType and "FLAT" chargeCalculationType results an error with the following data:
      | httpCode | errorMessage                                          |
      | 400      | The parameter `chargeTimeType` must be one of [ 2 ] . |

  @TestRailId:C80960
  Scenario: Verify Working Capital Charge product - UC7: invalid chargeCalculationType Percentage Amount fails (Negative)
    Then Creating working capital loan charge with "SPECIFIED_DUE_DATE" chargeTimeType and "PERCENTAGE_AMOUNT" chargeCalculationType results an error with the following data:
      | httpCode | errorMessage                                                 |
      | 400      | The parameter `chargeCalculationType` must be one of [ 1 ] . |

  @TestRailId:C80961
  Scenario: Verify Working Capital Charge product - UC8: invalid chargeTimeType Instalment Fee fails (Negative)
    Then Creating working capital loan charge with "INSTALLMENT_FEE" chargeTimeType and "FLAT" chargeCalculationType results an error with the following data:
      | httpCode | errorMessage                                          |
      | 400      | The parameter `chargeTimeType` must be one of [ 2 ] . |

#    #############################################
#    --- Loan Account level ---
#  ###############################################
  @TestRailId:C83048
  Scenario: Verify Working Capital Charge on loan account level - UC1: specified due date fee added with business date as due date
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 35.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 10 January 2026 | 35.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C83049
  Scenario: Verify Working Capital Charge on loan account level - UC2: specified due date penalty added with business date as due date
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "10 January 2026" due date and 35.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 10 January 2026 | 35.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C83050
  Scenario: Verify Working Capital Charge on loan account level - UC3: specified due date charge added with future due date
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 35.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 12 January 2026 | 35.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C83051
  Scenario: Verify Working Capital Charge on loan account level - UC4: specified due date charge added on disbursement date
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "01 January 2026" due date and 35.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 01 January 2026 | 35.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "01 January 2026"

  @TestRailId:C83052
  Scenario: Verify Working Capital Charge on loan account level - UC5: specified due date charge added on disbursement date with future due date
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 35.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 12 January 2026 | 35.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "01 January 2026"

  @TestRailId:C83053
  Scenario: Verify Working Capital Charge on loan account level - UC6: multiple charges can be added to the same loan
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 35.0 transaction amount
    Then a Working Capital Loan Add Charge business event is raised for "fee" charge "Working Capital Loan Fee" with "35.0" EUR amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "15 January 2026" due date and 50.0 transaction amount
    Then a Working Capital Loan Add Charge business event is raised for "penalty" charge "Working Capital Loan Penalty" with "50.0" EUR amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 12 January 2026 | 35.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 15 January 2026 | 50.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C83054
  Scenario: Verify Working Capital Charge on loan account level - UC7: fee and penalty with different amounts can be added
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "12 January 2026" due date and 25.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 12 January 2026 | 15.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 12 January 2026 | 25.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C83055
  Scenario: Verify Working Capital Charge on loan account level - UC8: charge can be added with minimum amount
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                  | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 1.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 12 January 2026 | 1.0    | EUR      | false     | Specified due date | Flat                    | Regular             |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C83056
  Scenario: Verify Working Capital Charge on loan account level - UC9: charge can be added with large amount
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 5000.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 12 January 2026 | 5000.0 | EUR      | false     | Specified due date | Flat                    | Regular             |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C83057
  Scenario: Verify Working Capital Charge on loan account level - UC10: charge balances are correctly calculated (Outstanding only, no repayment)
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 100.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 12 January 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    Then Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 100.0           | 0.0      | 0.0            | 0.0                 | 0.0          |
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "12 January 2026" due date and 50.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 12 January 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 12 January 2026 | 50.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    Then Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 100.0           | 0.0      | 50.0           | 50.0                | 0.0          |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "12 January 2026"

  @TestRailId:C83058
  Scenario: Verify Working Capital Charge on loan account level - UC11: charge in use cannot be deleted, template retrieved
    When Admin creates working capital loan charge without payment mode
    Then Admin retrieves working capital loan charge and verifies payment mode is Regular
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 0.0       | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Admin retrieves working capital loan charge template by loan id
    Then Admin add working capital loan charge by loan id and charge id with amount 45.0 and due date "21-01-2026"
    When Admin fails to delete working capital loan charge with status 403 message "This charge cannot be deleted, it is already used in"
    Then Working Capital Loan has the created charges
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C83059
  Scenario: Verify Working Capital Charge on loan account level - UC12: charge cannot be added in state "Submitted and pending approval"
    When Admin creates working capital loan charge without payment mode
    Then Admin retrieves working capital loan charge and verifies payment mode is Regular
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    Then Working Capital loan status will be "SUBMITTED_AND_PENDING_APPROVAL"
    And Trying to add working capital loan charge by loan id and charge id with amount 45.0 and due date "21-01-2026" results an error with the following data:
      | httpCode | errorMessage                    |
      | 400      | Loan should be in active status |
    When Admin rejects the working capital loan on "01 January 2026"
    Then Working capital loan rejection was successful

  @TestRailId:C83060
  Scenario: Verify Working Capital Charge on loan account level - UC13: charge cannot be added in state "Approved"
    When Admin creates working capital loan charge without payment mode
    Then Admin retrieves working capital loan charge and verifies payment mode is Regular
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 0.0       | 9000.0            | 100000.0           | 18.0              | null             |
    Then Working Capital loan status will be "APPROVED"
    And Trying to add working capital loan charge by loan id and charge id with amount 45.0 and due date "21-01-2026" results an error with the following data:
      | httpCode | errorMessage                    |
      | 400      | Loan should be in active status |
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    When Admin rejects the working capital loan on "01 January 2026"
    Then Working capital loan rejection was successful