@AssetExternalizationFeature
Feature: External Asset Owner Loan Product Attributes

  Scenario: Verify external asset owner loan product attributes template contains buy down fee amortization strategy
    Then External asset owner loan product attributes template contains the following attributes:
      | attributeKey                       | attributeValues                       | multiValue |
      | SETTLEMENT_MODEL                   | DEFAULT_SETTLEMENT,DELAYED_SETTLEMENT | false      |
      | BUY_DOWN_FEE_AMORTIZATION_STRATEGY | DEFERRED,IMMEDIATE                    | false      |

  Scenario: Verify buy down fee amortization strategy attribute can be created and retrieved
    When Admin creates a new loan product for external asset owner loan product attributes
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product does not exist
    When Admin creates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "IMMEDIATE" for the new loan product
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "IMMEDIATE"
    Then External asset owner loan product attribute "EXCLUDED_TRANSACTION_TYPES" of the new loan product does not exist

  Scenario: Verify buy down fee amortization strategy attribute can be switched between DEFERRED and IMMEDIATE
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "DEFERRED" for the new loan product
    When Admin updates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product to value "IMMEDIATE"
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "IMMEDIATE"
    When Admin updates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product to value "DEFERRED"
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"

  Scenario: Verify buy down fee amortization strategy attribute value is accepted in any letter case and stored canonical
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "immediate" for the new loan product
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "IMMEDIATE"
    When Admin updates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product to value "Deferred"
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"

  Scenario: Verify duplicate buy down fee amortization strategy attribute is rejected
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "DEFERRED" for the new loan product
    Then Creating external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "IMMEDIATE" for the new loan product results a 403 error and "LOAN_PRODUCT_ATTRIBUTE_ALREADY_EXISTS" error message
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"

  Scenario Outline: Verify buy down fee amortization strategy attribute creation with invalid value "<attributeValue>" is rejected
    When Admin creates a new loan product for external asset owner loan product attributes
    Then Creating external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "<attributeValue>" for the new loan product results a 403 error and "LOAN_PRODUCT_ATTRIBUTE_INVALID" error message
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product does not exist

    Examples:
      | attributeValue     |
      | UNKNOWN            |
      | DEFERRED,IMMEDIATE |
      | DEFAULT_SETTLEMENT |

  Scenario Outline: Verify buy down fee amortization strategy attribute update with invalid value "<attributeValue>" is rejected
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "DEFERRED" for the new loan product
    Then Updating external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product to value "<attributeValue>" results a 403 error and "LOAN_PRODUCT_ATTRIBUTE_INVALID" error message
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"

    Examples:
      | attributeValue     |
      | UNKNOWN            |
      | DEFERRED,IMMEDIATE |

  Scenario: Verify buy down fee amortization strategy attribute for non-existing loan product is rejected
    Then Creating external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "IMMEDIATE" for non-existing loan product results a 404 error and "LOAN_PRODUCT_NOT_FOUND" error message

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

  Scenario: Verify buy down fee amortization strategy attribute creation and update are guarded by their permissions
    When Admin creates a new loan product for external asset owner loan product attributes
    When Admin creates new user with "EAO_ATTR_NO_PERMISSION" username, "EAO_ATTR_NO_PERMISSION_ROLE" role name and given permissions:
      | READ_LOAN |
    Then Created user creating external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "IMMEDIATE" for the new loan product results a 403 error and "USER_HAS_NO_CREATE_ATTRIBUTE_AUTHORITY" error message
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product does not exist
    When Admin creates external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" with value "DEFERRED" for the new loan product
    Then Created user updating external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product to value "IMMEDIATE" results a 403 error and "USER_HAS_NO_UPDATE_ATTRIBUTE_AUTHORITY" error message
    Then External asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" of the new loan product has value "DEFERRED"

  Scenario: Verify buy down fee is amortized over the loan term as without the attribute when buy down fee amortization strategy is DEFERRED
    When Admin set external asset owner loan product attribute "BUY_DOWN_FEE_AMORTIZATION_STRATEGY" value "DEFERRED" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 January 2024    | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "01 January 2024" with "50" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee     | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 01 January 2024 | 50.0       | 0.0              | 50.0                     | 0.0             | 0.0                |
    When Admin sets the business date to "1 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 33.72 EUR transaction amount
    When Admin sets the business date to "1 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 33.72 EUR transaction amount
    When Admin sets the business date to "1 April 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 33.73 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 01 February 2024 | Repayment                 | 33.72  | 33.14     | 0.58     | 0.0  | 0.0       | 66.86        | false    |
      | 01 March 2024    | Repayment                 | 33.72  | 33.33     | 0.39     | 0.0  | 0.0       | 33.53        | false    |
      | 31 March 2024    | Accrual                   | 1.16   | 0.0       | 1.16     | 0.0  | 0.0       | 0.0          | false    |
      | 31 March 2024    | Buy Down Fee Amortization | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 01 April 2024    | Repayment                 | 33.73  | 33.53     | 0.2      | 0.0  | 0.0       | 0.0          | false    |
      | 01 April 2024    | Accrual                   | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee by external-id contains the following data:
      | Date            | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 01 January 2024 | 50.0       | 50.0             | 0.0                      | 0.0             | 0.0                |
