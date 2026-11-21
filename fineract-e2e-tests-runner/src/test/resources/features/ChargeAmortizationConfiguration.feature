@ChargeAmortizationConfigurationFeature
Feature: Charge amortization configuration on the charge definition

  Scenario: Verify that a loan fee without amortization configuration is not amortized
    When Admin creates a loan fee with "DISBURSEMENT" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      |                        |                  |                             |                        |
    Then The charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NONE                   |                  |                             |                        |

  Scenario Outline: Verify that a <charge_time_type> loan fee amortized over the full loan term defaults to flat fee income
    When Admin creates a loan fee with "<charge_time_type>" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  |                             |                        |
    Then The charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | FLAT                        | FEE                    |

    Examples:
      | charge_time_type     |
      | DISBURSEMENT         |
      | TRANCHE_DISBURSEMENT |
      | SPECIFIED_DUE_DATE   |

  Scenario: Verify that a loan fee can be amortized over a number of days as interest income
    When Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |
    Then The charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |

  Scenario: Verify that the effective rate method is accepted with the full loan term
    When Admin creates a loan fee with "DISBURSEMENT" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | EFFECTIVE_RATE              |                        |
    Then The charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | EFFECTIVE_RATE              | FEE                    |

  Scenario: Verify that the amortization configuration of a charge can be changed and removed
    When Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      |                        |                  |                             |                        |
    And Admin updates the amortization configuration of the charge to:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 45               |                             | INTEREST               |
    Then The charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 45               | FLAT                        | INTEREST               |
    When Admin updates the amortization configuration of the charge to:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  |                             |                        |
    Then The charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | FLAT                        | INTEREST               |
    When Admin updates the amortization configuration of the charge to:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NONE                   |                  |                             |                        |
    Then The charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NONE                   |                  |                             |                        |

  Scenario Outline: Verify that an invalid amortization configuration is rejected: <case>
    Then Creating a loan charge with "<charge_time_type>" charge time type, penalty "<penalty>" and the following amortization configuration results an error:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType | httpCode | errorMessage    |
      | <period_type>          | <days>           | <calculation_type>          | <income_type>          | 400      | <error_message> |

    Examples:
      | case                                | charge_time_type   | penalty | period_type    | days | calculation_type | income_type | error_message                                                                                    |
      | number of days without days         | DISBURSEMENT       | false   | NUMBER_OF_DAYS |      |                  |             | The parameter `amortizationDays` is mandatory.                                                   |
      | number of days with zero days       | DISBURSEMENT       | false   | NUMBER_OF_DAYS | 0    |                  |             | The parameter `amortizationDays` must be greater than 0.                                         |
      | days with the full loan term        | DISBURSEMENT       | false   | FULL_LOAN_TERM | 30   |                  |             | Amortization days can be set only when the amortization period type is NUMBER_OF_DAYS            |
      | effective rate with number of days  | DISBURSEMENT       | false   | NUMBER_OF_DAYS | 30   | EFFECTIVE_RATE   |             | Effective rate amortization is supported only with the FULL_LOAN_TERM amortization period type   |
      | calculation type without amortizing | DISBURSEMENT       | false   | NONE           |      | FLAT             |             | Amortization calculation type can be set only when the amortization period type is not NONE      |
      | income type without amortizing      | DISBURSEMENT       | false   |                |      |                  | INTEREST    | Amortization income type can be set only when the amortization period type is not NONE           |
      | penalty                             | SPECIFIED_DUE_DATE | true    | FULL_LOAN_TERM |      |                  |             | Penalties cannot be amortized                                                                    |
      | installment fee                     | INSTALLMENT_FEE    | false   | FULL_LOAN_TERM |      |                  |             | Only Disbursement, Tranche Disbursement and Specified due date charges can be amortized          |
      | unknown period type                 | DISBURSEMENT       | false   | WEEKLY         |      |                  |             | The parameter `amortizationPeriodType` must be one of [ NONE, FULL_LOAN_TERM, NUMBER_OF_DAYS ] . |

  Scenario: Verify that an amortized fee cannot be turned into a penalty
    When Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  |                             |                        |
    Then Turning the charge into a penalty results an error:
      | httpCode | errorMessage                  |
      | 400      | Penalties cannot be amortized |
    And The charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | FLAT                        | FEE                    |

  Scenario: Verify that the charge template lists the amortization options
    Then The loan charge template lists the following amortization options:
      | amortizationPeriodTypeOptions        | amortizationCalculationTypeOptions | amortizationIncomeTypeOptions |
      | NONE, FULL_LOAN_TERM, NUMBER_OF_DAYS | FLAT, EFFECTIVE_RATE               | FEE, INTEREST                 |
