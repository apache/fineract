@LoanProductChargeAmortizationFeature
Feature: Charge amortization flag on the loan product

  @TestRailId:C123855
  Scenario: Verify loan product charge amortization flag - UC1: product without the flag has charge amortization disabled by default
    When Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC |                          | false                         | false        |
    Then The loan product has charge amortization "false" and the deferred income liability account mapped "false"

  @TestRailId:C123856
  Scenario Outline: Verify loan product charge amortization flag - UC2: progressive product with <accounting_rule> accounting accepts an amortized <income_type> charge
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

  @TestRailId:C123857
  Scenario: Verify loan product charge amortization flag - UC3: amortized charge cannot be attached to a product without the flag
    When Admin creates a loan fee with "DISBURSEMENT" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  |                             |                        |
    Then Creating a loan product with the following charge amortization settings results an error:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge | httpCode | errorMessage                                                                     |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | false                    | true                          | true         | 400      | can be attached only to a loan product with charge amortization enabled |

  @TestRailId:C123858
  Scenario Outline: Verify loan product charge amortization flag - UC4: flag is rejected on an unsupported loan product - <case>
    Then Creating a loan product with the following charge amortization settings results an error:
      | loanScheduleType    | accountingRule    | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge | httpCode | errorMessage    |
      | <loan_schedule_type> | <accounting_rule> | true                     | <deferred_mapped>             | false        | 400      | <error_message> |

    Examples:
      | case                                       | loan_schedule_type | accounting_rule  | deferred_mapped | error_message                                                     |
      | cumulative loan product                    | CUMULATIVE         | ACCRUAL_PERIODIC | true            | Charge amortization is only supported for Progressive loans      |
      | cash based accounting                      | PROGRESSIVE        | CASH_BASED       | false           | Charge amortization is not supported with cash based accounting  |
      | accrual without deferred income liability  | PROGRESSIVE        | ACCRUAL_PERIODIC | false           | The parameter `deferredIncomeLiabilityAccountId` is mandatory.    |

  @TestRailId:C123859
  Scenario: Verify loan product charge amortization flag - UC5: flag can be enabled on an existing product only with the deferred income liability account
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

  @TestRailId:C123860
  Scenario: Verify loan product charge amortization flag - UC6: flag cannot be disabled while an amortized charge is attached
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

  @TestRailId:C123861
  Scenario: Verify loan product charge amortization flag - UC7: charge attached to a product without the flag cannot become amortized
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

  @TestRailId:C123862
  Scenario: Verify loan product charge amortization flag - UC8: amortized charge can be attached on product update only together with the flag
    When Admin creates a loan fee with "DISBURSEMENT" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  |                             |                        |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC |                          | false                         | false        |
    Then Updating the loan product with the following charge amortization settings results an error:
      | accountingRule | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge | httpCode | errorMessage                                                            |
      |                |                          | false                         | true         | 400      | can be attached only to a loan product with charge amortization enabled |
    And The loan product has charge amortization "false" and the deferred income liability account mapped "false"
    When Admin updates the loan product with the following charge amortization settings:
      | accountingRule | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      |                | true                     | true                          | true         |
    Then The loan product has charge amortization "true" and the deferred income liability account mapped "true"
