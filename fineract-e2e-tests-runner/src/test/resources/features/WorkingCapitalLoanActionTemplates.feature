@WorkingCapitalLoanActionTemplatesFeature
Feature: Working Capital Loan Action Templates

  @TestRailId:C72350
  Scenario: Verify Working Capital Loan approve template returns correct defaults for submitted loan - UC1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 15 January 2026          | 500             | 500          | 1                 | 0        |
    Then Working capital loan creation was successful
    When Admin retrieves the working capital loan action template with templateType "approve"
    Then The working capital loan approve template has the following data:
      | approvalAmount | approvalDate | expectedDisbursementDate |
      | 500            | 2026-01-15   | 2026-01-15               |

  @TestRailId:C72351
  Scenario: Verify Working Capital Loan approve template reflects modified principal and disbursement date - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 15 January 2026          | 500             | 500          | 1                 | 0        |
    Then Working capital loan creation was successful
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 | 25 January 2026          | 300             |              |                   |          |
    When Admin retrieves the working capital loan action template with templateType "approve"
    Then The working capital loan approve template has the following data:
      | approvalAmount | approvalDate | expectedDisbursementDate |
      | 300            | 2026-01-25   | 2026-01-25               |

  @TestRailId:C72352
  Scenario: Verify Working Capital Loan approve template reflects modified principal only - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 15 January 2026          | 500             | 500          | 1                 | 0        |
    Then Working capital loan creation was successful
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          | 200             |              |                   |          |
    When Admin retrieves the working capital loan action template with templateType "approve"
    Then The working capital loan approve template has the following data:
      | approvalAmount | expectedDisbursementDate |
      | 200            | 2026-01-15               |

  @TestRailId:C72353
  Scenario: Verify Working Capital Loan approve template reflects modified expected disbursement date only - UC4
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 15 January 2026          | 500             | 500          | 1                 | 0        |
    Then Working capital loan creation was successful
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 | 25 January 2026          |                 |              |                   |          |
    When Admin retrieves the working capital loan action template with templateType "approve"
    Then The working capital loan approve template has the following data:
      | approvalAmount | approvalDate | expectedDisbursementDate |
      | 500            | 2026-01-25   | 2026-01-25               |

  @TestRailId:C72354
  Scenario: Verify Working Capital Loan approve template returns correct approval amount - UC5
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    When Admin retrieves the working capital loan action template with templateType "approve"
    Then The working capital loan approve template has the following data:
      | approvalAmount |
      | 100            |

  @TestRailId:C72355
  Scenario: Verify Working Capital Loan disburse template returns correct defaults for approved loan - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 15 January 2026          | 500             | 500          | 1                 | 0        |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "500" amount and expected disbursement date on "15 January 2026"
    When Admin retrieves the working capital loan action template with templateType "disburse"
    Then The working capital loan disburse template has the following data:
      | expectedAmount | expectedDisbursementDate | paymentTypeOptionsPresent |
      | 500            | 2026-01-15               | true                      |

  @TestRailId:C72356
  Scenario: Verify Working Capital Loan disburse template reflects reduced approved principal - UC7
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 15 January 2026          | 500             | 500          | 1                 | 0        |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "300" amount and expected disbursement date on "15 January 2026"
    When Admin retrieves the working capital loan action template with templateType "disburse"
    Then The working capital loan disburse template has the following data:
      | expectedAmount | expectedDisbursementDate | paymentTypeOptionsPresent |
      | 300            | 2026-01-15               | true                      |

  @TestRailId:C72357
  Scenario: Verify Working Capital Loan action template with invalid templateType results in an error - UC8
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    Then Retrieving WC loan action template with invalid templateType "invalid" results in an error

  @TestRailId:C72358
  Scenario: Verify Working Capital Loan action template without templateType results in an error - UC9
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    Then Retrieving WC loan action template without templateType results in an error

  @TestRailId:C72359
  Scenario Outline: Verify Working Capital Loan action template for non-existent loan returns 404 - UC10
    Then Retrieving WC loan action template for non-existent loan id <loanId> results in a 404 error
    Examples:
      | loanId |
      | 999999 |
      | 0      |
