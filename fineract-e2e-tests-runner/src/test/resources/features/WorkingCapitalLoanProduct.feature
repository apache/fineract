@WorkingCapitalLoanProductFeature
Feature: WorkingCapitalLoanProduct

  @TestRailId:C70208
  Scenario: Verify Working capital Loan Product create/edit/delete with valid data - happy path - UC1
    When Admin creates a new Working Capital Loan Product
    When Admin updates a Working Capital Loan Product
    Then Admin deletes a Working Capital Loan Product
    Then Admin checks a Working Capital Loan Product is deleted and doesn't exist

  @TestRailId:C70209
  Scenario: Verify Working capital Loan Product create/edit/delete via external-id with valid data - happy path - UC2
    When Admin creates a new Working Capital Loan Product with external-id
    When Admin updates a Working Capital Loan Product via external-id
    Then Admin deletes a Working Capital Loan Product via external-id
    Then Admin checks a Working Capital Loan Product is deleted and doesn't exist via external-id

  @TestRailId:C70210
  Scenario Outline: Verify Working capital Loan Product create with invalid data shall outcome with error - validation check with mandatory fields - UC3
    Then Admin failed to create a new Working Capital Loan Product field "<wcp_field_name_mandatory>" with empty or null mandatory data <wcp_empty_field_value_mandatory>

    Examples:
      | wcp_field_name_mandatory  | wcp_empty_field_value_mandatory |
      | name                      | "null"                          |
      | name                      | ""                              |
      | shortName                 | ""                              |
      | shortName                 | "null"                          |
      | currencyCode              | ""                              |
      | currencyCode              | "null"                          |
      | digitsAfterDecimal        | "null"                          |
      | amortizationType          | "null"                          |
      | npvDayCount               | "null"                          |
      | principal                 | "null"                          |
      | periodPaymentRate         | "null"                          |
      | repaymentFrequencyType    | "null"                          |

  @TestRailId:C70211
  Scenario Outline: Verify Working capital Loan Product create with invalid data shall outcome with error - validation check with max allowed length - UC4
    Then Admin failed to create a new Working Capital Loan Product field "<wcp_field_name_max_length>" with max length data <wcp_invalid_field_value_max_length> while max allowed is <wcp_invalid_field_value_max_allowed_length>

    Examples:
      | wcp_field_name_max_length | wcp_invalid_field_value_max_length | wcp_invalid_field_value_max_allowed_length |
      | name                      | 101                                | 100                                        |
      | shortName                 | 9                                  | 4                                          |
      | description               | 550                                | 500                                        |
      | currencyCode              | 4                                  | 3                                          |

  @TestRailId:C70212
  Scenario Outline: Verify Working capital Loan Product update with invalid data shall outcome with error - validation check with max allowed length - UC5
    Then Admin failed to update a new Working Capital Loan Product field "<wcp_field_name_max_length>" with max length data <wcp_invalid_field_value_max_length> while max allowed is <wcp_invalid_field_value_max_allowed_length>

    Examples:
      | wcp_field_name_max_length | wcp_invalid_field_value_max_length | wcp_invalid_field_value_max_allowed_length |
      | name                      | 111                                | 100                                        |
      | shortName                 | 5                                  | 4                                          |
      | description               | 600                                | 500                                        |
      | currencyCode              | 10                                 | 3                                          |

  @TestRailId:C70213
  Scenario Outline: Verify Working capital Loan Product create with invalid data shall outcome with error - validation check with zero values - UC6
    Then Admin failed to create a new Working Capital Loan Product field "<wcp_field_name_zero_value>" with zero incorrect value

    Examples:
      | wcp_field_name_zero_value |
      | npvDayCount               |
      | principal                 |
      | minPrincipal              |
      | maxPrincipal              |
      | delinquencyBucketId       |

  @TestRailId:C70214
  Scenario Outline: Verify Working capital Loan Product update with invalid data shall outcome with error - validation check with zero values - UC7
    Then Admin failed to update a new Working Capital Loan Product field "<wcp_field_name_zero_value>" with zero incorrect value

    Examples:
      | wcp_field_name_zero_value |
      | npvDayCount               |
      | principal                 |
      | minPrincipal              |
      | maxPrincipal              |
      | delinquencyBucketId       |

  @TestRailId:C70215
  Scenario Outline: Verify Working capital Loan Product create with invalid data shall outcome with error - validation check with diff values - U8
    Then Admin failed to create a new Working Capital Loan Product field "<wcp_field_name>" with invalid data <wcp_invalid_field_value> and got an error <wcp_error_message>

    Examples:
      | wcp_field_name           | wcp_invalid_field_value  | wcp_error_message                                                                          |
      | digitsAfterDecimal       |  "25"                    | "The parameter `digitsAfterDecimal` must be between 0 and 6."                              |
      | inMultiplesOf            |  "-1"                    | "The parameter `inMultiplesOf` must be zero or greater."                                   |
      | periodPaymentRate        |  "-1"                    | "The parameter `periodPaymentRate` must be greater than or equal to 0."                    |
      | locale                   |  "null"                  | "The parameter `digitsAfterDecimal` requires a `locale` parameter to be passed with it."   |
      | locale                   |  ""                      | "The parameter `locale` is invalid. It cannot be blank."                                   |

  @TestRailId:C70216
  Scenario Outline: Verify Working capital Loan Product update with invalid data shall outcome with error - validation check with diff values - UC9
    Then Admin failed to update a new Working Capital Loan Product field "<wcp_field_name>" with invalid data <wcp_invalid_field_value> and got an error <wcp_error_message>

    Examples:
      | wcp_field_name           | wcp_invalid_field_value  | wcp_error_message                                                                          |
      | digitsAfterDecimal       |  "25"                    | "The parameter `digitsAfterDecimal` must be between 0 and 6."                              |
      | inMultiplesOf            |  "-1"                    | "The parameter `inMultiplesOf` must be zero or greater."                                   |
      | periodPaymentRate        |  "-1"                    | "The parameter `periodPaymentRate` must be greater than or equal to 0."                    |

  @TestRailId:C70217
  Scenario: Verify Working capital Loan Product create with invalid data shall outcome with error - validation check with number of payment allocation rules - UC10
    Then Admin failed to create a new Working Capital Loan Product with invalid number of payment allocation rules

  @TestRailId:C70218
  Scenario: Verify Working capital Loan Product create with invalid data shall outcome with error - validation check with payment allocation rules - UC11
    Then Admin failed to create a new Working Capital Loan Product with invalid value of payment allocation rules

  @TestRailId:C70219
  Scenario: Verify Working capital Loan Product update with invalid data shall outcome with error - validation check with number of payment allocation rules - UC12
    Then Admin failed to update a new Working Capital Loan Product with invalid number of payment allocation rules

  @TestRailId:C70220
  Scenario: Verify Working capital Loan Product update with invalid data shall outcome with error - validation check with payment allocation rules - UC13
    Then Admin failed to update a new Working Capital Loan Product with invalid value of payment allocation rules

  @TestRailId:C70221
  Scenario Outline: Verify Working capital Loan Product delete with invalid data shall outcome with error - validation check with id - UC14
    Then Admin failed to delete a Working Capital Loan Product with id <wcp_field_name_incorrect_value> that doesn't exist
    Examples:
      | wcp_field_name_incorrect_value |
      | 103284                         |
      | 0                              |

  @TestRailId:C70222
  Scenario Outline: Verify Working capital Loan Product retrieve with invalid data shall outcome with error - validation check with id - UC15
    Then Admin failed to retrieve a Working Capital Loan Product with id <wcp_field_name_incorrect_value> that doesn't exist
    Examples:
      | wcp_field_name_incorrect_value |
      | 565465                         |
      | 0                              |
