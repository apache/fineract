@LoanChargeAmortizationFeature
Feature: Charge amortization configuration on the loan charge

  Scenario: Verify that the amortization configuration is copied onto the loan charge and frozen
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

  Scenario: Verify that an amortized charge cannot be added to a loan whose product has charge amortization disabled
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

  Scenario: Verify that a non-amortized charge can still be added when charge amortization is disabled
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

  Scenario: Verify that modify loan application does not revalidate existing loan charges against an updated amortized charge definition
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

  Scenario: Verify that modify loan application keeps a frozen amortized loan charge when the charge definition is switched back to NONE
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
