@ExternalAssetOwnerLoanProductAttributes
Feature: External Asset Owner Loan Product Attributes

  @TestRailId:C106774
  Scenario: Verify EXCLUDED_TRANSACTION_TYPES loan product attribute - UC1: template contains EXCLUDED_TRANSACTION_TYPES and unchanged SETTLEMENT_MODEL
    When Admin retrieves the external asset owner loan product attribute template
    Then The template contains attribute "EXCLUDED_TRANSACTION_TYPES" with isMultiValue "true" and attributeValues containing "BUY_DOWN_FEE" and "BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT" but not "INVALID"
    Then The template contains attribute "SETTLEMENT_MODEL" with isMultiValue "false" and exact attributeValues "DEFAULT_SETTLEMENT,DELAYED_SETTLEMENT"

  @TestRailId:C106775
  Scenario: Verify EXCLUDED_TRANSACTION_TYPES loan product attribute - UC2: create, read back verbatim and update with normalisation
    Given Loan product "LP1_DUE_DATE_UC2" exists dedicated to this feature
    When Admin creates external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP1_DUE_DATE_UC2" with values:
      | BUY_DOWN_FEE                         |
      | BUY_DOWN_FEE_ADJUSTMENT              |
      | BUY_DOWN_FEE_AMORTIZATION            |
      | BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT |
    Then The request succeeds and returns loan product "LP1_DUE_DATE_UC2"
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP1_DUE_DATE_UC2" has values:
      | BUY_DOWN_FEE                         |
      | BUY_DOWN_FEE_ADJUSTMENT              |
      | BUY_DOWN_FEE_AMORTIZATION            |
      | BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT |
    # padding around the separator must be normalised
    When Admin updates external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP1_DUE_DATE_UC2" with attributeKey "EXCLUDED_TRANSACTION_TYPES" and values:
      | BUY_DOWN_FEE            |
      | BUY_DOWN_FEE_ADJUSTMENT |
    Then The request succeeds and returns loan product "LP1_DUE_DATE_UC2"
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP1_DUE_DATE_UC2" has values:
      | BUY_DOWN_FEE            |
      | BUY_DOWN_FEE_ADJUSTMENT |
    # delete afterwards to leave this dedicated loan product with no attribute, so this scenario is safely re-runnable
    When Admin deletes external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP1_DUE_DATE_UC2"
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP1_DUE_DATE_UC2" does not exist

  @TestRailId:C106776
  Scenario: Verify EXCLUDED_TRANSACTION_TYPES loan product attribute - UC3: duplicate create for the same product and key is rejected (Negative)
    Given Loan product "LP1_DUE_DATE_UC3" exists dedicated to this feature
    When Admin creates external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP1_DUE_DATE_UC3" with values:
      | BUY_DOWN_FEE |
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP1_DUE_DATE_UC3" has values:
      | BUY_DOWN_FEE |
    Then Creating external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" value "BUY_DOWN_FEE" for loan product "LP1_DUE_DATE_UC3" fails with:
      | errorMessage                                            |
      | error.msg.externalAssetOwnerLoanProductAttribute.exists |
    # delete afterwards to leave this dedicated loan product with no attribute, so this scenario is safely re-runnable
    When Admin deletes external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP1_DUE_DATE_UC3"
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP1_DUE_DATE_UC3" does not exist

  @TestRailId:C106777
  Scenario: Verify EXCLUDED_TRANSACTION_TYPES loan product attribute - UC4: update with a mismatching attributeKey is rejected (Negative)
    Given Loan product "LP1_DUE_DATE_UC4" exists dedicated to this feature
    When Admin creates external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP1_DUE_DATE_UC4" with values:
      | BUY_DOWN_FEE |
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP1_DUE_DATE_UC4" has values:
      | BUY_DOWN_FEE |
    Then Updating external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP1_DUE_DATE_UC4" with attributeKey "SETTLEMENT_MODEL" and value "DEFAULT_SETTLEMENT" fails with:
      | errorMessage                                              |
      | error.msg.externalAssetOwnerLoanProductAttributes.general |
    # delete afterwards to leave this dedicated loan product with no attribute, so this scenario is safely re-runnable
    When Admin deletes external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP1_DUE_DATE_UC4"
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP1_DUE_DATE_UC4" does not exist

  @TestRailId:C106778
  Scenario: Verify EXCLUDED_TRANSACTION_TYPES loan product attribute - UC5: unknown transaction type is rejected and nothing is persisted (Negative)
    Given Loan product "LP1_DUE_DATE_UC5" exists dedicated to this feature
    Then Creating external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" value "BUY_DOWN_FEE,NOT_A_TYPE" for loan product "LP1_DUE_DATE_UC5" fails with:
      | errorMessage                                                           |
      | error.msg.externalAssetOwnerLoanProductAttribute.invalidAttributeValue |
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" for loan product "LP1_DUE_DATE_UC5" does not exist

  @TestRailId:C106779
  Scenario Outline: Verify EXCLUDED_TRANSACTION_TYPES loan product attribute - UC6: malformed value "<value>" is rejected (Negative)
    Given Loan product "LP1_DUE_DATE_UC6" exists dedicated to this feature
    Then Creating external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" value "<value>" for loan product "LP1_DUE_DATE_UC6" fails with:
      | errorMessage                                                           |
      | error.msg.externalAssetOwnerLoanProductAttribute.invalidAttributeValue |

    Examples:
      | value                     |
      | ,                         |
      | BUY_DOWN_FEE,             |
      | BUY_DOWN_FEE,BUY_DOWN_FEE |
      | buy_down_fee              |

  @TestRailId:C106780
  Scenario: Verify EXCLUDED_TRANSACTION_TYPES loan product attribute - UC7: blank value is rejected (Negative)
    Given Loan product "LP1_DUE_DATE_UC7" exists dedicated to this feature
    Then Creating external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" value "" for loan product "LP1_DUE_DATE_UC7" fails with:
      | errorMessage                           |
      | validation.msg.validation.errors.exist |

  @TestRailId:C106781
  Scenario: Verify EXCLUDED_TRANSACTION_TYPES loan product attribute - UC8: value exceeding the maximum allowed length is rejected (Negative)
    Given Loan product "LP1_DUE_DATE_UC8" exists dedicated to this feature
    Then Creating external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" with an attribute value exceeding the maximum length for loan product "LP1_DUE_DATE_UC8" fails with:
      | errorMessage                           |
      | validation.msg.validation.errors.exist |

  @TestRailId:C106782
  Scenario: Verify EXCLUDED_TRANSACTION_TYPES loan product attribute - UC9: unknown loan product is rejected (Negative)
    Then Creating external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" value "BUY_DOWN_FEE" for loan product id 999999999 fails with:
      | errorMessage                     |
      | error.msg.loanproduct.id.invalid |
