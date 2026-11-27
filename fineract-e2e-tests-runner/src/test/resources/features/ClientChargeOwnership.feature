@ClientChargeOwnershipFeature
Feature: Client charge ownership

  Scenario: A client charge is only found through the client it belongs to
    When Admin sets the business date to "1 January 2025"
    When Admin creates two clients and adds a charge to the second client
    Then Waiving the client charge through the first client is not found
    Then Paying the client charge through the first client is not found
    Then Paying the client charge through the second client succeeds
