@AssetExternalizationFeature
@ExternalAssetOwnerLoanProductAttributes
Feature: External Asset Owner Loan Product Attributes

  @TestRailId:C106786
  Scenario: Verify external asset owner loan product attributes template contains buy down fee amortization strategy
    Then External asset owner loan product attributes template contains the following attributes:
      | attributeKey                       | attributeValues                       | multiValue |
      | SETTLEMENT_MODEL                   | DEFAULT_SETTLEMENT,DELAYED_SETTLEMENT | false      |
      | BUY_DOWN_FEE_AMORTIZATION_STRATEGY | DEFERRED,IMMEDIATE                    | false      |

  @TestRailId:C106787
  Scenario: Verify buy down fee amortization strategy attribute can be created and retrieved
    When Admin creates a new loan product for external asset owner loan product attributes
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product does not exist
    When Admin creates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "IMMEDIATE" for the new loan product
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "IMMEDIATE"
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" of the new loan product does not exist

  @TestRailId:C106788
  Scenario: Verify buy down fee amortization strategy attribute can be switched between DEFERRED and IMMEDIATE
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "DEFERRED" for the new loan product
    When Admin updates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product to value "IMMEDIATE"
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "IMMEDIATE"
    When Admin updates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product to value "DEFERRED"
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"

  @TestRailId:C106789
  Scenario: Verify buy down fee amortization strategy attribute value is accepted in any letter case and stored canonical
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "immediate" for the new loan product
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "IMMEDIATE"
    When Admin updates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product to value "Deferred"
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"

  @TestRailId:C106790
  Scenario: Verify duplicate buy down fee amortization strategy attribute is rejected
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "DEFERRED" for the new loan product
    Then Creating external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "IMMEDIATE" for the new loan product results a 403 error and "LOAN_PRODUCT_ATTRIBUTE_ALREADY_EXISTS" error message
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"

  @TestRailId:C106791
  Scenario Outline: Verify buy down fee amortization strategy attribute creation with invalid value "<attributeValue>" is rejected
    When Admin creates a new loan product for external asset owner loan product attributes
    Then Creating external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "<attributeValue>" for the new loan product results a 403 error and "LOAN_PRODUCT_ATTRIBUTE_VALUE_INVALID" error message
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product does not exist

    Examples:
      | attributeValue     |
      | UNKNOWN            |
      | DEFERRED,IMMEDIATE |
      | DEFAULT_SETTLEMENT |

  @TestRailId:C106792
  Scenario Outline: Verify buy down fee amortization strategy attribute update with invalid value "<attributeValue>" is rejected
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "DEFERRED" for the new loan product
    Then Updating external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product to value "<attributeValue>" results a 403 error and "LOAN_PRODUCT_ATTRIBUTE_VALUE_INVALID" error message
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"

    Examples:
      | attributeValue     |
      | UNKNOWN            |
      | DEFERRED,IMMEDIATE |

  @TestRailId:C106793
  Scenario: Verify buy down fee amortization strategy attribute for non-existing loan product is rejected
    Then Creating external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "IMMEDIATE" for non-existing loan product results a 404 error and "LOAN_PRODUCT_NOT_FOUND" error message

  @TestRailId:C106794
  Scenario: Verify buy down fee amortization strategy and excluded transaction types attributes are managed independently on the same loan product
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" with value "BUY_DOWN_FEE" for the new loan product
    When Admin updates external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" of the new loan product to value "BUY_DOWN_FEE,BUY_DOWN_FEE_AMORTIZATION"
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" of the new loan product has value "BUY_DOWN_FEE,BUY_DOWN_FEE_AMORTIZATION"
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product does not exist
    When Admin creates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "DEFERRED" for the new loan product
    When Admin updates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product to value "IMMEDIATE"
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "IMMEDIATE"
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" of the new loan product has value "BUY_DOWN_FEE,BUY_DOWN_FEE_AMORTIZATION"

  @TestRailId:C106795
  Scenario: Verify buy down fee amortization strategy attribute creation and update are guarded by their permissions
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates new user with "EAO_ATTR_NO_PERMISSION" username, "EAO_ATTR_NO_PERMISSION_ROLE" role name and given permissions:
      | READ_LOAN |
    Then Created user creating external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "IMMEDIATE" for the new loan product results a 403 error and "USER_HAS_NO_CREATE_ATTRIBUTE_AUTHORITY" error message
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product does not exist
    When Admin creates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "DEFERRED" for the new loan product
    Then Created user updating external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product to value "IMMEDIATE" results a 403 error and "USER_HAS_NO_UPDATE_ATTRIBUTE_AUTHORITY" error message
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"

  @TestRailId:C106797
  Scenario: Verify external asset owner loan product attributes template lists every attribute once and keeps excluded transaction types unchanged
    Then External asset owner loan product attributes template contains exactly the attribute keys "SETTLEMENT_MODEL,EXCLUDED_TRANSACTION_TYPES,BUY_DOWN_FEE_AMORTIZATION_STRATEGY"
    Then External asset owner loan product attributes template contains the following attributes:
      | attributeKey                       | attributeValues                       | multiValue |
      | SETTLEMENT_MODEL                   | DEFAULT_SETTLEMENT,DELAYED_SETTLEMENT | false      |
      | BUY_DOWN_FEE_AMORTIZATION_STRATEGY | DEFERRED,IMMEDIATE                    | false      |
    Then External asset owner loan product attributes template attribute "EXCLUDED_TRANSACTION_TYPES" has multiValue "true", contains "BUY_DOWN_FEE,BUY_DOWN_FEE_ADJUSTMENT,BUY_DOWN_FEE_AMORTIZATION,BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT" and does not contain "INVALID,DEFERRED,IMMEDIATE"

  @TestRailId:C106798
  Scenario Outline: Verify external asset owner loan product attribute with key "<attributeKey>" and value "<attributeValue>" is rejected as invalid
    When Admin creates a new loan product for external asset owner loan product attributes
    Then Creating external asset owner loan product attribute "<attributeKey>" with value "<attributeValue>" for the new loan product results a 403 error and "<errorMessage>" error message
    Then External asset owner loan product attribute "<attributeKey>" of the new loan product does not exist
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product does not exist

    Examples:
      | attributeKey                       | attributeValue | errorMessage                         |
      | buy_down_fee_amortization_strategy | IMMEDIATE      | LOAN_PRODUCT_ATTRIBUTE_INVALID       |
      | SETTLEMENT_MODEL                   | IMMEDIATE      | LOAN_PRODUCT_ATTRIBUTE_INVALID       |
      | EXCLUDED_TRANSACTION_TYPES         | DEFERRED       | LOAN_PRODUCT_ATTRIBUTE_VALUE_INVALID |

  @TestRailId:C106799
  Scenario: Verify buy down fee amortization strategy attribute with blank value is rejected with a validation error
    When Admin creates a new loan product for external asset owner loan product attributes
    Then Creating external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "" for the new loan product results a 400 error and "LOAN_PRODUCT_ATTRIBUTE_VALUE_MANDATORY" error message
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product does not exist

  @TestRailId:C106800
  Scenario: Verify deleted buy down fee amortization strategy attribute falls back to absent and can be configured again
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "IMMEDIATE" for the new loan product
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "IMMEDIATE"
    When Admin deletes external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product does not exist
    When Admin creates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "DEFERRED" for the new loan product
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"

  @TestRailId:C106801
  Scenario: Verify buy down fee amortization strategy attribute can be created and updated by a user holding only the attribute permissions
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates new user with "EAO_ATTR_PERMISSION" username, "EAO_ATTR_PERMISSION_ROLE" role name and given permissions:
      | CREATE_EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE |
      | UPDATE_EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE |
    When Created user creates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "IMMEDIATE" for the new loan product
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "IMMEDIATE"
    When Created user updates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product to value "DEFERRED"
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"

  @TestRailId:C106802
  Scenario: Verify buy down fee amortization strategy attribute cannot be updated through another loan product
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "DEFERRED" for the new loan product
    Then Updating external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product through another loan product to value "IMMEDIATE" results a 403 error and "LOAN_PRODUCT_ATTRIBUTE_OF_ANOTHER_LOAN_PRODUCT" error message
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"

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
