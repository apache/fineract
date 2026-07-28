@WorkingCapital
@WorkingCapitalLoanCreditBalanceRefundFeature
Feature: Working Capital Loan Credit Balance Refund

  @TestRailId:C76660
  Scenario: Verify working capital loan credit balance refund - partial refund keeps OVERPAID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    When Customer makes credit balance refund on "03 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Customer makes credit balance refund on "03 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C76661
  Scenario: Verify working capital loan credit balance refund - full refund closes loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9050.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    When Customer makes credit balance refund on "03 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C76662
  Scenario: Verify working capital loan credit balance refund - non-overpaid loan returns 400
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    When Admin sets the business date to "02 January 2026"
    And Initiating a credit balance refund on "02 January 2026" with 10.0 transaction amount on Working Capital loan results an error with the following data:
      | HTTP response code | Error message                                            |
      | 400                | Credit balance refund is allowed only for overpaid loans |
    And Customer makes repayment on "02 January 2026" with 9000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C76663
  Scenario: Verify working capital loan credit balance refund - payment details are accepted
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9050.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    When Customer makes credit balance refund on "03 January 2026" with 25.0 transaction amount on Working Capital loan with the following payment details:
      | paymentType   | accountNumber | checkNumber | routingCode | receiptNumber | bankNumber |
      | CHECK_PAYMENT | 12345         | 321         | 456         | 789           | 654        |
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan transaction with type "CREDIT_BALANCE_REFUND" has payment type "CHECK_PAYMENT"
    And Customer makes credit balance refund on "03 January 2026" with 25.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C76664
  Scenario: Verify working capital loan credit balance refund - refund amount greater than overpayment returns 400
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Initiating a credit balance refund on "03 January 2026" with 200.0 transaction amount on Working Capital loan results an error with the following data:
      | HTTP response code | Error message                                                 |
      | 400                | Credit balance refund amount cannot exceed overpayment amount |
    And Customer makes credit balance refund on "03 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C76665
  Scenario: Verify working capital loan credit balance refund - future transaction date returns 400
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Initiating a credit balance refund on "15 January 2026" with 50.0 transaction amount on Working Capital loan results an error with the following data:
      | HTTP response code | Error message           |
      | 400                | cannot.be.a.future.date |
    And Customer makes credit balance refund on "02 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C76666
  Scenario: Verify that a credit balance refund equal to the exact overpayment amount closes the loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9075.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Customer makes credit balance refund on "03 January 2026" with 75.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C76667
  Scenario: Verify that sequential partial credit balance refunds totaling less than the overpayment keep the loan OVERPAID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9200.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Customer makes credit balance refund on "03 January 2026" with 40.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Customer makes credit balance refund on "03 January 2026" with 60.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Customer makes credit balance refund on "03 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Customer makes credit balance refund on "03 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C76668
  Scenario: Verify that multiple partial repayments exceeding total payable accumulate into a single overpayment balance
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 3000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Customer makes repayment on "02 January 2026" with 3000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Customer makes repayment on "02 January 2026" with 3200.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance overpaymentAmount is "200.00"
    When Admin sets the business date to "03 January 2026"
    And Customer makes credit balance refund on "03 January 2026" with 200.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C76669
  Scenario: Verify that a credit balance refund with a valid classification is persisted and exposes the classification on the transaction
    Given A code value "REFUND_TO_CUSTOMER" exists for code name "working_capital_loan_credit_balance_refund_classification"
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Customer makes credit balance refund on "03 January 2026" with 50.0 transaction amount on Working Capital loan with valid classification "REFUND_TO_CUSTOMER"
    Then Verify Working Capital loan credit balance refund transaction has classification "REFUND_TO_CUSTOMER"

  @TestRailId:C76670
  Scenario: Verify that a credit balance refund with a non-existent classificationId is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Initiating a credit balance refund on "03 January 2026" with 50.0 transaction amount on Working Capital loan with classificationId 999999999 results an error with the following data:
      | HTTP response code | Error message                                                                               |
      | 400                | Code value does not exist in code working_capital_loan_credit_balance_refund_classification |

  @TestRailId:C76671
  Scenario Outline: Verify that a credit balance refund cannot be initiated on a non-OVERPAID loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin brings the working capital loan to "<status>"
    When Admin sets the business date to "03 January 2026"
    And Initiating a credit balance refund on "03 January 2026" with 50.0 transaction amount on Working Capital loan results an error with the following data:
      | HTTP response code | Error message                                            |
      | 400                | Credit balance refund is allowed only for overpaid loans |
    Examples:
      | status                         |
      | SUBMITTED_AND_PENDING_APPROVAL |
      | APPROVED                       |
      | ACTIVE                         |
      | CLOSED_OBLIGATIONS_MET         |

  @TestRailId:C76672
  Scenario: Verify that a credit balance refund with zero transaction amount is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Initiating a credit balance refund on "02 January 2026" with 0.0 transaction amount on Working Capital loan results an error with the following data:
      | HTTP response code | Error message                                            |
      | 400                | The parameter `transactionAmount` must be greater than 0 |

  @TestRailId:C76673
  Scenario: Verify that a credit balance refund with a negative transaction amount is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Initiating a credit balance refund on "02 January 2026" with -10.0 transaction amount on Working Capital loan results an error with the following data:
      | HTTP response code | Error message                                            |
      | 400                | The parameter `transactionAmount` must be greater than 0 |

  @TestRailId:C76674
  Scenario: Verify that a credit balance refund with transaction date before disbursal date is rejected
    When Admin sets the business date to "10 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 10 January 2026 | 10 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "10 January 2026" with "9000" amount and expected disbursement date on "10 January 2026"
    And Admin successfully disburse the Working Capital loan on "10 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "11 January 2026"
    And Customer makes repayment on "11 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Initiating a credit balance refund on "09 January 2026" with 50.0 transaction amount on Working Capital loan results an error with the following data:
      | HTTP response code | Error message                   |
      | 400                | cannot.be.before.disbursal.date |

  @TestRailId:C76675
  Scenario: Verify that a backdated credit balance refund is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "05 January 2026"
    And Initiating a credit balance refund on "03 January 2026" with 50.0 transaction amount on Working Capital loan results an error with the following data:
      | HTTP response code | Error message                                  |
      | 400                | Backdated credit balance refund is not allowed |

  @TestRailId:C76676
  Scenario: Verify that a credit balance refund cannot be repeated after the loan has been closed via full refund
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Customer makes credit balance refund on "03 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Initiating a credit balance refund on "03 January 2026" with 10.0 transaction amount on Working Capital loan results an error with the following data:
      | HTTP response code | Error message                                            |
      | 400                | Credit balance refund is allowed only for overpaid loans |

  @TestRailId:C76677
  Scenario: Verify that a credit balance refund note exceeding 1000 characters is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Initiating a credit balance refund on "02 January 2026" with 50.0 transaction amount and note of length 1001 on Working Capital loan results an error with the following data:
      | HTTP response code | Error message      |
      | 400                | exceeds max length |

  @TestRailId:C76678
  Scenario: Verify that a credit balance refund with a valid note within the allowed length is accepted
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Customer makes credit balance refund on "03 January 2026" with 50.0 transaction amount on Working Capital loan with note "Partial CBR for customer #123"
    Then Working Capital loan status will be "OVERPAID"

  @TestRailId:C76679
  Scenario: Verify that a successful credit balance refund raises the CBR transaction business event
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Customer makes credit balance refund on "03 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital credit balance refund transaction business event is raised with "50.0" amount and reversed "false"

  @TestRailId:C76680
  Scenario: Verify that the transaction template endpoint for creditBalanceRefund returns the current overpayment as expectedAmount
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9250.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin requests the Working Capital loan transaction template for command "creditBalanceRefund"
    Then The Working Capital loan transaction template expectedAmount is "250.00"

  @TestRailId:C76681
  Scenario: Verify that a successful credit balance refund decrements the loan balance overpaymentAmount by the refunded amount
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9200.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance overpaymentAmount is "200.00"
    When Admin sets the business date to "03 January 2026"
    And Customer makes credit balance refund on "03 January 2026" with 75.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance overpaymentAmount is "125.00"
    And Working Capital loan balance principalOutstanding is "0.00"

  @TestRailId:C76682
  Scenario: Verify that the credit balance refund transaction is stored with CREDIT_BALANCE_REFUND type and the provided externalId
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Customer makes credit balance refund on "03 January 2026" with 50.0 transaction amount and externalId "wcl-cbr-ext-001" on Working Capital loan
    Then Verify Working Capital loan credit balance refund transaction has type "CREDIT_BALANCE_REFUND" and externalId "wcl-cbr-ext-001"

  @TestRailId:C76683
  Scenario: Verify that reusing an externalId across two working capital loans for credit balance refund is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Customer makes credit balance refund on "03 January 2026" with 50.0 transaction amount and externalId "wcl-cbr-shared-001" on Working Capital loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Initiating a credit balance refund on "03 January 2026" with 50.0 transaction amount and externalId "wcl-cbr-shared-001" on Working Capital loan results an error with the following data:
      | HTTP response code | Error message  |
      | 400                | already.exists |

  @TestRailId:C76684
  Scenario: Verify that no accounting journal entries are created for a credit balance refund on a product with no accounting
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Customer makes credit balance refund on "03 January 2026" with 50.0 transaction amount on Working Capital loan
    Then No accounting journal entries are created for the Working Capital loan credit balance refund transaction

  @TestRailId:C76685
  Scenario: Verify that the Working Capital loan retrieval returns overpaymentAmount in the balance payload after credit balance refund
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Customer makes credit balance refund on "03 January 2026" with 25.0 transaction amount on Working Capital loan
    Then Working Capital loan balance payload contains the following fields:
      | field                | value |
      | principalOutstanding | 0.00  |
      | overpaymentAmount    | 75.00 |

  @TestRailId:C76686
  Scenario: Verify that a single overpaying repayment correctly attributes the full principal to totalPaidPrincipal
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance payload contains the following fields:
      | field                | value   |
      | principalOutstanding | 0.00    |
      | overpaymentAmount    | 100.00  |
      | totalPaidPrincipal   | 9000.00 |

  @TestRailId:C85504
  Scenario: Verify working capital loan credit balance refund match with overpaid amount to make loan closed - UC1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "02 January 2026" with 9050.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 0.0            | 0.0              | 50.0              |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment    | 9050.0            | 9000.0           | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "10 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Customer makes credit balance refund on "10 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                  | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement          | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment             | 9050.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Credit Balance Refund | 50.0              | 0.0              | 0.0               | 0.0                   | false    |

  @TestRailId:C85505
  Scenario: Verify working capital loan credit balance refund backdated less then overpaid amount to make loan overpaid - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "02 January 2026" with 9050.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 0.0            | 0.0              | 50.0              |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment    | 9050.0            | 9000.0           | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "10 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Customer makes credit balance refund on "10 January 2026" with 30.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 0.0            | 0.0              | 20.0              |
    And Working Capital Loan has transactions:
      | transactionDate | type                  | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement          | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment             | 9050.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Credit Balance Refund | 30.0              | 0.0              | 0.0               | 0.0                   | false    |

  @TestRailId:C85506
  Scenario: Verify working capital loan credit balance refund with backdated repayment undo to make loan active - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "02 January 2026" with 200.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 200.0              | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment    | 200.0             | 200.0            | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "05 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "05 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 0.0            | 0.0              | 300.0             |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment    | 200.0             | 200.0            | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 9100.0            | 8800.0           | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "10 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Customer makes credit balance refund on "10 January 2026" with 300.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                  | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement          | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment             | 200.0             | 200.0            | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment             | 9100.0            | 8800.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Credit Balance Refund | 300.0             | 0.0              | 0.0               | 0.0                   | false    |
    And Customer undo "1"th "REPAYMENT" transaction made on "02 January 2026" on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9200.0    | 9000.0             | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                  | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement          | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment             | 200.0             | 200.0            | 0.0               | 0.0                   | true     |
      | 05 January 2026 | Repayment             | 9100.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Credit Balance Refund | 300.0             | 200.0            | 0.0               | 0.0                   | false    |
    # The original 9000 amortization is untouched (base stays 9000, every period keeps its 50). The over-refunded
    # excess (200) is due principal, not amortized: it surfaces only on the CBR transaction date (10 January), where
    # the period's expected payment becomes 50 + 200 = 250. The 9000 repayment settles the projection from 06 January
    # on, so those periods would end the schedule early; 10 January still carries its due principal and keeps them.
    Then Working Capital loan amortization schedule has 10 periods, with the following data for periods:
      | paymentNo | paymentDate     | expectedPaymentAmount | actualPaymentAmount | expectedBalance | expectedAmortizationAmount | actualAmortizationAmount | expectedDiscountFeeBalance |
      | 0         | 01 January 2026 | -9000.00              |                     | 9000.00         |                            |                          | 0.00                       |
      | 1         | 02 January 2026 | 50.00                 | 0.00                | 8950.00         | 0.00                       | 0.00                     | 0.00                       |
      | 2         | 03 January 2026 | 50.00                 | 0.00                | 8900.00         | 0.00                       | 0.00                     | 0.00                       |
      | 3         | 04 January 2026 | 50.00                 | 0.00                | 8850.00         | 0.00                       | 0.00                     | 0.00                       |
      | 4         | 05 January 2026 | 50.00                 | 9000.00             | 8800.00         | 0.00                       | 0.00                     | 0.00                       |
      | 5         | 06 January 2026 | 50.00                 |                     | 8750.00         | 0.00                       |                          | 0.00                       |
      | 6         | 07 January 2026 | 50.00                 |                     | 8700.00         | 0.00                       |                          | 0.00                       |
      | 7         | 08 January 2026 | 50.00                 |                     | 8650.00         | 0.00                       |                          | 0.00                       |
      | 8         | 09 January 2026 | 50.00                 |                     | 8600.00         | 0.00                       |                          | 0.00                       |
      | 9         | 10 January 2026 | 250.00                |                     | 8550.00         | 0.00                       |                          | 0.00                       |

  @TestRailId:C85507
  Scenario: Verify working capital loan credit balance refund with backdated repayment to make loan overpaid - UC4
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "02 January 2026" with 9200.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 0.0            | 0.0              | 200.0             |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment    | 9200.0            | 9000.0           | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "10 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Customer makes credit balance refund on "10 January 2026" with 200.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                  | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement          | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment             | 9200.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Credit Balance Refund | 200.0             | 0.0              | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "12 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "05 January 2026" with 250.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 0.0            | 0.0              | 250.0             |
    And Working Capital Loan has transactions:
      | transactionDate | type                  | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement          | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment             | 9200.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment             | 250.0             | 0.0              | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Credit Balance Refund | 200.0             | 0.0              | 0.0               | 0.0                   | false    |

  @TestRailId:C85508
  Scenario: Verify working capital loan credit balance refund additional after backdated repayment to make loan overpaid - UC5
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "05 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "05 January 2026" with 9200.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 0.0            | 0.0              | 200.0             |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 9200.0            | 9000.0           | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "10 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Customer makes credit balance refund on "10 January 2026" with 200.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                  | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement          | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment             | 9200.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Credit Balance Refund | 200.0             | 0.0              | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "12 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "02 January 2026" with 250.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 0.0            | 0.0              | 250.0             |
    And Working Capital Loan has transactions:
      | transactionDate | type                  | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement          | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment             | 250.0             | 250.0            | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment             | 9200.0            | 8750.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Credit Balance Refund | 200.0             | 0.0              | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "15 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Customer makes credit balance refund on "15 January 2026" with 250.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                  | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement          | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment             | 250.0             | 250.0            | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment             | 9200.0            | 8750.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Credit Balance Refund | 200.0             | 0.0              | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Credit Balance Refund | 250.0             | 0.0              | 0.0               | 0.0                   | false    |

  @TestRailId:C85509
  Scenario: Verify working capital loan credit balance refund backdated is rejected - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "02 January 2026" with 9200.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 0.0            | 0.0              | 200.0             |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment    | 9200.0            | 9000.0           | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "10 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then Customer fails to make credit balance refund on "05 January 2026" with 200.0 EUR transaction amount backdated outcomes with error message
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 0.0            | 0.0              | 200.0             |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment    | 9200.0            | 9000.0           | 0.0               | 0.0                   | false    |

  @TestRailId:C85510
  Scenario: Verify working capital loan credit balance refund within breach schedule with undo backdated repayment- UC7
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | MONTHS              | FLAT                        | 3000         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "05 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 3000.00          | 3000.00           | null       | true   |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 3000.00          | 3000.00           | null       | null   |
#--- backdated repayment on Jan 5, 2026---#
    And Customer makes repayment on "05 January 2026" with 2500.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 2500.0             | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 2500.0            | 2500.0           | 0.0               | 0.0                   | false    |
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 3000.00          | 500.00            | null       | true   |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 3000.00          | 3000.00           | null       | null   |
#--- backdated repayment on Jan 10, 2026---#
    And Customer makes repayment on "10 January 2026" with 8000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 0.0            | 0.0              | 1500.0            |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 2500.0            | 2500.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 8000.0            | 6500.0           | 0.0               | 0.0                   | false    |
