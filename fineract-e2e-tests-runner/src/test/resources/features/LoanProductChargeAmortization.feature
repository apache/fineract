@LoanProductChargeAmortizationFeature
Feature: Charge amortization flag on the loan product

  Scenario: Verify that a loan product without the flag does not amortize charges
    When Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC |                          | false                         | false        |
    Then The loan product has charge amortization "false" and the deferred income liability account mapped "false"

  Scenario Outline: Verify that a progressive loan product with <accounting_rule> accounting accepts an amortized <income_type> charge
    When Admin creates a loan fee with "DISBURSEMENT" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  |                             | <income_type>          |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule    | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | <accounting_rule> | true                     | <deferred_mapped>             | true         |
    Then The loan product has charge amortization "true" and the deferred income liability account mapped "<deferred_mapped>"

    Examples:
      | accounting_rule  | income_type | deferred_mapped |
      | ACCRUAL_PERIODIC | FEE         | true            |
      | ACCRUAL_UPFRONT  | INTEREST    | true            |
      | NONE             | FEE         | false           |

  Scenario: Verify that an amortized charge cannot be attached to a loan product without the flag
    When Admin creates a loan fee with "DISBURSEMENT" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  |                             |                        |
    Then Creating a loan product with the following charge amortization settings results an error:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge | httpCode | errorMessage                                                                     |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | false                    | true                          | true         | 400      | can be attached only to a loan product with charge amortization enabled |

  Scenario Outline: Verify that the flag is rejected on an unsupported loan product: <case>
    Then Creating a loan product with the following charge amortization settings results an error:
      | loanScheduleType    | accountingRule    | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge | httpCode | errorMessage    |
      | <loan_schedule_type> | <accounting_rule> | true                     | <deferred_mapped>             | false        | 400      | <error_message> |

    Examples:
      | case                                       | loan_schedule_type | accounting_rule  | deferred_mapped | error_message                                                     |
      | cumulative loan product                    | CUMULATIVE         | ACCRUAL_PERIODIC | true            | Charge amortization is only supported for Progressive loans      |
      | cash based accounting                      | PROGRESSIVE        | CASH_BASED       | false           | Charge amortization is not supported with cash based accounting  |
      | accrual without deferred income liability  | PROGRESSIVE        | ACCRUAL_PERIODIC | false           | The parameter `deferredIncomeLiabilityAccountId` is mandatory.    |

  Scenario: Verify that the flag can be enabled on an existing loan product with the deferred income liability account
    When Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC |                          | false                         | false        |
    Then Updating the loan product with the following charge amortization settings results an error:
      | accountingRule | enableChargeAmortization | deferredIncomeLiabilityMapped | httpCode | errorMessage                                                                                                 |
      |                | true                     | false                         | 400      | The deferredIncomeLiabilityAccountId mapping is required when charge amortization is enabled with accrual accounting |
    When Admin updates the loan product with the following charge amortization settings:
      | accountingRule | enableChargeAmortization | deferredIncomeLiabilityMapped |
      |                | true                     | true                          |
    Then The loan product has charge amortization "true" and the deferred income liability account mapped "true"
    Then Updating the loan product with the following charge amortization settings results an error:
      | accountingRule | enableChargeAmortization | deferredIncomeLiabilityMapped | httpCode | errorMessage                                                    |
      | CASH_BASED     |                          | false                         | 400      | Charge amortization is not supported with cash based accounting |
    When Admin updates the loan product with the following charge amortization settings:
      | accountingRule | enableChargeAmortization | deferredIncomeLiabilityMapped |
      |                | false                    | false                         |
    Then The loan product has charge amortization "false" and the deferred income liability account mapped "false"

  Scenario: Verify that the flag cannot be disabled while an amortized charge is attached
    When Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               |                             |                        |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | true         |
    Then Updating the loan product with the following charge amortization settings results an error:
      | accountingRule | enableChargeAmortization | deferredIncomeLiabilityMapped | httpCode | errorMessage                                                            |
      |                | false                    | false                         | 400      | can be attached only to a loan product with charge amortization enabled |
    And The loan product has charge amortization "true" and the deferred income liability account mapped "true"

  Scenario: Verify that a charge attached to a loan product without the flag cannot become amortized
    When Admin creates a loan fee with "DISBURSEMENT" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      |                        |                  |                             |                        |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | false                    | false                         | true         |
    Then Amortizing the charge over the full loan term results an error:
      | httpCode | errorMessage                                                                                      |
      | 403      | it is attached to a loan product that does not have charge amortization enabled                   |
    And The charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NONE                   |                  |                             |                        |
