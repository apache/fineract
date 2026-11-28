@ExternalServicesAndHooksFeature
Feature: External services and hooks

  @ExternalServicePropertiesRestore
  Scenario: Secret external service properties are returned masked
    When Admin sets the S3 external service properties s3_access_key to "ClearAccessKey123" and s3_secret_key to "ClearSecretKey456"
    Then The S3 external service properties are returned without the values "ClearAccessKey123" and "ClearSecretKey456"

  Scenario Outline: A hook with an internal payload address is rejected
    Then Creating a hook with payload URL "<url>" is rejected with a validation error

    Examples:
      | url                       |
      | http://127.0.0.1:9/       |
      | http://169.254.169.254/   |