#--- CBR on the same biz date ---#
    When Customer makes credit balance refund on "05 February 2026" with 1500.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate  | type                  | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026  | Disbursement          | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026  | Repayment             | 2500.0            | 2500.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026  | Repayment             | 8000.0            | 6500.0           | 0.0               | 0.0                   | false    |
      | 05 February 2026 | Credit Balance Refund | 1500.0            | 0.0              | 0.0               | 0.0                   | false    |
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 3000.00          | 0.00              | null       | false  |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 0.00             | 0.00              | null       | false  |
    When Admin sets the business date to "10 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer undo "1"th "REPAYMENT" transaction made on "05 January 2026" on Working Capital loan
    # Undoing the 2500 leaves only the 8000 repayment, so the recomputed overpayment is 0 and the whole 1500 refund
    # over-refunds: it becomes due principal (principal: 9000 original + 1500 principal adjustment). The customer owes
    # the 1000 of original principal still unpaid plus the 1500 they were refunded in error - 10500 - 8000 = 2500.
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10500.0   | 8000.0             | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate  | type                  | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026  | Disbursement          | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026  | Repayment             | 2500.0            | 2500.0           | 0.0               | 0.0                   | true     |
      | 10 January 2026  | Repayment             | 8000.0            | 8000.0           | 0.0               | 0.0                   | false    |
      | 05 February 2026 | Credit Balance Refund | 1500.0            | 1500.0           | 0.0               | 0.0                   | false    |
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 3000.00          | 0.00              | null       | false  |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 2500.00          | 2500.00           | null       | null   |

  @TestRailId:C89753
  Scenario: Verify that undoing a repayment after a partial credit balance refund removes the full repayment amount from the breach schedule
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | MONTHS              | FLAT                        | 3000         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "05 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 3000.00          | 3000.00           | null       | true   |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 3000.00          | 3000.00           | null       | null   |
    And Customer makes repayment on "05 January 2026" with 2500.0 transaction amount on Working Capital loan
    And Customer makes repayment on "10 January 2026" with 8000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 0.0            | 0.0              | 1500.0            |
    When Customer makes credit balance refund on "05 February 2026" with 1000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 0.0            | 0.0              | 500.0             |
    When Admin sets the business date to "10 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer undo "1"th "REPAYMENT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 2500.0             | 0.0            | 0.0              | 0.0               |
    # Undoing the 8000 must take the whole 8000 back out of January, leaving only the 2500 paid: 500 short of the
    # 3000 minimum, so the period breaches. (The loan was overpaid by 500 before the undo; the reconciliation has to
    # account for that overpayment too, or 500 stays behind as phantom paid amount and the breach is missed.)
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 3000.00          | 500.00            | null       | true   |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 3000.00          | 3000.00           | null       | null   |

  @TestRailId:C89754
  Scenario: Verify that the breach schedule stays correct through the next COB run after a repayment undo following a partial credit balance refund
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | MONTHS              | FLAT                        | 3000         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "05 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "05 January 2026" with 2500.0 transaction amount on Working Capital loan
    And Customer makes repayment on "10 January 2026" with 8000.0 transaction amount on Working Capital loan
    When Customer makes credit balance refund on "05 February 2026" with 1000.0 transaction amount on Working Capital loan
    When Admin sets the business date to "10 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer undo "1"th "REPAYMENT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 2500.0             | 0.0            | 0.0              | 0.0               |
    # Same correction as the previous scenario: January keeps only the surviving 2500, so it stays 500 short.
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 3000.00          | 500.00            | null       | true   |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 3000.00          | 3000.00           | null       | null   |
    When Admin sets the business date to "01 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 3000.00          | 500.00            | null       | true   |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 3000.00          | 3000.00           | null       | true   |
      | 3            | 2026-03-01 | 2026-03-31 | 31           | 3000.00          | 3000.00           | null       | null   |

  @TestRailId:C89755
  Scenario: Verify that journal entries reflect the reprocessed transaction allocations after a repayment undo following a credit balance refund
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    # Booking-time entries are correct at booking time (outstanding was 8800, so 300 overpays):
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 9100.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 8800.0 |
      | LIABILITY | 245000       | Other Credit Liability    |        | 300.0  |
    When Admin sets the business date to "10 January 2026"
    When Customer makes credit balance refund on "10 January 2026" with 300.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 245000       | Other Credit Liability    | 300.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 300.0  |
    And Customer undo "1"th "REPAYMENT" transaction made on "02 January 2026" on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9200.0    | 9000.0             | 0.0            | 0.0              | 0.0               |
    # The undone Repayment 200 must be fully reversed in the ledger (original + mirror):
    Then Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 200.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 200.0  |
      | ASSET     | 112601       | Loans Receivable          | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 200.0  |
    # The reprocess re-split this repayment (principal 8800 -> 9000, overpayment 300 -> 100). The ledger is
    # append-only: the stale booking lines are cancelled by offsetting mirrors and the corrected split is posted, so
    # all three generations are visible. NET = 9100 in / 9000 principal / 100 overpayment.
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 9100.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 8800.0 |
      | LIABILITY | 245000       | Other Credit Liability    |        | 300.0  |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 9100.0 |
      | ASSET     | 112601       | Loans Receivable          | 8800.0 |        |
      | LIABILITY | 245000       | Other Credit Liability    | 300.0  |        |
      | LIABILITY | 145023       | Suspense/Clearing account | 9100.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 9000.0 |
      | LIABILITY | 245000       | Other Credit Liability    |        | 100.0  |
    # The refund was booked fully against overpayment; after the undo only 100 of it still is, and 200 became
    # newly-lent principal. Booking lines cancelled by mirrors, corrected split posted. NET = 100 overpayment +
    # 200 receivable / 300 out.
    And Working Capital Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 245000       | Other Credit Liability    | 300.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 300.0  |
      | LIABILITY | 245000       | Other Credit Liability    |       | 300.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 300.0 |        |
      | LIABILITY | 245000       | Other Credit Liability    | 100.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 300.0  |

  @TestRailId:C89756
  Scenario: Verify that a repayment undo after a partial credit balance refund keeps journal entries consistent with the recomputed loan balances
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 2500.0 transaction amount on Working Capital loan
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 8000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "15 January 2026"
    When Customer makes credit balance refund on "15 January 2026" with 1000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 0.0            | 0.0              | 500.0             |
    When Admin sets the business date to "20 January 2026"
    And Customer undo "1"th "REPAYMENT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 2500.0             | 0.0            | 0.0              | 0.0               |
    Then Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 8000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 6500.0 |
      | LIABILITY | 245000       | Other Credit Liability    |        | 1500.0 |
      | ASSET     | 112601       | Loans Receivable          | 6500.0 |        |
      | LIABILITY | 245000       | Other Credit Liability    | 1500.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 8000.0 |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 2500.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 2500.0 |
    # After the undo the refund has no overpayment behind it at all, so the whole 1000 is newly-lent principal.
    # The booking lines (which drove Other Credit Liability to -1000) are cancelled by mirrors rather than deleted,
    # then the corrected split is posted. NET = 1000 receivable / 1000 out, and Other Credit Liability nets to 0.
    And Working Capital Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 245000       | Other Credit Liability    | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
      | LIABILITY | 245000       | Other Credit Liability    |        | 1000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |

