Feature: Batch Service

  @batch
  Scenario: Verify that batch builder methods are called
    Given A batch request
    When The user calls the batch service handle request method without enclosing transaction
    Then The batch service handle request method without enclosing transaction should have been called
