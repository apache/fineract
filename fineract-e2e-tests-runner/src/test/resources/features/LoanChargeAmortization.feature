@LoanChargeAmortizationFeature
Feature: Charge amortization configuration on the loan charge

  @TestRailId:C127968
  Scenario: Verify loan charge amortization configuration - UC1: configuration is copied onto the loan charge and frozen
    When Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product and adds the amortized charge
    Then The loan charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |
    When Admin updates the amortization configuration of the charge to:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | EFFECTIVE_RATE              | FEE                    |
    Then The charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | EFFECTIVE_RATE              | FEE                    |
    And The loan charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |

  @TestRailId:C127969
  Scenario: Verify loan charge amortization configuration - UC2: amortized charge cannot be added to a loan whose product has the flag disabled
    When Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  |                             |                        |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | false                    | false                         | false        |
    And Admin creates a client with random data
    Then Adding the amortized charge to a new Loan with the charge amortization product results an error:
      | httpCode | errorMessage                                                              |
      | 403      | can be added only to a loan whose product has charge amortization enabled |

  @TestRailId:C127970
  Scenario: Verify loan charge amortization configuration - UC3: non-amortized charge can still be added when the flag is disabled
    When Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      |                        |                  |                             |                        |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | false                    | false                         | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product and adds the amortized charge
    Then The loan charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NONE                   |                  |                             |                        |

  @TestRailId:C127971
  Scenario: Verify loan charge amortization configuration - UC4: modify loan application does not revalidate existing loan charges against an updated charge definition
    When Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      |                        |                  |                             |                        |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | false                    | false                         | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product and adds the amortized charge
    Then The loan charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NONE                   |                  |                             |                        |
    When Admin updates the amortization configuration of the charge to:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  |                             |                        |
    And Admin modifies the loan resending its existing charges
    Then The loan charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NONE                   |                  |                             |                        |

  @TestRailId:C127972
  Scenario: Verify loan charge amortization configuration - UC5: modify loan application keeps the frozen configuration when the charge definition is switched back to NONE
    When Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product and adds the amortized charge
    Then The loan charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |
    When Admin updates the amortization configuration of the charge to:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NONE                   |                  |                             |                        |
    Then The charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NONE                   |                  |                             |                        |
    When Admin modifies the loan resending its existing charges
    Then The loan charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |

  @TestRailId:C127973
  Scenario: Verify loan charge amortization configuration - UC6: default calculation and income types are copied onto the loan charge
    When Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  |                             |                        |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product and adds the amortized charge
    Then The loan charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | FLAT                        | FEE                    |

  @TestRailId:C127974
  Scenario: Verify loan charge amortization configuration - UC7: amortized charge in the loan application is copied and validated against the flag
    When Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product and the amortized charge in the loan application
    Then The loan charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |
    When Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | false                    | false                         | false        |
    Then Creating a new Loan with charge amortization product and the amortized charge in the loan application results an error:
      | httpCode | errorMessage                                                              |
      | 400      | can be added only to a loan whose product has charge amortization enabled |

  @TestRailId:C127975
  Scenario: Verify loan charge amortization configuration - UC8: amortized charge can be added to a disbursed loan and stays frozen
    When Admin sets the business date to "01 January 2024"
    And Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product on "01 January 2024"
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin disburses the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin adds the amortized charge to the loan on "15 January 2024"
    Then The loan charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |
