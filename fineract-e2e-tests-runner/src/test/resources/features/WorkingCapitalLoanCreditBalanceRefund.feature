@WorkingCapitalLoanCreditBalanceRefundFeature
Feature: Working Capital Loan Credit Balance Refund

  @TestRailId:C76660
  Scenario: Verify working capital loan credit balance refund - partial refund keeps OVERPAID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
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
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
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
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    When Admin sets the business date to "02 January 2026"
    And Initiating a credit balance refund on "02 January 2026" with 10.0 transaction amount on Working Capital loan results an error with the following data:
      | HTTP response code | Error message                                                  |
      | 400                | Credit balance refund is allowed only for overpaid loans       |
    And Customer makes repayment on "02 January 2026" with 9000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C76663
  Scenario: Verify working capital loan credit balance refund - payment details are accepted
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
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
    And Customer makes credit balance refund on "03 January 2026" with 25.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C76664
  Scenario: Verify working capital loan credit balance refund - refund amount greater than overpayment returns 400
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Initiating a credit balance refund on "03 January 2026" with 200.0 transaction amount on Working Capital loan results an error with the following data:
      | HTTP response code | Error message                                                        |
      | 400                | Credit balance refund amount cannot exceed overpayment amount        |
    And Customer makes credit balance refund on "03 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C76665
  Scenario: Verify working capital loan credit balance refund - future transaction date returns 400
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
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
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
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
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
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
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
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
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
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
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Initiating a credit balance refund on "03 January 2026" with 50.0 transaction amount on Working Capital loan with classificationId 999999999 results an error with the following data:
      | HTTP response code | Error message                                                                                                     |
      | 400                | Code value does not exist in code working_capital_loan_credit_balance_refund_classification                       |

  @TestRailId:C76671
  Scenario Outline: Verify that a credit balance refund cannot be initiated on a non-OVERPAID loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin brings the working capital loan to "<status>"
    When Admin sets the business date to "03 January 2026"
    And Initiating a credit balance refund on "03 January 2026" with 50.0 transaction amount on Working Capital loan results an error with the following data:
      | HTTP response code | Error message                                            |
      | 400                | Credit balance refund is allowed only for overpaid loans |
    Examples:
      | status                          |
      | SUBMITTED_AND_PENDING_APPROVAL  |
      | APPROVED                        |
      | ACTIVE                          |
      | CLOSED_OBLIGATIONS_MET          |

  @TestRailId:C76672
  Scenario: Verify that a credit balance refund with zero transaction amount is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Initiating a credit balance refund on "02 January 2026" with 0.0 transaction amount on Working Capital loan results an error with the following data:
      | HTTP response code | Error message                                         |
      | 400                | The parameter `transactionAmount` must be greater than 0 |

  @TestRailId:C76673
  Scenario: Verify that a credit balance refund with a negative transaction amount is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Initiating a credit balance refund on "02 January 2026" with -10.0 transaction amount on Working Capital loan results an error with the following data:
      | HTTP response code | Error message                                         |
      | 400                | The parameter `transactionAmount` must be greater than 0 |

  @TestRailId:C76674
  Scenario: Verify that a credit balance refund with transaction date before disbursal date is rejected
    When Admin sets the business date to "10 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 10 January 2026 | 10 January 2026          | 9000            | 100000       | 18                | 0        |
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
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "05 January 2026"
    And Initiating a credit balance refund on "03 January 2026" with 50.0 transaction amount on Working Capital loan results an error with the following data:
      | HTTP response code | Error message                                         |
      | 400                | Backdated credit balance refund is not allowed        |

  @TestRailId:C76676
  Scenario: Verify that a credit balance refund cannot be repeated after the loan has been closed via full refund
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
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

    Scenario: Verify that a credit balance refund note exceeding 1000 characters is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Initiating a credit balance refund on "02 January 2026" with 50.0 transaction amount and note of length 1001 on Working Capital loan results an error with the following data:
      | HTTP response code | Error message      |
      | 400                | exceeds max length |

  @TestRailId:C76677
  Scenario: Verify that a credit balance refund with a valid note within the allowed length is accepted
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Customer makes credit balance refund on "03 January 2026" with 50.0 transaction amount on Working Capital loan with note "Partial CBR for customer #123"
    Then Working Capital loan status will be "OVERPAID"

  @TestRailId:C76678
  Scenario: Verify that a successful credit balance refund raises the CBR transaction business event
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Customer makes credit balance refund on "03 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital credit balance refund transaction business event is raised with "50.0" amount and reversed "false"

  @TestRailId:C76679
  Scenario: Verify that the transaction template endpoint for creditBalanceRefund returns the current overpayment as expectedAmount
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9250.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin requests the Working Capital loan transaction template for command "creditBalanceRefund"
    Then The Working Capital loan transaction template expectedAmount is "250.00"

  @TestRailId:C76680
  Scenario: Verify that a successful credit balance refund decrements the loan balance overpaymentAmount by the refunded amount
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
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

  @TestRailId:C76681
  Scenario: Verify that the credit balance refund transaction is stored with CREDIT_BALANCE_REFUND type and the provided externalId
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Customer makes credit balance refund on "03 January 2026" with 50.0 transaction amount and externalId "wcl-cbr-ext-001" on Working Capital loan
    Then Verify Working Capital loan credit balance refund transaction has type "CREDIT_BALANCE_REFUND" and externalId "wcl-cbr-ext-001"

  @TestRailId:C76682
  Scenario: Verify that reusing an externalId across two working capital loans for credit balance refund is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
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
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Initiating a credit balance refund on "03 January 2026" with 50.0 transaction amount and externalId "wcl-cbr-shared-001" on Working Capital loan results an error with the following data:
      | HTTP response code | Error message  |
      | 400                | already.exists |


  @TestRailId:C76683
  Scenario: Verify that no accounting journal entries are created for a successful credit balance refund
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Customer makes credit balance refund on "03 January 2026" with 50.0 transaction amount on Working Capital loan
    Then No accounting journal entries are created for the Working Capital loan credit balance refund transaction

  @TestRailId:C76684
  Scenario: Verify that the Working Capital loan retrieval returns overpaymentAmount in the balance payload after credit balance refund
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "03 January 2026"
    And Customer makes credit balance refund on "03 January 2026" with 25.0 transaction amount on Working Capital loan
    Then Working Capital loan balance payload contains the following fields:
      | field               | value  |
      | principalOutstanding| 0.00   |
      | overpaymentAmount   | 75.00  |

  @TestRailId:C76685
  Scenario: Verify that a single overpaying repayment correctly attributes the full principal to totalPaidPrincipal
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
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

