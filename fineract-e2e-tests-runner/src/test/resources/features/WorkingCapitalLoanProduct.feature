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
    When Admin creates a new Working Capital Loan Product
    Then Admin failed to update a new Working Capital Loan Product field "<wcp_field_name_max_length>" with max length data <wcp_invalid_field_value_max_length> while max allowed is <wcp_invalid_field_value_max_allowed_length>
    Then Admin deletes a Working Capital Loan Product

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
    When Admin creates a new Working Capital Loan Product
    Then Admin failed to update a new Working Capital Loan Product field "<wcp_field_name_zero_value>" with zero incorrect value
    Then Admin deletes a Working Capital Loan Product

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
      | discount                 |  "-1"                    | "The parameter `discount` must be greater than or equal to 0."                             |
      | principal                |  "1"                     | "Failed data validation due to: must.be.greater.than.or.equal.to.min."                     |
      | principal                |  "1000000"               | "Failed data validation due to: must.be.less.than.or.equal.to.max."                        |
      | periodPaymentRate        |  "-1"                    | "The parameter `periodPaymentRate` must be greater than or equal to 0."                    |
      | locale                   |  "null"                  | "The parameter `digitsAfterDecimal` requires a `locale` parameter to be passed with it."   |
      | locale                   |  ""                      | "The parameter `locale` is invalid. It cannot be blank."                                   |

  @TestRailId:C70216
  Scenario Outline: Verify Working capital Loan Product update with invalid data shall outcome with error - validation check with diff values - UC9
    When Admin creates a new Working Capital Loan Product
    Then Admin failed to update a new Working Capital Loan Product field "<wcp_field_name>" with invalid data <wcp_invalid_field_value> and got an error <wcp_error_message>
    Then Admin deletes a Working Capital Loan Product

    Examples:
      | wcp_field_name           | wcp_invalid_field_value  | wcp_error_message                                                                          |
      | digitsAfterDecimal       |  "25"                    | "The parameter `digitsAfterDecimal` must be between 0 and 6."                              |
      | inMultiplesOf            |  "-1"                    | "The parameter `inMultiplesOf` must be zero or greater."                                   |
      | discount                 |  "-1"                    | "The parameter `discount` must be greater than or equal to 0."                             |
      | principal                |  "1"                     | "Failed data validation due to: must.be.greater.than.or.equal.to.min."                     |
      | principal                |  "1000000"               | "Failed data validation due to: must.be.less.than.or.equal.to.max."                        |
      | periodPaymentRate        |  "-1"                    | "The parameter `periodPaymentRate` must be greater than or equal to 0."                    |

  @TestRailId:C70217
  Scenario: Verify Working capital Loan Product create with invalid data shall outcome with error - validation check with number of payment allocation rules - UC10
    Then Admin failed to create a new Working Capital Loan Product with invalid number of payment allocation rules

  @TestRailId:C70218
  Scenario: Verify Working capital Loan Product create with invalid data shall outcome with error - validation check with payment allocation rules - UC11
    Then Admin failed to create a new Working Capital Loan Product with invalid value of payment allocation rules

  @TestRailId:C70219
  Scenario: Verify Working capital Loan Product update with invalid data shall outcome with error - validation check with number of payment allocation rules - UC12
    When Admin creates a new Working Capital Loan Product
    Then Admin failed to update a new Working Capital Loan Product with invalid number of payment allocation rules
    Then Admin deletes a Working Capital Loan Product

  @TestRailId:C70220
  Scenario: Verify Working capital Loan Product update with invalid data shall outcome with error - validation check with payment allocation rules - UC13
    When Admin creates a new Working Capital Loan Product
    Then Admin failed to update a new Working Capital Loan Product with invalid value of payment allocation rules
    Then Admin deletes a Working Capital Loan Product

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

  @TestRailId:C72378
  Scenario: Verify WC Loan Product create with delinquencyGraceDays and delinquencyStartType LOAN_CREATION
    When Admin creates a new Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "LOAN_CREATION"
    Then Working Capital Loan Product has delinquencyGraceDays 3 and delinquencyStartType "LOAN_CREATION"
    Then Admin deletes a Working Capital Loan Product

  @TestRailId:C72379
  Scenario: Verify WC Loan Product create with delinquencyGraceDays 0 and delinquencyStartType DISBURSEMENT
    When Admin creates a new Working Capital Loan Product with delinquencyGraceDays 0 and delinquencyStartType "DISBURSEMENT"
    Then Working Capital Loan Product has delinquencyGraceDays 0 and delinquencyStartType "DISBURSEMENT"
    Then Admin deletes a Working Capital Loan Product

  @TestRailId:C72380
  Scenario: Verify WC Loan Product create without delinquencyGraceDays uses defaults
    When Admin creates a new Working Capital Loan Product
    Then Working Capital Loan Product has null delinquencyGraceDays and null delinquencyStartType
    Then Admin deletes a Working Capital Loan Product

  @TestRailId:C72381
  Scenario: Verify WC Loan Product update to set delinquencyGraceDays
    When Admin creates a new Working Capital Loan Product
    When Admin updates Working Capital Loan Product with delinquencyGraceDays 5 and delinquencyStartType "DISBURSEMENT"
    Then Working Capital Loan Product has delinquencyGraceDays 5 and delinquencyStartType "DISBURSEMENT"
    Then Admin deletes a Working Capital Loan Product

  @TestRailId:C72382
  Scenario: Verify WC Loan Product update to change delinquencyGraceDays
    When Admin creates a new Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "LOAN_CREATION"
    When Admin updates Working Capital Loan Product with delinquencyGraceDays 10 and delinquencyStartType "DISBURSEMENT"
    Then Working Capital Loan Product has delinquencyGraceDays 10 and delinquencyStartType "DISBURSEMENT"
    Then Admin deletes a Working Capital Loan Product

  @TestRailId:C72383
  Scenario: Verify WC Loan Product template includes delinquencyStartTypeOptions
    When Admin retrieves the Working Capital Loan Product template
    Then Working Capital Loan Product template has delinquencyStartTypeOptions containing:
      | LOAN_CREATION |
      | DISBURSEMENT  |

  @TestRailId:C72384
  Scenario Outline: Verify WC Loan Product create with invalid delinquency grace data
    Then Admin failed to create a new Working Capital Loan Product field "<wcp_field_name>" with invalid data <wcp_invalid_field_value> and got an error <wcp_error_message>
    Examples:
      | wcp_field_name         | wcp_invalid_field_value | wcp_error_message                                                   |
      | delinquencyGraceDays   | "-1"                    | "The parameter `delinquencyGraceDays` must be zero or greater."     |
      | delinquencyStartType   | "INVALID_TYPE"          | "invalid.delinquency.start.type"                                    |

  @TestRailId:C72385
  Scenario Outline: Verify WC Loan Product update with invalid delinquency grace data
    When Admin creates a new Working Capital Loan Product
    Then Admin failed to update a new Working Capital Loan Product field "<wcp_field_name>" with invalid data <wcp_invalid_field_value> and got an error <wcp_error_message>
    Then Admin deletes a Working Capital Loan Product

    Examples:
      | wcp_field_name         | wcp_invalid_field_value | wcp_error_message                                                   |
      | delinquencyGraceDays   | "-5"                   | "The parameter `delinquencyGraceDays` must be zero or greater."     |
      | delinquencyStartType   | "BOGUS"                | "invalid.delinquency.start.type"                                    |

  @TestRailId:C74469
  Scenario: Verify WC Loan Product create with breachId
    When Admin creates a new Working Capital Loan Product with breachId
    Then Admin deletes a Working Capital Loan Product

  @TestRailId:C74470
  Scenario Outline: Verify WC Loan Product create/update with invalid breachId
    Then Admin failed to create a new Working Capital Loan Product field "<wcp_field_name>" with invalid data <wcp_invalid_field_value> and got an error <wcp_error_message>
    When Admin creates a new Working Capital Loan Product
    Then Admin failed to update a new Working Capital Loan Product field "<wcp_field_name>" with invalid data <wcp_invalid_field_value> and got an error <wcp_error_message>
    Then Admin deletes a Working Capital Loan Product

    Examples:
      | wcp_field_name | wcp_invalid_field_value | wcp_error_message                                  |
      | breachId       | "0"                     | "The parameter `breachId` must be greater than 0." |

  @TestRailId:C74445
  Scenario: Verify Working capital Loan Product create with Cash based accounting
    When Admin creates a new Working Capital Loan Product with accounting rule "CASH_BASED"
    Then Admin verifies Working Capital Loan Product has accounting rule "CASH_BASED"
    Then Admin deletes Working Capital Loan Product and verifies GL account mappings are cleaned up

  @TestRailId:C74446
  Scenario: Verify Working capital Loan Product create with accounting rule None
    When Admin creates a new Working Capital Loan Product with accounting rule "NONE"
    Then Admin verifies Working Capital Loan Product has accounting rule "NONE"
    Then Admin deletes a Working Capital Loan Product

  @TestRailId:C74447
  Scenario: Verify Working capital Loan Product create with Cash based accounting fails when required GL accounts are missing
    Then Admin failed to create a new Working Capital Loan Product with Cash based accounting and missing required GL accounts

  @TestRailId:C74448
  Scenario: Verify Working capital Loan Product update accounting rule from None to Cash based
    When Admin creates a new Working Capital Loan Product with accounting rule "NONE"
    When Admin updates Working Capital Loan Product accounting rule from None to Cash based
    Then Admin verifies Working Capital Loan Product has accounting rule "CASH_BASED"
    Then Admin deletes Working Capital Loan Product and verifies GL account mappings are cleaned up

  @TestRailId:C74449
  Scenario: Verify Working capital Loan Product update accounting rule from Cash based to None
    When Admin creates a new Working Capital Loan Product with accounting rule "CASH_BASED"
    When Admin updates Working Capital Loan Product accounting rule from Cash based to None
    Then Admin verifies Working Capital Loan Product has accounting rule "NONE"
    Then Admin deletes a Working Capital Loan Product

  @TestRailId:C74450
  Scenario: Verify Working capital Loan Product update GL account mappings on existing Cash based product
    When Admin creates a new Working Capital Loan Product with accounting rule "CASH_BASED"
    When Admin updates GL account mappings on existing Cash based Working Capital Loan Product
    Then Admin verifies Working Capital Loan Product has accounting rule "CASH_BASED"
    Then Admin deletes a Working Capital Loan Product

  @TestRailId:C74451
  Scenario: Verify Working capital Loan Product template has accounting options
    Then Admin verifies Working Capital Loan Product template has accounting options

  @TestRailId:C74452
  Scenario: Verify Working capital Loan Product create with Cash based accounting and verify each GL account mapping value
    When Admin creates a new Working Capital Loan Product with Cash based accounting for GL mapping verification
    Then Admin verifies Working Capital Loan Product GL account mapping values match the request
    Then Admin deletes Working Capital Loan Product and verifies GL account mappings are cleaned up

  @TestRailId:C74454
  Scenario: Verify Working capital Loan Product create with accounting rule None has no accounting mappings
    When Admin creates a new Working Capital Loan Product with accounting rule "NONE"
    Then Admin verifies Working Capital Loan Product has no accounting mappings
    Then Admin deletes a Working Capital Loan Product

  @TestRailId:C74455
  Scenario: Verify Working capital Loan Product create with non-existent GL account ID fails
    Then Admin failed to create a Working Capital Loan Product with Cash based accounting and non-existent GL account ID with status 404

  @TestRailId:C74456
  Scenario: Verify Working capital Loan Product update to Cash based without required GL accounts fails
    When Admin creates a new Working Capital Loan Product with accounting rule "NONE"
    Then Admin failed to update Working Capital Loan Product to Cash based without required GL accounts with status 400
    Then Admin deletes a Working Capital Loan Product

  @TestRailId:C74457
  Scenario: Verify Working capital Loan Product update single GL account on Cash based product and verify changed value
    When Admin creates a new Working Capital Loan Product with accounting rule "CASH_BASED"
    When Admin updates writeOff GL account on Cash based Working Capital Loan Product
    Then Admin verifies Working Capital Loan Product writeOff GL account was updated
    Then Admin deletes a Working Capital Loan Product

  @TestRailId:C74458
  Scenario: Verify Working capital Loan Product update from Cash based to None removes all GL account mappings
    When Admin creates a new Working Capital Loan Product with Cash based accounting for GL mapping verification
    Then Admin verifies Working Capital Loan Product GL account mapping values match the request
    When Admin updates Working Capital Loan Product accounting rule from Cash based to None
    Then Admin verifies Working Capital Loan Product has no accounting mappings
    Then Admin deletes a Working Capital Loan Product

  @TestRailId:C74459
  Scenario: Verify Working capital Loan Product list returns accounting rule field
    When Admin creates a new Working Capital Loan Product with accounting rule "CASH_BASED"
    Then Admin verifies Working Capital Loan Product list contains the product with accounting rule "CASH_BASED"
    Then Admin deletes a Working Capital Loan Product

  @TestRailId:C74460
  Scenario: Verify Working capital Loan Product create with wrong GL account type for loanPortfolio fails
    Then Admin failed to create a Working Capital Loan Product with wrong GL account type for loanPortfolio with status 403

  @TestRailId:C74461
  Scenario: Verify Working capital Loan Product template accounting rule options contain NONE and CASH_BASED
    Then Admin verifies Working Capital Loan Product template has NONE and CASH_BASED accounting rule options
