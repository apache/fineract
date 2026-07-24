@LoanOverrideFields
Feature: LoanOverrideFields

  @TestRailId:C4142
  Scenario: Verify that all nullable fields default to product when overrides not allowed and not provided
    When Admin sets the business date to the actual date
    When Admin creates a client with random data
    When Admin creates a new Loan with the following override data:
      | loanProduct             | LP1_NO_OVERRIDES |
      | inArrearsTolerance      | null             |
      | graceOnPrincipalPayment | null             |
      | graceOnInterestPayment  | null             |
      | graceOnArrearsAgeing    | null             |
    Then LoanDetails has "inArrearsTolerance" field with value: "10"
    Then LoanDetails has "graceOnPrincipalPayment" field with value: "1"
    Then LoanDetails has "graceOnInterestPayment" field with value: "1"
    Then LoanDetails has "graceOnArrearsAgeing" field with value: "3"

  @TestRailId:C4143
  Scenario: Verify that all nullable fields ignore overrides when overrides not allowed
    When Admin sets the business date to the actual date
    When Admin creates a client with random data
    When Admin creates a new Loan with the following override data:
      | loanProduct             | LP1_NO_OVERRIDES |
      | inArrearsTolerance      | 11               |
      | graceOnPrincipalPayment | 2                |
      | graceOnInterestPayment  | 2                |
      | graceOnArrearsAgeing    | 4                |
    Then LoanDetails has "inArrearsTolerance" field with value: "10"
    Then LoanDetails has "graceOnPrincipalPayment" field with value: "1"
    Then LoanDetails has "graceOnInterestPayment" field with value: "1"
    Then LoanDetails has "graceOnArrearsAgeing" field with value: "3"

  @TestRailId:C4144
  Scenario: Verify that nullable fields default to product when override is allowed but not provided
    When Admin sets the business date to the actual date
    When Admin creates a client with random data
    When Admin creates a new Loan with the following override data:
      | loanProduct             | LP1_WITH_OVERRIDES |
      | inArrearsTolerance      | null               |
      | graceOnPrincipalPayment | null               |
      | graceOnInterestPayment  | null               |
      | graceOnArrearsAgeing    | null               |
    Then LoanDetails has "inArrearsTolerance" field with value: "10"
    Then LoanDetails has "graceOnPrincipalPayment" field with value: "1"
    Then LoanDetails has "graceOnInterestPayment" field with value: "1"
    Then LoanDetails has "graceOnArrearsAgeing" field with value: "3"

  @TestRailId:C4145
  Scenario: Verify that nullable fields default to product when override is allowed and provided
    When Admin sets the business date to the actual date
    When Admin creates a client with random data
    When Admin creates a new Loan with the following override data:
      | loanProduct             | LP1_WITH_OVERRIDES |
      | inArrearsTolerance      | 11                 |
      | graceOnPrincipalPayment | 2                  |
      | graceOnInterestPayment  | 2                  |
      | graceOnArrearsAgeing    | 4                  |
    Then LoanDetails has "inArrearsTolerance" field with value: "11"
    Then LoanDetails has "graceOnPrincipalPayment" field with value: "2"
    Then LoanDetails has "graceOnInterestPayment" field with value: "2"
    Then LoanDetails has "graceOnArrearsAgeing" field with value: "4"

  # The loan is created with interestType FLAT (1), which is a real override: LP1_WITH_OVERRIDES defaults to
  # DECLINING BALANCE (0). Changing the principal forces the schedule to be recalculated, so omitting interestType
  # from the modify request used to be read as null and fail with a NullPointerException (HTTP 500).
  @TestRailId:C4698
  Scenario: Verify that a loan can be modified when an override-enabled schedule field is omitted from the request
    When Admin sets the business date to the actual date
    When Admin creates a client with random data
    When Admin creates a new Loan with the following override data:
      | loanProduct  | LP1_WITH_OVERRIDES |
      | interestType | 1                  |
    Then LoanDetails has "interestType" field with value: "1"
    When Admin modifies the loan, changing principal to "2000" and omitting the override-enabled schedule fields
    Then LoanDetails has "principal" field with value: "2000"
    Then LoanDetails has "interestType" field with value: "1"
    Then LoanDetails has "amortizationType" field with value: "1"
    Then LoanDetails has "interestCalculationPeriodType" field with value: "1"
    Then LoanDetails has "repaymentEvery" field with value: "30"

  @TestRailId:C4699
  Scenario: Verify that an override-enabled schedule field provided on modify is honoured
    When Admin sets the business date to the actual date
    When Admin creates a client with random data
    When Admin creates a new Loan with the following override data:
      | loanProduct  | LP1_WITH_OVERRIDES |
      | interestType | 1                  |
    Then LoanDetails has "interestType" field with value: "1"
    When Admin modifies the loan, changing principal to "2000" and setting interestType to "0"
    Then LoanDetails has "principal" field with value: "2000"
    Then LoanDetails has "interestType" field with value: "0"
