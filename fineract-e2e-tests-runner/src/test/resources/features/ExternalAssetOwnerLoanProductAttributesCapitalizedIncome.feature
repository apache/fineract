@AssetExternalizationFeature
Feature: External Asset Owner Loan Product Attributes - Capitalized Income Amortization Strategy

  @TestRailId:C106811
  Scenario: Verify external asset owner loan product attributes template contains capitalized income amortization strategy
    Then External asset owner loan product attributes template contains the following attributes:
      | attributeKey                             | attributeValues                       | multiValue |
      | SETTLEMENT_MODEL                         | DEFAULT_SETTLEMENT,DELAYED_SETTLEMENT | false      |
      | CAPITALIZED_INCOME_AMORTIZATION_STRATEGY | DEFERRED,IMMEDIATE                    | false      |
    Then External asset owner loan product attributes template attribute "EXCLUDED_TRANSACTION_TYPES" has multiValue "true", contains "CAPITALIZED_INCOME,CAPITALIZED_INCOME_ADJUSTMENT,CAPITALIZED_INCOME_AMORTIZATION,CAPITALIZED_INCOME_AMORTIZATION_ADJUSTMENT" and does not contain "INVALID,DEFERRED,IMMEDIATE"

  @TestRailId:C106812
  Scenario: Verify capitalized income amortization strategy attribute can be created and retrieved
    When Admin creates a new loan product for external asset owner loan product attributes
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product does not exist
    When Admin creates external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" with value "IMMEDIATE" for the new loan product
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product has value "IMMEDIATE"
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" of the new loan product does not exist

  @TestRailId:C106813
  Scenario: Verify capitalized income amortization strategy attribute can be switched between DEFERRED and IMMEDIATE
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" with value "DEFERRED" for the new loan product
    When Admin updates external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product to value "IMMEDIATE"
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product has value "IMMEDIATE"
    When Admin updates external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product to value "DEFERRED"
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" of the new loan product does not exist

  @TestRailId:C106814
  Scenario: Verify capitalized income amortization strategy attribute value is accepted in any letter case and stored canonical
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" with value "immediate" for the new loan product
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product has value "IMMEDIATE"
    When Admin updates external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product to value "Deferred"
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"

  @TestRailId:C106815
  Scenario: Verify duplicate capitalized income amortization strategy attribute is rejected
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" with value "DEFERRED" for the new loan product
    Then Creating external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" with value "IMMEDIATE" for the new loan product results a 403 error and "LOAN_PRODUCT_ATTRIBUTE_ALREADY_EXISTS" error message
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"

  @TestRailId:C106816
  Scenario Outline: Verify capitalized income amortization strategy attribute creation with invalid value "<attributeValue>" is rejected
    When Admin creates a new loan product for external asset owner loan product attributes
    Then Creating external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" with value "<attributeValue>" for the new loan product results a 403 error and "LOAN_PRODUCT_ATTRIBUTE_VALUE_INVALID" error message
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product does not exist

    Examples:
      | attributeValue      |
      | UNKNOWN             |
      | DEFERRED,IMMEDIATE  |
      | IMMEDIATE,IMMEDIATE |
      | DELAYED_SETTLEMENT  |
      | CAPITALIZED_INCOME  |

  @TestRailId:C106817
  Scenario Outline: Verify capitalized income amortization strategy attribute update with invalid value "<attributeValue>" is rejected
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" with value "DEFERRED" for the new loan product
    Then Updating external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product to value "<attributeValue>" results a 403 error and "LOAN_PRODUCT_ATTRIBUTE_VALUE_INVALID" error message
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"

    Examples:
      | attributeValue     |
      | UNKNOWN            |
      | DEFERRED,IMMEDIATE |

  @TestRailId:C106818
  Scenario: Verify capitalized income amortization strategy attribute with blank value is rejected with a validation error
    When Admin creates a new loan product for external asset owner loan product attributes
    Then Creating external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" with value "" for the new loan product results a 400 error and "LOAN_PRODUCT_ATTRIBUTE_VALUE_MANDATORY" error message
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product does not exist

  @TestRailId:C106819
  Scenario: Verify capitalized income amortization strategy attribute for non-existing loan product is rejected
    Then Creating external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" with value "IMMEDIATE" for non-existing loan product results a 404 error and "LOAN_PRODUCT_NOT_FOUND" error message

  @TestRailId:C106820
  Scenario: Verify capitalized income amortization strategy and excluded transaction types attributes are managed independently on the same loan product
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" with value "IMMEDIATE" for the new loan product
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" of the new loan product does not exist
    When Admin creates external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" with value "CAPITALIZED_INCOME,CAPITALIZED_INCOME_AMORTIZATION" for the new loan product
    When Admin updates external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" of the new loan product to value "CAPITALIZED_INCOME"
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product has value "IMMEDIATE"
    When Admin updates external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product to value "DEFERRED"
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" of the new loan product has value "CAPITALIZED_INCOME"
    When Admin deletes external asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" of the new loan product
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"

  @TestRailId:C106821
  Scenario: Verify deleted capitalized income amortization strategy attribute falls back to absent and can be configured again
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" with value "IMMEDIATE" for the new loan product
    When Admin deletes external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product does not exist
    When Admin creates external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" with value "DEFERRED" for the new loan product
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"

  @TestRailId:C106822
  Scenario: Verify capitalized income amortization strategy attribute creation and update are guarded by their permissions
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates new user with "EAO_CI_NO_PERMISSION" username, "EAO_CI_NO_PERMISSION_ROLE" role name and given permissions:
      | READ_LOAN |
    Then Created user creating external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" with value "IMMEDIATE" for the new loan product results a 403 error and "USER_HAS_NO_CREATE_ATTRIBUTE_AUTHORITY" error message
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product does not exist
    When Admin creates external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" with value "DEFERRED" for the new loan product
    Then Created user updating external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product to value "IMMEDIATE" results a 403 error and "USER_HAS_NO_UPDATE_ATTRIBUTE_AUTHORITY" error message
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"
    When Admin creates new user with "EAO_CI_UPDATE_PERMISSION" username, "EAO_CI_UPDATE_PERMISSION_ROLE" role name and given permissions:
      | UPDATE_EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE |
    When Created user updates external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product to value "IMMEDIATE"
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product has value "IMMEDIATE"

  @TestRailId:C106823
  Scenario: Verify capitalized income amortization strategy attribute cannot be updated through another loan product
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" with value "DEFERRED" for the new loan product
    Then Updating external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product through another loan product to value "IMMEDIATE" results a 403 error and "LOAN_PRODUCT_ATTRIBUTE_OF_ANOTHER_LOAN_PRODUCT" error message
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"

  @TestRailId:C106824 @ApplicationCacheScenario
  Scenario: Verify capitalized income amortization strategy attribute changes are visible immediately with the single node cache enabled
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin switches the application cache to "SINGLE_NODE"
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product does not exist
    When Admin creates external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" with value "IMMEDIATE" for the new loan product
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product has value "IMMEDIATE"
    When Admin updates external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product to value "DEFERRED"
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"
    When Admin deletes external asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product
    Then External asset owner loan product attribute "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY" of the new loan product does not exist
    When Admin switches the application cache to "NO_CACHE"
