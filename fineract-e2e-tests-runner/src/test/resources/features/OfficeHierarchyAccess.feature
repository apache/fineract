@OfficeHierarchyAccessFeature
Feature: Office hierarchy access

  Scenario: User cannot repay a loan in a sibling office
    When Admin sets the business date to "1 January 2025"
    When Admin creates a new office
    When Admin creates a client with random data in the last created office
    When Admin creates a new default Loan with date: "1 January 2025"
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    When Admin successfully disburse the loan on "1 January 2025" with "1000" EUR transaction amount
    When Admin creates a new office
    When Admin creates new user with "SIBLING_LOAN" username, "SIBLING_LOAN_ROLE" role name, assigned to the last created office and given permissions:
      | REPAYMENT_LOAN |
    Then Created user cannot make "AUTOPAY" repayment on "1 January 2025" with 100 EUR transaction amount

  Scenario: User cannot deposit to a savings account in a sibling office
    When Admin sets the business date to "1 January 2025"
    When Admin creates a new office
    When Admin creates a client with random data in the last created office
    And Admin creates a EUR savings product
    And Client creates a new EUR savings account with "1 January 2025" submitted on date
    And Approve EUR savings account on "1 January 2025" date
    And Activate EUR savings account on "1 January 2025" date
    When Admin creates a new office
    When Admin creates new user with "SIBLING_SAVINGS" username, "SIBLING_SAVINGS_ROLE" role name, assigned to the last created office and given permissions:
      | DEPOSIT_SAVINGSACCOUNT |
    Then Created user cannot deposit 100 EUR to the savings account on "1 January 2025" date
