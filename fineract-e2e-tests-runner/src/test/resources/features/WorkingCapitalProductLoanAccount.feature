@WorkingCapitalLoanAccountFeature
Feature: WorkingCapitalLoanAccount

  @TestRailId:C70250
  Scenario: Create Working Capital Loan account - UC1: Create loan with all fields (LP overridables disabled)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |

  @TestRailId:C70251
  Scenario: Create Working Capital Loan account - UC2: Create loan with mandatory fields only
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null     |

  @TestRailId:C70252
  Scenario: Create Working Capital Loan account - UC3: Create loan with non-default values
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 500.0           | 1000.0       | 2.0               | 5.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 500.0     | 0.0               | 1000.0       | 2.0               | 5.0      |

  @TestRailId:C70253
  Scenario: Create Working Capital Loan account - UC4: With LP overridables disabled/disallowed, loan creation will result an error when trying override values (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    Then Creating a working capital loan with LP overridables disabled and with the following data will result an error:
      | LoanProduct                       | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount | delinquencyBucketId | repaymentEvery | repaymentFrequencyType |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      | 1                   | 30             | DAYS                   |

  @TestRailId:C74453
  Scenario: Create Working Capital Loan account - UC4.1: With LP overridables enabled/allowed, loan creation will override discount value
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount | delinquencyBucketId | repaymentEvery | repaymentFrequencyType |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 2.0               | 60.0     | 1                   | 1              | MONTHS                 |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 2.0               | 60.0     |

  @TestRailId:C74479
  Scenario: Create Working Capital Loan account - UC4.2: With LP overridables disabled/disallowed, loan created with discount amount from loan product level
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                                | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount | delinquencyBucketId | repaymentEvery | repaymentFrequencyType |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               |          | 1                   | 30             | DAYS                   |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 50.0     |

  @TestRailId:C70254
  Scenario: Create Working Capital Loan account - UC5: Create with principal amount greater than WCLP max (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    Then Creating a working capital loan with principal amount greater than Working Capital Loan Product max will result an error:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 1000000.0       | 100.0        | 1.0               | 0.0      |

  @TestRailId:C70255
  Scenario: Create Working Capital Loan account - UC6: Create with principal amount smaller than WCLP min (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    Then Creating a working capital loan with principal amount smaller than Working Capital Loan Product min will result an error:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 1.0             | 100.0        | 1.0               | 0.0      |

  @TestRailId:C70256
  Scenario: Create Working Capital Loan account - UC7: Create loan with mandatory field missing (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    Then Creating a working capital loan with missing mandatory fields will result an error:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          |                 | 100.0        | 1.0               | 0.0      |

  @TestRailId:C70257
  Scenario: Create Working Capital Loan account - UC8: Create loan with multiple mandatory field missing (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    Then Creating a working capital loan with missing mandatory fields will result an error:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        |                 |                          |                 |              |                   | 0.0      |

  @TestRailId:C70258
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC1: Change principal amount (lower)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          | 80.0            |              |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 80.0      | 0.0               | 100.0        | 1.0               | 0.0      |

  @TestRailId:C70259
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC2: Change principal amount (higher)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          | 500.0           |              |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 500.0     | 0.0               | 100.0        | 1.0               | 0.0      |

  @TestRailId:C70260
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC3: Change submittedOnDate
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 20 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-20               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | 15 December 2025 |                          |                 |              |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2025-12-15      | 2026-01-20               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |

  @TestRailId:C70279
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC3.1: Change submittedOnDate after business date results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 20 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-20               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    And Changing submittedOnDate after business date results an error:
      | submittedOnDate | expectedDisbursementDate |
      | 05 January 2026 |                          |

  @TestRailId:C70280
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC3.2: Change submittedOnDate after business date and expectedDisbursementDate in one call results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 20 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-20               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    And Changing submittedOnDate after business date results an error:
      | submittedOnDate | expectedDisbursementDate |
      | 05 January 2026 | 25 January 2026          |

  @TestRailId:C70261
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC4: Change submittedOnDate after expectedDisbursementDate results an error (Negative)
    When Admin sets the business date to "10 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    And Changing submittedOnDate after expectedDisbursementDate results an error:
      | submittedOnDate |
      | 05 January 2026 |

  @TestRailId:C70262
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC5: Change submittedOnDate and expectedDisbursementDate in separate calls
    When Admin sets the business date to "10 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 20 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-20               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 | 25 January 2026          |                 |              |                   |          |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | 05 January 2026 |                          |                 |              |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-05      | 2026-01-25               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |

  @TestRailId:C70263
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC6: Change submittedOnDate and expectedDisbursementDate in one call
    When Admin sets the business date to "10 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 20 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-20               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | 05 January 2026 | 25 January 2026          |                 |              |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-05      | 2026-01-25               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |

  @TestRailId:C70264
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC7: Change multiple parameters
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 | 15 January 2026          | 500.0           | 500.0        | 2.0               | 5.0      |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-15               | Submitted and pending approval | 500.0     | 0.0               | 500.0        | 2.0               | 5.0      |

  @TestRailId:C70265
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC8: Modify by externalId
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin modifies the working capital loan by externalId with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 | 15 January 2026          | 500.0           | 500.0        | 2.0               | 5.0      |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-15               | Submitted and pending approval | 500.0     | 0.0               | 500.0        | 2.0               | 5.0      |

  @TestRailId:C70266
  Scenario: Delete Working Capital Loan account in Submitted and pending approval state - UC1: Delete loan account by loanId
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin deletes the working capital loan account
    Then Working capital loan account deletion was successful

  @TestRailId:C70267
  Scenario: Delete Working Capital Loan account in Submitted and pending approval state - UC2: Delete loan account by externalId
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin deletes the working capital loan account by externalId
    Then Working capital loan account deletion was successful

  @TestRailId:C70268
  Scenario: Modify Working Capital Loan account - UC9: Change expectedDisbursementDate only
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 20 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 | 25 January 2026          |                 |              |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-25               | Submitted and pending approval | 100.0     | 100.0        | 1.0               | 0.0      |

  @TestRailId:C70269
  Scenario: Modify Working Capital Loan account - UC10: Change totalPayment only
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          |                 | 500.0        |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 500.0        | 1.0               | 0.0      |

  @TestRailId:C70270
  Scenario: Modify Working Capital Loan account - UC11: Change periodPaymentRate only
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          |                 |              | 3.0               |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 100.0        | 3.0               | 0.0      |

  @TestRailId:C70271
  Scenario: Modify Working Capital Loan account - UC12: Change discount only
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          |                 |              |                   | 10.0     |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 100.0        | 1.0               | 10.0     |

  @TestRailId:C70272
  Scenario: Modify Working Capital Loan account - UC13: Principal exceeds product max results in an error
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    Then Modifying the working capital loan with principal exceeding product max results in an error:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          | 1000000.0       |              |                   |          |

  @TestRailId:C70273
  Scenario: Modify Working Capital Loan account - UC14: Principal below product min results in an error
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    Then Modifying the working capital loan with principal below product min results in an error:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          | 1.0             |              |                   |          |

  @TestRailId:C70274
  Scenario: Modify Working Capital Loan account - UC15: Empty modification request results in an error
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    Then Modifying the working capital loan with empty request results in an error

  @TestRailId:C70275
  Scenario: Modify Working Capital Loan account - UC16: Future submittedOnDate results in an error
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    Then Modifying the working capital loan with future submittedOnDate results in an error:
      | submittedOnDate | expectedDisbursementDate |
      | 01 January 2027 | 01 January 2027          |

  @TestRailId:C70276
  Scenario: Modify Working Capital Loan account - UC17: Modify non-existent loan ID results in an error (Negative)
    When Admin attempts to modify a non-existent working capital loan
    Then Working capital loan modification fails with a 404 not found error

  @TestRailId:C70277
  Scenario: Modify Working Capital Loan account - UC18: Verify response contains changes for modified fields
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          | 500.0           |              |                   |          |
    Then Working capital loan modification response contains changes for "principalAmount"
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 500.0     | 100.0        | 1.0               | 0.0      |

  @TestRailId:C72337
  Scenario: Approve Working Capital Loan account - UC1: Approve loan in SUBMITTED AND PENDING APPROVAL state with default values
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 0.0      |

  @TestRailId:C72338
  Scenario: Approve Working Capital Loan account - UC2: Approve with modified principal lower than created
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "80" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 80.0              | 100.0        | 1.0               | 0.0      |

  @TestRailId:C72339
  Scenario: Approve Working Capital Loan account - UC3: Approve with principal greater than created amount results an error (negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    And Approval of working capital loan on "01 January 2026" with "150" amount and expected disbursement date on "01 January 2026" results an error with the following data:
      | httpErrorCode | errorMessage                            |
      | 400           | amount.cannot.exceed.proposed.principal |

  @TestRailId:C72340
  Scenario: Approve Working Capital Loan account - UC4: Approve with modified expected disbursement date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "15 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-15               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 0.0      |

  @TestRailId:C72341
  Scenario: Approve Working Capital Loan account - UC5: Approve with past approval date results an error (negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    And Approval of working capital loan on "31 December 2025" with "100" amount and expected disbursement date on "01 January 2026" results an error with the following data:
      | httpErrorCode | errorMessage                    |
      | 400           | cannot.be.before.submittal.date |

  @TestRailId:C72342
  Scenario: Approve Working Capital Loan account - UC6: Reject loan in SUBMITTED AND PENDING APPROVAL state
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin rejects the working capital loan on "01 January 2026"
    Then Working capital loan rejection was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Rejected | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |

  @TestRailId:C72343
  Scenario: Approve Working Capital Loan account - UC7: Undo approval returns loan from APPROVED to SUBMITTED AND PENDING APPROVAL
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 0.0      |
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null     |

  @TestRailId:C72344
  Scenario: Approve Working Capital Loan account - UC8: Re-approve after undo approval
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 0.0      |
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null     |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null     |

  @TestRailId:C72345
  Scenario: Approve Working Capital Loan account - UC9: Approve on working capital loan in APPROVED state results an error (negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 0.0      |
    And Approval of working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" results an error with the following data:
      | httpErrorCode | errorMessage                                                 |
      | 400           | Transition LOAN_APPROVED is not allowed from status APPROVED |

  @TestRailId:C72346
  Scenario: Approve Working Capital Loan account - UC10: Approve on working capital loan in REJECTED state results an error (negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin rejects the working capital loan on "01 January 2026"
    Then Working capital loan rejection was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Rejected | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    And Approval of working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" results an error with the following data:
      | httpErrorCode | errorMessage                                                 |
      | 400           | Transition LOAN_APPROVED is not allowed from status REJECTED |

  @TestRailId:C72347
  Scenario: Approve Working Capital Loan account - UC11: Approve loan in SUBMITTED AND PENDING APPROVAL state with default values by externalId
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin successfully approves the working capital loan by externalId on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 0.0      |


  @TestRailId:C72367
  Scenario: Approve Working Capital Loan account - UC8: Undo approval on already-disbursed loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Verify Working Capital loan disbursement was successful
    Then Undo approval on the working capital loan results an error with the following data:
      | httpErrorCode | errorMessage                                                    |
      | 400           | Transition LOAN_APPROVAL_UNDO is not allowed from status ACTIVE |

  @TestRailId:C72386
  Scenario: Verify WC Loan inherits product delinquencyGraceDays defaults
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "LOAN_CREATION" for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | 01 January 2027 | 01 January 2027          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has delinquencyGraceDays 3 and delinquencyStartType "LOAN_CREATION"

  @TestRailId:C72387
  Scenario: Verify WC Loan overrides product delinquencyGraceDays
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "LOAN_CREATION" for loan test
    And Admin creates a working capital loan with grace days override and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount | delinquencyGraceDays | delinquencyStartType |
      | 01 January 2027 | 01 January 2027          | 100             | 100          | 1                 |          | 7                    | DISBURSEMENT         |
    Then Working capital loan creation was successful
    And Working capital loan account has delinquencyGraceDays 7 and delinquencyStartType "DISBURSEMENT"

  @TestRailId:C72388
  Scenario: Verify WC Loan creation with delinquencyGraceDays 0
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "DISBURSEMENT" for loan test
    And Admin creates a working capital loan with grace days override and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount | delinquencyGraceDays | delinquencyStartType |
      | 01 January 2027 | 01 January 2027          | 100             | 100          | 1                 |          | 0                    | LOAN_CREATION        |
    Then Working capital loan creation was successful
    And Working capital loan account has delinquencyGraceDays 0 and delinquencyStartType "LOAN_CREATION"

  @TestRailId:C72389
  Scenario: Verify WC Loan update delinquencyGraceDays in pending state
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "LOAN_CREATION" for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | 01 January 2027 | 01 January 2027          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    When Admin modifies the working capital loan with grace days:
      | delinquencyGraceDays | delinquencyStartType |
      | 10                   | DISBURSEMENT         |
    Then Working capital loan account has delinquencyGraceDays 10 and delinquencyStartType "DISBURSEMENT"

  @TestRailId:C72390
  Scenario: Verify WC Loan grace days persisted after approval
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 5 and delinquencyStartType "DISBURSEMENT" for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | 01 January 2027 | 01 January 2027          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    When Admin approves the working capital loan on "01 January 2027"
    Then Working capital loan account has delinquencyGraceDays 5 and delinquencyStartType "DISBURSEMENT"

  @TestRailId:C72391
  Scenario: Verify WC Loan creation with negative delinquencyGraceDays results in error
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "LOAN_CREATION" for loan test
    Then Creating a working capital loan with invalid delinquencyGraceDays -1 will result with status code 400

  @TestRailId:C72392
  Scenario: Verify WC Loan creation with invalid delinquencyStartType results in error
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "LOAN_CREATION" for loan test
    Then Creating a working capital loan with invalid delinquencyStartType "INVALID" will result with status code 400

  @TestRailId:C72368
  Scenario: Create Working Capital Loan account - UC13: Attempt to modify loan in DISBURSED state (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0        | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Verify Working Capital loan disbursement was successful
    Then Modifying the working capital loan that is Disbursed in Active state results in an error

  @TestRailId:C72371
  Scenario: Disburse WCL loan account that is not approved failure - UC1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal| totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0              | 100.0        | 1.0               | 0.0      |
    Then Admin fails to disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount because of not approved

  @TestRailId:C72372
  Scenario: Disburse WCL loan account successful use case - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0        | 1.0               | 0.0      |

  @TestRailId:C72373
  Scenario Outline: Disburse WCL loan account with invalid data outcomes with an error - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 10 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-10               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    When Admin sets the business date to "05 January 2026"
    Then Admin successfully approves the working capital loan on "05 January 2026" with "100" amount and expected disbursement date on "10 January 2026"
    Then Admin fails to disburse the Working Capital loan on "<wcp_disburse_date>" with "<wcp_disburse_amount>" EUR transaction amount with invalid data outcomes with error message <wcp_error_message>

    Examples:
      | wcp_disburse_date        | wcp_disburse_amount  | wcp_error_message                                |
      | 05 January 2027          | 100                  | "cannot.be.a.future.date."                       |
      | 05 January 2025          | 100                  | "cannot.be.before.submitted.date."               |
      | 02 January 2026          | 100                  | "cannot.be.before.approval.date."                |
      | 10 January 2026          | 1000                 | "amount.cannot.exceed.approved.principal."       |

  @TestRailId:C72374
  Scenario Outline: Disburse WCL loan account without mandatory data outcomes with an error - UC4
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 10 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-10               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null     |
    When Admin sets the business date to "05 January 2026"
    Then Admin successfully approves the working capital loan on "05 January 2026" with "100" amount and expected disbursement date on "10 January 2026"
    Then Admin fails to disburse the Working Capital loan on <wcp_disburse_date> with "<wcp_disburse_amount>" EUR transaction amount without mandatory data outcomes with error message <wcp_error_message>

    Examples:
      | wcp_disburse_date        | wcp_disburse_amount  | wcp_error_message                                            |
      | ""                       | 100                  | "The parameter `actualDisbursementDate` is mandatory."       |
      | "05 January 2025"        | 0                    | "The parameter `transactionAmount` must be greater than 0."  |

  @TestRailId:C72375
  Scenario: Undo Disbursal of WCL account successful use case - UC5
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 0.0      |

  @TestRailId:C72376
  Scenario: Undo disbursal of WCL account that is submitted or approved is failed - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    Then Admin fails to undo disbursal the Working Capital loan due to loan status "SUBMITTED_AND_PENDING_APPROVAL"
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin fails to undo disbursal the Working Capital loan due to loan status "APPROVED"

  @TestRailId:C72377
  Scenario: Disburse WCL account and undo disbursal via externalId successful use case - UC7
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 0.0      |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan by externalId on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0        | 1.0               | 0.0      |
    Then Admin successfully undo Working Capital disbursal by externalId
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 0.0      |

  @TestRailId:C74471
  Scenario Outline: Verify WC Loan creation with invalid breachId results in error
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    Then Creating a working capital loan with breachId <breach_id> on "01 January 2027" will result with status code <expected_status_code>
    Examples:
      | breach_id           | expected_status_code |
      | 0                   | 400                  |
      | 9223372036854775807 | 403                  |

  @TestRailId:C74472
  Scenario Outline: Verify WC Loan breach override on application depends on product config
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    Then Creating a working capital loan with breach override allowed "<breach_override_allowed>" on "01 January 2027" will result with status code <expected_status_code>
    Examples:
      | breach_override_allowed | expected_status_code |
      | true                    | 200                  |
      | false                   | 400                  |

  @TestRailId:C72393
  Scenario: Discount on WCL account added after disbursement as on the same as disburse date processed successfully -  UC1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0        | 1.0               | null     |
# --- add discount after disbursement on the same disbursement date --- #
    Then Admin successfully update discount with "12" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 112.0     | 100.0             | 100.0        | 1.0               | 12.0     |

  @TestRailId:C72394
  Scenario: Discount update on WCL account on diff from disbursement date outcomes with an error - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null    |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0        | 1.0               | null     |
# --- add discount after disbursement on diff from same disbursement date should outcome with an error --- #
    When Admin sets the business date to "08 January 2026"
    Then Update discount with "10" amount on Working Capital loan account failed due to date diff from disbursement date
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0        | 1.0               | null     |

  @TestRailId:C72395
  Scenario: Discount update on WCL account on the same as disburse date failed as already added discount before while create loan - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 15       |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 15.0     |
    Then Admin failed to approve the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "18" exceeded discount amount
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "14" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 14.0     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "15" exceeded discount amount
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "13" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 113.0     | 100.0             | 100.0        | 1.0               | 13.0     |
    Then Update discount with "10" amount on Working Capital loan account failed due to already added discount before disbursement

  @TestRailId:C72396
  Scenario: Discount update on WCL account on the same as disburse date failed as already added discount before while approve loan - UC4
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "14" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 14.0     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 114.0     | 100.0             | 100.0        | 1.0               | 14.0     |
    Then Update discount with "10" amount on Working Capital loan account failed due to already added discount before disbursement

  @TestRailId:C72397
  Scenario: Discount update od WCL loan account on the same as disburse date failed as already added discount before while disburse loan - UC5
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "13" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 113.0     | 100.0             | 100.0        | 1.0               | 13.0     |
    Then Update discount with "10" amount on Working Capital loan account failed due to already added discount before disbursement

  @TestRailId:C74490
  Scenario: Discount is forbidden to be added after disbursement as discount is set on loan product level with WCLP overrides disallowed - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                                | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 50.0     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 50.0     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Active | 150.0     | 100.0             | 100.0        | 1.0               | 50.0     |
# --- add discount after disbursement is forbidden due to overrides disallowed --- #
    And Update discount with "20" amount on Working Capital loan account failed due to override disallowed by product

  @TestRailId:C74491
  Scenario: Discount is forbidden to be added after disbursement as NO discount is set on loan product level with WCLP overrides disallowed - UC7
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                       | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | null     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name                      | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0        | 1.0               | null     |
# --- add discount after disbursement is forbidden due to overrides disallowed --- #
    And Update discount with "20" amount on Working Capital loan account failed due to override disallowed by product

  @TestRailId:C74492
  Scenario: Discount is allowed to be added after disbursement as discount is set on loan product level with WCLP overrides allowed - UC8
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 50.0     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 50.0     |
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 150.0     | 100.0             | 100.0        | 1.0               | 50.0     |
# --- add discount after disbursement on the same disbursement date --- #
    Then Admin successfully update discount with "12" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 112.0     | 100.0             | 100.0        | 1.0               | 12.0     |

  @TestRailId:C74493
  Scenario: Discount is forbidden to be added after Approve with modified discount amount allowed as discount is set on loan product level with WCLP overrides allowed - UC9
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 50.0     |
    Then Admin failed to approve the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "60" exceeded discount amount
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and "40" discount amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 40.0     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "50" exceeded discount amount
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 140.0     | 100.0             | 100.0        | 1.0               | 40.0     |
# --- add discount after disbursement is forbidden as discount is already added --- #
    Then Update discount with "10" amount on Working Capital loan account failed due to already added discount before disbursement

  @TestRailId:C74494
  Scenario: Discount is forbidden to be added after Disburse with modified discount amount allowed as discount is set on loan product level with WCLP overrides allowed - UC10
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0     | 0.0               | 100.0        | 1.0               | 50.0     |
    Then Admin failed to approve the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" with "60" exceeded discount amount
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Approved | 100.0     | 100.0             | 100.0        | 1.0               | 50.0     |
    Then Admin failed to disburse the working capital loan on "01 January 2026" with "100" amount with "60" exceeded discount amount
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and "45" discount amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Active | 145.0     | 100.0             | 100.0        | 1.0               | 45.0     |
# --- add discount after disbursement is forbidden as discount is already added --- #
    Then Update discount with "10" amount on Working Capital loan account failed due to already added discount before disbursement

