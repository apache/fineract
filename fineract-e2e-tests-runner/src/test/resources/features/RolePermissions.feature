@RolePermissionsFeature
Feature: Role permissions

  Scenario: A user can only grant permissions that the user holds
    When Admin creates a role named "INPUT_VALIDATION_TARGET_ROLE"
    When Admin creates new user with "INPUT_VALIDATION_ROLE_EDITOR" username, "INPUT_VALIDATION_ROLE_EDITOR_ROLE" role name and given permissions:
      | READ_ROLE        |
      | UPDATE_ROLE      |
      | PERMISSIONS_ROLE |
    Then Created user can grant permission "READ_ROLE" to the last created role
    Then Created user cannot grant permission "ALL_FUNCTIONS" to the last created role
    Then Created user cannot grant permission "CREATE_LOAN" to the last created role

  Scenario: The super user role can only be modified by a user holding all functions
    When Admin creates new user with "INPUT_VALIDATION_SUPER_ROLE" username, "INPUT_VALIDATION_SUPER_ROLE_ROLE" role name and given permissions:
      | READ_ROLE        |
      | UPDATE_ROLE      |
      | PERMISSIONS_ROLE |
    Then Created user cannot modify the permissions of the super user role
    Then Admin can modify the permissions of the super user role
