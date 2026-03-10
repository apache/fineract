@WorkingCapitalProductFeature
Feature: WorkingCapitalProduct

  @TestRailId:C70250
  Scenario: Create Working Capital Loan account - UC1: Create loan with all fields (LP overridables disabled)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100       | 100          | 1                 | 0        |

  @TestRailId:C70251
  Scenario: Create Working Capital Loan account - UC2: Create loan with mandatory fields only
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100       | 100          | 1                 | null        |

  @TestRailId:C70252
  Scenario: Create Working Capital Loan account - UC3: Create loan with non-default values
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 500             | 1000         | 2                 | 5        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 500       | 1000         | 2                 | 5        |

  @TestRailId:C70253
  Scenario: Create Working Capital Loan account - UC4: With LP overridables disabled, loan creation will result an error when trying override values (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    Then Creating a working capital loan with LP overridables disabled and with the following data will result an error:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |delinquencyBucketId|repaymentEvery|repaymentFrequencyType|
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |     0    |1                  |30            |DAYS                  |

  @TestRailId:C70254
  Scenario: Create Working Capital Loan account - UC5: Create with principal amount greater than WCLP max (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    Then Creating a working capital loan with principal amount greater than Working Capital Loan Product max will result an error:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 1000000         | 100          | 1                 | 0        |


  @TestRailId:C70255
  Scenario: Create Working Capital Loan account - UC6: Create with principal amount smaller than WCLP min (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    Then Creating a working capital loan with principal amount smaller than Working Capital Loan Product min will result an error:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 1               | 100          | 1                 | 0        |

  @TestRailId:C70256
  Scenario: Create Working Capital Loan account - UC7: Create loan with mandatory field missing (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    Then Creating a working capital loan with missing mandatory fields will result an error:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          |                 | 100          | 1                 | 0        |

  @TestRailId:C70257
  Scenario: Create Working Capital Loan account - UC8: Create loan with multiple mandatory field missing (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    Then Creating a working capital loan with missing mandatory fields will result an error:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        |                 |                          |                 |              |                   | 0        |

  @TestRailId:C70258
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC1: Change principal amount (lower)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100       | 100          | 1                 | 0        |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          | 80              |              |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 80       | 100          | 1                 | 0        |

  @TestRailId:C70259
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC2: Change principal amount (higher)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100       | 100          | 1                 | 0        |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          | 500             |              |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 500       | 100          | 1                 | 0        |

  @TestRailId:C70260
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC3: Change submittedOnDate
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 20 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-20               | Submitted and pending approval | 100       | 100          | 1                 | 0        |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | 15 December 2025 |                          |                 |              |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2025-12-15      | 2026-01-20               | Submitted and pending approval | 100       | 100          | 1                 | 0        |

  @TestRailId:C70279
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC3.1: Change submittedOnDate after business date results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 20 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-20               | Submitted and pending approval | 100       | 100          | 1                 | 0        |
    And Changing submittedOnDate after business date results an error:
      | submittedOnDate | expectedDisbursementDate |
      | 05 January 2026 |                          |

  @TestRailId:C70280
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC3.2: Change submittedOnDate after business date and expectedDisbursementDate in one call results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 20 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-20               | Submitted and pending approval | 100       | 100          | 1                 | 0        |
    And Changing submittedOnDate after business date results an error:
      | submittedOnDate | expectedDisbursementDate |
      | 05 January 2026 | 25 January 2026          |

  @TestRailId:C70261
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC4: Change submittedOnDate after expectedDisbursementDate results an error (Negative)
    When Admin sets the business date to "10 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100       | 100          | 1                 | 0        |
    And Changing submittedOnDate after expectedDisbursementDate results an error:
      | submittedOnDate |
      | 05 January 2026 |

  @TestRailId:C70262
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC5: Change submittedOnDate and expectedDisbursementDate in separate calls
    When Admin sets the business date to "10 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 20 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-20               | Submitted and pending approval | 100       | 100          | 1                 | 0        |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 | 25 January 2026          |                 |              |                   |          |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | 05 January 2026 |                          |                 |              |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-05      | 2026-01-25               | Submitted and pending approval | 100       | 100          | 1                 | 0        |

  @TestRailId:C70263
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC6: Change submittedOnDate and expectedDisbursementDate in one call
    When Admin sets the business date to "10 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 20 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-20               | Submitted and pending approval | 100       | 100          | 1                 | 0        |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | 05 January 2026 | 25 January 2026          |                 |              |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-05      | 2026-01-25               | Submitted and pending approval | 100       | 100          | 1                 | 0        |

  @TestRailId:C70264
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC7: Change multiple parameters
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100       | 100          | 1                 | 0        |
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 | 15 January 2026          | 500             | 500          | 2                 | 5        |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-15               | Submitted and pending approval | 500       | 500          | 2                 | 5        |

  @TestRailId:C70265
  Scenario: Modify Working Capital Loan account in Submitted and pending approval state - UC8: Modify by externalId
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100       | 100          | 1                 | 0        |
    When Admin modifies the working capital loan by externalId with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 | 15 January 2026          | 500             | 500          | 2                 | 5        |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-15               | Submitted and pending approval | 500       | 500          | 2                 | 5        |

  @TestRailId:C70266
  Scenario: Delete Working Capital Loan account in Submitted and pending approval state - UC1: Delete loan account by loanId
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100       | 100          | 1                 | 0        |
    When Admin deletes the working capital loan account
    Then Working capital loan account deletion was successful

  @TestRailId:C70267
  Scenario: Delete Working Capital Loan account in Submitted and pending approval state - UC2: Delete loan account by externalId
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100       | 100          | 1                 | 0        |
    When Admin deletes the working capital loan account by externalId
    Then Working capital loan account deletion was successful

  @TestRailId:C70268
  Scenario: Modify Working Capital Loan account - UC9: Change expectedDisbursementDate only
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 20 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 | 25 January 2026          |                 |              |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-25               | Submitted and pending approval | 100       | 100          | 1                 | 0        |

  @TestRailId:C70269
  Scenario: Modify Working Capital Loan account - UC10: Change totalPayment only
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          |                 | 500          |                   |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100       | 500          | 1                 | 0        |

  @TestRailId:C70270
  Scenario: Modify Working Capital Loan account - UC11: Change periodPaymentRate only
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          |                 |              | 3                 |          |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100       | 100          | 3                 | 0        |

  @TestRailId:C70271
  Scenario: Modify Working Capital Loan account - UC12: Change discount only
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          |                 |              |                   | 10       |
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100       | 100          | 1                 | 10       |

  @TestRailId:C70272
  Scenario: Modify Working Capital Loan account - UC13: Principal exceeds product max results in an error
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    Then Modifying the working capital loan with principal exceeding product max results in an error:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          | 1000000         |              |                   |          |

  @TestRailId:C70273
  Scenario: Modify Working Capital Loan account - UC14: Principal below product min results in an error
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    Then Modifying the working capital loan with principal below product min results in an error:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          | 1               |              |                   |          |

  @TestRailId:C70274
  Scenario: Modify Working Capital Loan account - UC15: Empty modification request results in an error
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    Then Modifying the working capital loan with empty request results in an error

  @TestRailId:C70275
  Scenario: Modify Working Capital Loan account - UC16: Future submittedOnDate results in an error
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
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
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      |                 |                          | 500             |              |                   |          |
    Then Working capital loan modification response contains changes for "principalAmount"
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 500       | 100          | 1                 | 0        |

# TODO implement with approval testcases
  @Skip @TestRailId:tempApprove
  Scenario: Create Working Capital Loan account - UC12: Attempt to modify loan in APPROVED state (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100       | 100          | 1                 | 0        |

    # TODO implement with disbursal testcases
  @Skip @TestRailId:tempDisburse
  Scenario: Create Working Capital Loan account - UC13: Attempt to modify loan in DISBURSED state (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | principal | totalPayment | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100       | 100          | 1                 | 0        |
