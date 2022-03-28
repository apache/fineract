Feature: Classpath duplicates

  @infrastructure
  Scenario: Verify if any duplicates exist in the classpath
    Given A class graph
    When The user scans the class graph
    Then There should be no duplicates
