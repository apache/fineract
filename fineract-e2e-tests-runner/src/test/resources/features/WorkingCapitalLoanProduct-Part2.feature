@WorkingCapital
@WorkingCapitalLoanProductFeature
Feature: WorkingCapitalLoanProduct - Part2

  @TestRailId:C76715
  Scenario Outline: Verify WC Loan Product create/update failed with near breach invalid data - UC9
    Then Admin failed to create a new Working Capital Loan Product with breach with field "<wcp_field_name>" invalid data <wcp_invalid_field_value> and got an error <wcp_error_message>
    When Admin creates a new Working Capital Loan Product with breachId
    Then Admin failed to update a new Working Capital Loan Product with breach with field "<wcp_field_name>" invalid data <wcp_invalid_field_value> and got an error <wcp_error_message>
    Then Admin deletes a Working Capital Loan Product
    When Admin deletes WC Breach With Values

    Examples:
      | wcp_field_name | wcp_invalid_field_value | wcp_error_message                                      |
      | nearBreachId   | "0"                     | "Working Capital Near Breach with id 0 was not found." |

  @TestRailId:C80962
  Scenario: Verify WC Loan Product template exposes the 6 advanced payment allocation types
    When Admin retrieves the Working Capital Loan Product template
    Then Working Capital Loan Product template advancedPaymentAllocationTypes contains:
      | DUE_PENALTY          | Due Penalty          |
      | DUE_FEE              | Due Fee              |
      | DUE_PRINCIPAL        | Due Principal        |
      | IN_ADVANCE_PENALTY   | In Advance Penalty   |
      | IN_ADVANCE_FEE       | In Advance Fee       |
      | IN_ADVANCE_PRINCIPAL | In Advance Principal |

  @TestRailId:C80963
  Scenario: Verify WC Loan Product create persists DUE-first then IN_ADVANCE payment allocation order
    When Admin creates a new Working Capital Loan Product with payment allocation order:
      | DUE_PENALTY          |
      | DUE_FEE              |
      | DUE_PRINCIPAL        |
      | IN_ADVANCE_PENALTY   |
      | IN_ADVANCE_FEE       |
      | IN_ADVANCE_PRINCIPAL |
    Then Working Capital Loan Product payment allocation order is:
      | DUE_PENALTY          | 1 |
      | DUE_FEE              | 2 |
      | DUE_PRINCIPAL        | 3 |
      | IN_ADVANCE_PENALTY   | 4 |
      | IN_ADVANCE_FEE       | 5 |
      | IN_ADVANCE_PRINCIPAL | 6 |
    Then Admin deletes a Working Capital Loan Product

  @TestRailId:C80964
  Scenario: Verify WC Loan Product create persists PRINCIPAL-first interleaved payment allocation order
    When Admin creates a new Working Capital Loan Product with payment allocation order:
      | DUE_PRINCIPAL        |
      | IN_ADVANCE_PRINCIPAL |
      | DUE_FEE              |
      | IN_ADVANCE_FEE       |
      | DUE_PENALTY          |
      | IN_ADVANCE_PENALTY   |
    Then Working Capital Loan Product payment allocation order is:
      | DUE_PRINCIPAL        | 1 |
      | IN_ADVANCE_PRINCIPAL | 2 |
      | DUE_FEE              | 3 |
      | IN_ADVANCE_FEE       | 4 |
      | DUE_PENALTY          | 5 |
      | IN_ADVANCE_PENALTY   | 6 |
    Then Admin deletes a Working Capital Loan Product

  @TestRailId:C80965
  Scenario: Verify WC Loan Product update changes the payment allocation order
    When Admin creates a new Working Capital Loan Product with payment allocation order:
      | DUE_PENALTY          |
      | DUE_FEE              |
      | DUE_PRINCIPAL        |
      | IN_ADVANCE_PENALTY   |
      | IN_ADVANCE_FEE       |
      | IN_ADVANCE_PRINCIPAL |
    When Admin updates Working Capital Loan Product payment allocation order:
      | IN_ADVANCE_PRINCIPAL |
      | IN_ADVANCE_FEE       |
      | IN_ADVANCE_PENALTY   |
      | DUE_PRINCIPAL        |
      | DUE_FEE              |
      | DUE_PENALTY          |
    Then Working Capital Loan Product payment allocation order is:
      | IN_ADVANCE_PRINCIPAL | 1 |
      | IN_ADVANCE_FEE       | 2 |
      | IN_ADVANCE_PENALTY   | 3 |
      | DUE_PRINCIPAL        | 4 |
      | DUE_FEE              | 5 |
      | DUE_PENALTY          | 6 |
    Then Admin deletes a Working Capital Loan Product

  @TestRailId:C80966
  Scenario: Verify WC Loan Product create fails when payment allocation rules contain duplicates
    Then Admin failed to create a new Working Capital Loan Product with duplicate payment allocation rules

  @TestRailId:C106771
  Scenario: Verify WC Loan Product create fails when payment allocation rules contain transaction type duplicates
    Then Admin failed to create a new Working Capital Loan Product with duplicate transaction type per payment allocation rules

  @TestRailId:C106772
  Scenario: Verify WC Loan Product update fails when payment allocation rules contain transaction type duplicates
    When Admin creates a new Working Capital Loan Product
    Then Admin failed to update a new Working Capital Loan Product with duplicate transaction type per payment allocation rules
    Then Admin deletes a Working Capital Loan Product

  @TestRailId:C106773
  Scenario: Verify WC Loan Product template exposes advanced payment allocation defined transaction types
    When Admin retrieves the Working Capital Loan Product template
    Then Working Capital Loan Product template advancedPaymentAllocation Transaction Types contains:
      | DEFAULT           |
      | REPAYMENT         |
      | PAYOUT_REFUND     |
      | GOODWILL_CREDIT   |
      | CHARGE_ADJUSTMENT |

  @TestRailId:C106742
  Scenario: Verify WC Loan Product template delinquencyBucketOptions are WORKING_CAPITAL only
    When Admin retrieves the Working Capital Loan Product template
    Then Working Capital Loan Product template delinquencyBucketOptions all have bucketType "WORKING_CAPITAL"
    And Working Capital Loan Product template delinquencyBucketOptions do not contain:
      | BASIC_DELINQUENCY_BUCKET |

  @TestRailId:C106743
  Scenario Outline: Verify WC Loan Product create rejects non-WORKING_CAPITAL delinquency bucket
    Then Admin failed to create a new Working Capital Loan Product with field "<wcp_field_name>" invalid data <wcp_invalid_field_value> and got an error <wcp_error_message>

    Examples:
      | wcp_field_name      | wcp_invalid_field_value    | wcp_error_message                                                                           |
      | delinquencyBucketId | "BASIC_DELINQUENCY_BUCKET" | "The parameter `delinquencyBucketId` must reference a WORKING_CAPITAL delinquency bucket." |

  @TestRailId:C106744
  Scenario Outline: Verify WC Loan Product update rejects non-WORKING_CAPITAL delinquency bucket
    When Admin creates a new Working Capital Loan Product
    Then Admin failed to update a new Working Capital Loan Product field "<wcp_field_name>" with invalid data <wcp_invalid_field_value> and got an error <wcp_error_message>
    Then Admin deletes a Working Capital Loan Product

    Examples:
      | wcp_field_name      | wcp_invalid_field_value    | wcp_error_message                                                                          |
      | delinquencyBucketId | "BASIC_DELINQUENCY_BUCKET" | "The parameter `delinquencyBucketId` must reference a WORKING_CAPITAL delinquency bucket." |

  @TestRailId:C115288
  Scenario Outline: Verify WC Loan Product create rejects unsupported repayment frequency settings
    Then Admin failed to create a new Working Capital Loan Product with field "<wcp_field_name>" invalid data <wcp_invalid_field_value> and got an error <wcp_error_message>

    Examples:
      | wcp_field_name         | wcp_invalid_field_value | wcp_error_message                                              |
      | repaymentFrequencyType | "YEARS"                 | "invalid.period.frequency.type"                                |
      | repaymentEvery         | "0"                     | "The parameter `repaymentEvery` must be greater than 0."       |
      | repaymentEvery         | "-3"                    | "The parameter `repaymentEvery` must be greater than 0."       |
