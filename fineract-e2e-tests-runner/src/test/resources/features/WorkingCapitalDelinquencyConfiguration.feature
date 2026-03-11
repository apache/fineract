@WCDelinquencyCFeature @WC
Feature: Working Capital Delinquency Configuration

  @TestRailId:CXXXX
  Scenario: Verify Working Capital Delinquency Configuration CRUD
    When Admin Calls Delinquency Template
    When Admin creates WC Delinquency Bucket With Values
    Then Get Delinquency Bucket has the following values
    Then Get Delinquency Bucket With Template has the following values
    When Admin modifies WC Delinquency Bucket With Values
    Then Get Delinquency Bucket has the following values
    When Admin deletes WC Delinquency Bucket With Values