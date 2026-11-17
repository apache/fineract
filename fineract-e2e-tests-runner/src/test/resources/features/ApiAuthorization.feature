@ApiAuthorizationFeature
Feature: API authorization

  Scenario: Verify a user without the required permissions is refused by the request matcher rules
    When Admin creates new user with "API_AUTHZ_NO_PERMISSION" username, "API_AUTHZ_NO_PERMISSION_ROLE" role name and given permissions:
      | READ_OFFICE |
    Then Created user is refused when it calls the API to "list hooks"
    Then Created user is refused when it calls the API to "create a hook"
    Then Created user is refused when it calls the API to "list staff"
    Then Created user is refused when it calls the API to "create a staff member"
    Then Created user is refused when it calls the API to "list payment types"
    Then Created user is refused when it calls the API to "update a payment type"
    Then Created user is refused when it calls the API to "delete a payment type"
    Then Created user is refused when it calls the API to "list business dates"
    Then Created user is refused when it calls the API to "update currencies"
    Then Created user is refused when it calls the API to "list meetings of a group"
    Then Created user is refused when it calls the API to "list templates"
    Then Created user is refused when it calls the API to "list interest rate charts"
    Then Created user is refused when it calls the API to "read the product mix of a loan product"
    Then Created user is refused when it calls the API to "read a code"
    Then Created user is refused when it calls the API to "list tellers"
    Then Created user is refused when it calls the API to "list ad hoc queries"
    Then Created user is refused when it calls the API to "list provisioning criteria"
    Then Created user is refused when it calls the API to "list surveys"

  Scenario: Verify a user granted a read permission can call the routes of that permission only
    When Admin creates new user with "API_AUTHZ_READ_PERMISSIONS" username, "API_AUTHZ_READ_PERMISSIONS_ROLE" role name and given permissions:
      | READ_HOOK        |
      | READ_PAYMENTTYPE |
      | READ_STAFF       |
      | READ_TELLER      |
      | READ_ADHOC       |
    Then Created user can call the API to "list hooks"
    Then Created user can call the API to "list payment types"
    Then Created user can call the API to "list staff"
    Then Created user can call the API to "list tellers"
    Then Created user can call the API to "list ad hoc queries"
    Then Created user is refused when it calls the API to "create a hook"
    Then Created user is refused when it calls the API to "list templates"

  Scenario: Verify the permissions of the request matcher rules are available for roles
    Then Permission "READ_SHAREACCOUNT" is returned with grouping "SHAREACCOUNT", entity "SHAREACCOUNT" and action "READ"
    Then Permission "READ_TELLER" is returned with grouping "cash_mgmt", entity "TELLER" and action "READ"
    Then Permission "READ_SURVEY" is returned with grouping "survey", entity "SURVEY" and action "READ"
    Then Permission "UPDATE_CLIENTIMAGE" is returned with grouping "portfolio", entity "CLIENTIMAGE" and action "UPDATE"
