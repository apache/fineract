@SavingsAccount
  Feature: SavingsAccount
    
#    TODO activate when Swagger enhancement for PostSavingsProductsRequest is completed (also in SavingsProductRequestFactory)
    @Skip @TestRailId:C2438
    Scenario: As a user I would like to Deposit to my savings account
      When Admin sets the business date to "1 June 2022"
      When Admin creates a client with random data
      And Client creates a new EUR savings account with "1 June 2022" submitted on date
      And Approve EUR savings account on "1 June 2022" date
      And Activate EUR savings account on "1 June 2022" date
      And Client successfully deposits 1000 EUR to the savings account on "1 June 2022" date

    @Skip @TestRailId:C2439
    Scenario: As a user I would like to Withdraw from my savings account
      When Admin sets the business date to "1 June 2022"
      When Admin creates a client with random data
      And Client creates a new EUR savings account with "1 June 2022" submitted on date
      And Approve EUR savings account on "1 June 2022" date
      And Activate EUR savings account on "1 June 2022" date
      And Client successfully deposits 1000 EUR to the savings account on "1 June 2022" date
      And Client successfully withdraw 1000 EUR from the savings account on "1 June 2022" date

    @Skip @TestRailId:C2440
    Scenario: As a user I would like to create an EUR and USD Savings accounts and make deposit and withdraw events
      When Admin sets the business date to "1 June 2022"
      When Admin creates a client with random data
      And Client creates a new EUR savings account with "1 June 2022" submitted on date
      And Approve EUR savings account on "1 June 2022" date
      And Activate EUR savings account on "1 June 2022" date
      And Client creates a new USD savings account with "1 June 2022" submitted on date
      And Approve USD savings account on "1 June 2022" date
      And Activate USD savings account on "1 June 2022" date
      And Client successfully deposits 1000 EUR to the savings account on "1 June 2022" date
      And Client successfully withdraw 1000 EUR from the savings account on "1 June 2022" date
      And Client successfully deposits 1000 USD to the savings account on "1 June 2022" date
