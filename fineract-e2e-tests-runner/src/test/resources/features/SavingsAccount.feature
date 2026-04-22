@SavingsAccount
Feature: SavingsAccount

  @TestRailId:C2438
  Scenario: As a user I would like to Deposit to my savings account
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin creates a EUR savings product
    And Client creates a new EUR savings account with "1 June 2022" submitted on date
    And Approve EUR savings account on "1 June 2022" date
    And Activate EUR savings account on "1 June 2022" date
    And Client successfully deposits 1000 EUR to the savings account on "1 June 2022" date

  @TestRailId:C2439
  Scenario: As a user I would like to Withdraw from my savings account
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin creates a EUR savings product
    And Client creates a new EUR savings account with "1 June 2022" submitted on date
    And Approve EUR savings account on "1 June 2022" date
    And Activate EUR savings account on "1 June 2022" date
    And Client successfully deposits 1000 EUR to the savings account on "1 June 2022" date
    And Client successfully withdraw 1000 EUR from the savings account on "1 June 2022" date

  @TestRailId:C2440
  Scenario: As a user I would like to create an EUR and USD Savings accounts and make deposit and withdraw events
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin creates a EUR savings product
    And Client creates a new EUR savings account with "1 June 2022" submitted on date
    And Approve EUR savings account on "1 June 2022" date
    And Activate EUR savings account on "1 June 2022" date
    And Client creates a new USD savings account with "1 June 2022" submitted on date
    And Approve USD savings account on "1 June 2022" date
    And Activate USD savings account on "1 June 2022" date
    And Client successfully deposits 1000 EUR to the savings account on "1 June 2022" date
    And Client successfully withdraw 1000 EUR from the savings account on "1 June 2022" date
    And Client successfully deposits 1000 USD to the savings account on "1 June 2022" date

  @TestRailId:C2441
  Scenario: As a user I would like to verify that withdrawal fails when balance is insufficient
    When Admin sets the business date to "1 June 2022"
    And Admin creates a client with random data
    And Admin creates a EUR savings product
    And Client creates a new EUR savings account with "1 June 2022" submitted on date
    And Approve EUR savings account on "1 June 2022" date
    And Activate EUR savings account on "1 June 2022" date
    And Client successfully deposits 500 EUR to the savings account on "1 June 2022" date
    And Client tries to withdraw 1000 "EUR" from savings account on "1 June 2022" date and expects an error
    Then The savings account withdrawal error response has an HTTP status of 403
    And The savings account withdrawal developer message contains "Insufficient account balance."