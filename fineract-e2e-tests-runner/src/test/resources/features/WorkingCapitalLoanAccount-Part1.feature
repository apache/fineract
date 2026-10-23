@WorkingCapital
@WorkingCapitalLoanAccountFeature
Feature: WorkingCapitalLoanAccount - Part1

  @TestRailId:C70250
  Scenario: Create Working Capital Loan account - UC1: Create loan with all fields (LP overridables disabled)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |

  @TestRailId:C70251
  Scenario: Create Working Capital Loan account - UC2: Create loan with mandatory fields only
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null     |

  @TestRailId:C70252
  Scenario: Create Working Capital Loan account - UC3: Create loan with non-default values
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 500.0           | 1000.0             | 2.0               | 5.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 500.0             | 0.0               | 1000.0             | 2.0               | 5.0              |

  @TestRailId:C70253
  Scenario: Create Working Capital Loan account - UC4: With LP overridable disabled/disallowed, loan creation will result an error when trying override values (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    Then Creating a working capital loan with LP overridable disabled and with the following data will result an error:
      | LoanProduct                       | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount | delinquencyBucketId   | repaymentEvery | repaymentFrequencyType |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      | WC_DELINQUENCY_BUCKET | 30             | DAYS                   |

  @TestRailId:C74453
  Scenario: Create Working Capital Loan account - UC4.1: With LP overridable enabled/allowed, loan creation will override discount value
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount | delinquencyBucketId   | repaymentEvery | repaymentFrequencyType |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 2.0               | 60.0     | WC_DELINQUENCY_BUCKET | 1              | MONTHS                 |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 2.0               | 60.0             |

  @TestRailId:C74479
  Scenario: Create Working Capital Loan account - UC4.2: With LP overridable disabled/disallowed, loan created with discount amount from loan product level
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                                | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount | delinquencyBucketId   | repaymentEvery | repaymentFrequencyType |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               |          | WC_DELINQUENCY_BUCKET | 30             | DAYS                   |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name                               | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 50.0             |

  @TestRailId:C70254
  Scenario: Create Working Capital Loan account - UC5: Create with principal amount greater than WCLP max (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    Then Creating a working capital loan with principal amount greater than Working Capital Loan Product max will result an error:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 1000000.0       | 100.0              | 1.0               | 0.0      |

  @TestRailId:C70255
  Scenario: Create Working Capital Loan account - UC6: Create with principal amount smaller than WCLP min (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    Then Creating a working capital loan with principal amount smaller than Working Capital Loan Product min will result an error:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 1.0             | 100.0              | 1.0               | 0.0      |

  @TestRailId:C70256
  Scenario: Create Working Capital Loan account - UC7: Create loan with mandatory field missing (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    Then Creating a working capital loan with missing mandatory fields will result an error:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          |                 | 100.0              | 1.0               | 0.0      |

  @TestRailId:C70257
  Scenario: Create Working Capital Loan account - UC8: Create loan with multiple mandatory field missing (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    Then Creating a working capital loan with missing mandatory fields will result an error:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        |                 |                          |                 |                    |                   | 0.0      |

  @TestRailId:C70258
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC1: Change principal amount (lower)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 |                          | 80.0            |                    |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 80.0              | 0.0               | 100.0              | 1.0               | 0.0              |

  @TestRailId:C70259
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC2: Change principal amount (higher)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 |                          | 200.0           |                    |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 200.0             | 0.0               | 100.0              | 1.0               | 0.0              |

  @TestRailId:C70260
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC3: Change submittedOnDate
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 20 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-20               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 15 December 2025 |                          |                 |                    |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2025-12-15      | 2026-01-20               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |

  @TestRailId:C70279
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC3.1: Change submittedOnDate after business date results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 20 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-20               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    And Changing submittedOnDate after business date results an error:
      | submittedOnDate | expectedDisbursementDate |
      | 05 January 2026 |                          |

  @TestRailId:C70280
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC3.2: Change submittedOnDate after business date and expectedDisbursementDate in one call results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 20 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-20               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    And Changing submittedOnDate after business date results an error:
      | submittedOnDate | expectedDisbursementDate |
      | 05 January 2026 | 25 January 2026          |

  @TestRailId:C70261
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC4: Change submittedOnDate after expectedDisbursementDate results an error (Negative)
    When Admin sets the business date to "10 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    And Changing submittedOnDate after expectedDisbursementDate results an error:
      | submittedOnDate |
      | 05 January 2026 |

  @TestRailId:C70262
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC5: Change submittedOnDate and expectedDisbursementDate in separate calls
    When Admin sets the business date to "10 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 20 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-20               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 | 25 January 2026          |                 |                    |                   |          |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 05 January 2026 |                          |                 |                    |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-05      | 2026-01-25               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |

  @TestRailId:C70263
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC6: Change submittedOnDate and expectedDisbursementDate in one call
    When Admin sets the business date to "10 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 20 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-20               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 05 January 2026 | 25 January 2026          |                 |                    |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-05      | 2026-01-25               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |

  @TestRailId:C70264
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC7: Change multiple parameters
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 | 15 January 2026          | 500.0           | 500.0              | 2.0               | 5.0      |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-15               | Submitted and pending approval | 500.0             | 0.0               | 500.0              | 2.0               | 5.0              |

  @TestRailId:C70265
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC8: Modify by externalId
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin modifies the working capital loan by externalId with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 | 15 January 2026          | 500.0           | 500.0              | 2.0               | 5.0      |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-15               | Submitted and pending approval | 500.0             | 0.0               | 500.0              | 2.0               | 5.0              |

  @TestRailId:C70266
  Scenario: Delete Working Capital Loan account in Submitted and pending approval state - UC1: Delete loan account by loanId
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin deletes the working capital loan account
    Then Working capital loan account deletion was successful

  @TestRailId:C70267
  Scenario: Delete Working Capital Loan account in Submitted and pending approval state - UC2: Delete loan account by externalId
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin deletes the working capital loan account by externalId
    Then Working capital loan account deletion was successful

  @TestRailId:C70268
  Scenario: Modify Working Capital Loan account - UC9: Change expectedDisbursementDate only
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 20 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 | 25 January 2026          |                 |                    |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-25               | Submitted and pending approval | 100.0             | 100.0              | 1.0               | 0.0              |

  @TestRailId:C70269
  Scenario: Modify Working Capital Loan account - UC10: Change totalPaymentVolume only
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 |                          |                 | 500.0              |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 500.0              | 1.0               | 0.0              |

  @TestRailId:C70270
  Scenario: Modify Working Capital Loan account - UC11: Change periodPaymentRate only
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 |                          |                 |                    | 3.0               |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 100.0              | 3.0               | 0.0              |

  @TestRailId:C70271
  Scenario: Modify Working Capital Loan account - UC12: Change discount only
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 |                          |                 |                    |                   | 10.0     |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 100.0              | 1.0               | 10.0             |

  @TestRailId:C70272
  Scenario: Modify Working Capital Loan account - UC13: Principal exceeds product max results in an error
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    Then Modifying the working capital loan with principal exceeding product max results in an error:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 |                          | 1000000.0       |                    |                   |          |

  @TestRailId:C70273
  Scenario: Modify Working Capital Loan account - UC14: Principal below product min results in an error
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    Then Modifying the working capital loan with principal below product min results in an error:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 |                          | 1.0             |                    |                   |          |

  @TestRailId:C70274
  Scenario: Modify Working Capital Loan account - UC15: Empty modification request results in an error
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    Then Modifying the working capital loan with empty request results in an error

  @TestRailId:C70275
  Scenario: Modify Working Capital Loan account - UC16: Future submittedOnDate results in an error
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
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
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 |                          | 200.0           |                    |                   |          |
    Then Working capital loan modification response contains changes for "principalAmount"
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 200.0             | 100.0              | 1.0               | 0.0              |

  @TestRailId:C72337
  Scenario: Approve Working Capital Loan account - UC1: Approve loan in SUBMITTED AND PENDING APPROVAL state with default values
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 0.0              | null             |

  @TestRailId:C72338
  Scenario: Approve Working Capital Loan account - UC2: Approve with modified principal lower than created
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin successfully approves the working capital loan on "01 January 2026" with "80" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 80.0              | 100.0              | 1.0               | 0.0              | null             |

  @TestRailId:C72339
  Scenario: Approve Working Capital Loan account - UC3: Approve with principal greater than created amount results an error (negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    And Approval of working capital loan on "01 January 2026" with "150" amount and expected disbursement date on "01 January 2026" results an error with the following data:
      | httpErrorCode | errorMessage                            |
      | 400           | amount.cannot.exceed.proposed.principal |

  @TestRailId:C72340
  Scenario: Approve Working Capital Loan account - UC4: Approve with modified expected disbursement date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "15 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved |
      | WCLP         | 2026-01-01      | 2026-01-15               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 0.0              | null             |

  @TestRailId:C72341
  Scenario: Approve Working Capital Loan account - UC5: Approve with past approval date results an error (negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    And Approval of working capital loan on "31 December 2025" with "100" amount and expected disbursement date on "01 January 2026" results an error with the following data:
      | httpErrorCode | errorMessage                    |
      | 400           | cannot.be.before.submittal.date |

  @TestRailId:C72342
  Scenario: Approve Working Capital Loan account - UC6: Reject loan in SUBMITTED AND PENDING APPROVAL state
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin rejects the working capital loan on "01 January 2026"
    Then Working capital loan rejection was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Rejected | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |

  @TestRailId:C72343
  Scenario: Approve Working Capital Loan account - UC7: Undo approval returns loan from APPROVED to SUBMITTED AND PENDING APPROVAL
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 0.0              | null             |
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null     |

  @TestRailId:C72344
  Scenario: Approve Working Capital Loan account - UC8: Re-approve after undo approval
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 0.0              | null             |
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 0.0              | null             |

  @TestRailId:C72345
  Scenario: Approve Working Capital Loan account - UC9: Approve on working capital loan in APPROVED state results an error (negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 0.0              | null             |
    And Approval of working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" results an error with the following data:
      | httpErrorCode | errorMessage                                                 |
      | 400           | Transition LOAN_APPROVED is not allowed from status APPROVED |

  @TestRailId:C72346
  Scenario: Approve Working Capital Loan account - UC10: Approve on working capital loan in REJECTED state results an error (negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin rejects the working capital loan on "01 January 2026"
    Then Working capital loan rejection was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Rejected | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    And Approval of working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026" results an error with the following data:
      | httpErrorCode | errorMessage                                                 |
      | 400           | Transition LOAN_APPROVED is not allowed from status REJECTED |

  @TestRailId:C72347
  Scenario: Approve Working Capital Loan account - UC11: Approve loan in SUBMITTED AND PENDING APPROVAL state with default values by externalId
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin successfully approves the working capital loan by externalId on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 0.0              | null             |

  @TestRailId:C72367
  Scenario: Approve Working Capital Loan account - UC8: Undo approval on already-disbursed loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
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
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2027 | 01 January 2027          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has delinquencyGraceDays 3 and delinquencyStartType "LOAN_CREATION"

  @TestRailId:C72387
  Scenario: Verify WC Loan overrides product delinquencyGraceDays
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "LOAN_CREATION" for loan test
    And Admin creates a working capital loan with grace days override and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount | delinquencyGraceDays | delinquencyStartType |
      | 01 January 2027 | 01 January 2027          | 100             | 100                | 1                 |          | 7                    | DISBURSEMENT         |
    Then Working capital loan creation was successful
    And Working capital loan account has delinquencyGraceDays 7 and delinquencyStartType "DISBURSEMENT"

  @TestRailId:C72388
  Scenario: Verify WC Loan creation with delinquencyGraceDays 0
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "DISBURSEMENT" for loan test
    And Admin creates a working capital loan with grace days override and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount | delinquencyGraceDays | delinquencyStartType |
      | 01 January 2027 | 01 January 2027          | 100             | 100                | 1                 |          | 0                    | LOAN_CREATION        |
    Then Working capital loan creation was successful
    And Working capital loan account has delinquencyGraceDays 0 and delinquencyStartType "LOAN_CREATION"

  @TestRailId:C72389
  Scenario: Verify WC Loan update delinquencyGraceDays in pending state
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "LOAN_CREATION" for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2027 | 01 January 2027          | 100             | 100                | 1                 |          |
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
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2027 | 01 January 2027          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    When Admin approves the working capital loan on "01 January 2027"
    Then Working capital loan account has delinquencyGraceDays 5 and delinquencyStartType "DISBURSEMENT"

  @TestRailId:C72391
  Scenario: Verify WC Loan creation with negative delinquencyGraceDays results in error
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "LOAN_CREATION" for loan test
    Then Creating a working capital loan with invalid delinquencyGraceDays -1 will result with status code 400
