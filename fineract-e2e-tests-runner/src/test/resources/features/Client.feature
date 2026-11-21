@ClientFeature
Feature: Client

  @TestRailId:C14 @Smoke
  Scenario Outline: Client creation functionality for Fineract
    When Admin creates a client with Firstname <firstName> and Lastname <lastName>
    Then Client is created successfully
    Examples:
      | firstName    | lastName |
      | "FirstName1" | "Test1"  |


  @TestRailId:C15
  Scenario Outline: Client creation with address functionality for Fineract
    When Global configuration "enable-address" is enabled
    When Admin creates a client with Firstname <firstName> and Lastname <lastName> with address
    Then Client is created successfully
    Examples:
      | firstName    | lastName |
      | "FirstName2" | "Test2"  |

  @TestRailId:C119630
  Scenario: Verify recoverable idempotent error on client creation - UC1: retry executes after business date moves, success replays from cache
    When Admin sets the business date to "14 January 2025"
# --- Failing attempt on lagged business date ---
    When Admin creates an active client with activation date "15 January 2025" and idempotency key
    Then The client creation attempt returned the following error:
      | httpCode | errorMessage                                   |
      | 400      | error.msg.clients.activationDate.in.the.future |
    Then The client creation attempt was not served from cache
    Then Exactly 0 clients exist with the stored externalId
# --- Business date catches up, retry executes for real ---
    When Admin sets the business date to "15 January 2025"
    When Admin retries the stored client request with the stored idempotency key
    Then The client creation attempt returned 200 and was not served from cache
    Then The client creation attempt returned a clientId
    Then Exactly 1 client exists with the stored externalId
# --- Audit check: the processed attempt created its own audit row ---
    Then The audit search for action "CREATE" on entity "CLIENT" filtered by resourceId of the created client returns 1 entries
    And The returned audit entry has the following data:
      | field            | value          |
      | resourceId       | created client |
      | processingResult | Processed      |
# --- Third call replays the processed result ---
    When Admin retries the stored client request with the stored idempotency key
    Then The client creation attempt returned 200 served from cache with the same clientId
    Then Exactly 1 client exists with the stored externalId

  @TestRailId:C119631
  Scenario: Verify recoverable idempotent error on client creation - UC2: every attempt keeps its own audit row
    When Admin sets the business date to "14 January 2025"
    When Admin creates new user with "IDEMPOTENCY_MAKER" username, "IDEMPOTENCY_MAKER_ROLE" role name and given permissions:
      | CREATE_CLIENT   |
      | ACTIVATE_CLIENT |
# --- Two failing attempts on lagged business date ---
    When Created user creates an active client with activation date "15 January 2025" and idempotency key
    Then The client creation attempt returned the following error:
      | httpCode | errorMessage                                   |
      | 400      | error.msg.clients.activationDate.in.the.future |
    Then The client creation attempt was not served from cache
    When Created user retries the stored client request with the stored idempotency key
    Then The client creation attempt returned the following error:
      | httpCode | errorMessage                                   |
      | 400      | error.msg.clients.activationDate.in.the.future |
    Then The client creation attempt was not served from cache
# --- Business date catches up, retry succeeds ---
    When Admin sets the business date to "15 January 2025"
    When Created user retries the stored client request with the stored idempotency key
    Then The client creation attempt returned 200 and was not served from cache
    Then The client creation attempt returned a clientId
# --- Audit check: all three attempts have their own row ---
    Then The audit search for action "CREATE" on entity "CLIENT" filtered by maker of the created user returns 3 entries
    Then The returned audit entries have the following processing results in call order:
      | Error     |
      | Error     |
      | Processed |
    Then Exactly 1 client exists with the stored externalId

  @TestRailId:C119632
  Scenario: Verify recoverable idempotent error on client creation - UC3: unlisted error code replays the cached failure
    When Admin sets the business date to "14 January 2025"
# --- Failing attempt with a non-configured error code ---
    When Admin creates a pending client with submitted on date "15 January 2025" and idempotency key
    Then The client creation attempt returned the following error:
      | httpCode | errorMessage                                     |
      | 400      | error.msg.clients.submittedOnDate.in.the.future |
    Then The client creation attempt was not served from cache
# --- Business date catches up but the error is not recoverable: cached 400 is replayed ---
    When Admin sets the business date to "15 January 2025"
    When Admin retries the stored client request with the stored idempotency key
    Then The client creation attempt returned the following error:
      | httpCode | errorMessage                                     |
      | 400      | error.msg.clients.submittedOnDate.in.the.future |
    Then The client creation attempt was served from cache
    Then The client creation attempt returned the same failure body
    Then Exactly 0 clients exist with the stored externalId

  @TestRailId:C119633
  Scenario: Verify recoverable idempotent error on client creation - UC4: code removed from configuration replays the cached failure
    When Admin sets the business date to "14 January 2025"
# --- Remove the client activation code from the recoverable list ---
    When Global config "idempotency-recoverable-error-codes" value set to "error.msg.group.activationDate.in.the.future"
    When Admin creates an active client with activation date "15 January 2025" and idempotency key
    Then The client creation attempt returned the following error:
      | httpCode | errorMessage                                   |
      | 400      | error.msg.clients.activationDate.in.the.future |
    Then The client creation attempt was not served from cache
# --- Business date catches up but the code is no longer recoverable: cached 400 is replayed ---
    When Admin sets the business date to "15 January 2025"
    When Admin retries the stored client request with the stored idempotency key
    Then The client creation attempt returned the following error:
      | httpCode | errorMessage                                   |
      | 400      | error.msg.clients.activationDate.in.the.future |
    Then The client creation attempt was served from cache
    Then The client creation attempt returned the same failure body
    Then Exactly 0 clients exist with the stored externalId
# --- Restore default configuration ---
    When Global config "idempotency-recoverable-error-codes" value set to "error.msg.clients.activationDate.in.the.future,error.msg.group.activationDate.in.the.future"

  @TestRailId:C119634
  Scenario: Verify recoverable idempotent error on client creation - UC5: concurrent retries create exactly one client
    When Admin sets the business date to "14 January 2025"
    When Admin creates an active client with activation date "15 January 2025" and idempotency key
    Then The client creation attempt returned the following error:
      | httpCode | errorMessage                                   |
      | 400      | error.msg.clients.activationDate.in.the.future |
    Then The client creation attempt was not served from cache
# --- Business date catches up, two parallel retries race for the freed key ---
    When Admin sets the business date to "15 January 2025"
    When Two admins create the client concurrently with the stored idempotency key
    Then Exactly one of the two attempts returned 200 and was not served from cache
    Then The other attempt returned one of the following:
      | httpCode | servedFromCache |
      | 425      | false           |
      | 200      | true            |
    Then Exactly 1 client exists with the stored externalId

  @TestRailId:C119635
  Scenario: Verify recoverable idempotent error on client creation - UC6: a client key ending with the rename suffix is accepted and unaffected by the rename
    When Admin sets the business date to "14 January 2025"
    When Admin prepares an idempotency key
# --- A client may send a key of the form "<key>_RE1" itself ---
    When Admin creates an active client with activation date "14 January 2025" and idempotency key suffix "_RE1"
    Then The client creation attempt returned 200 and was not served from cache
    Then The client creation attempt returned a clientId
    Then Exactly 1 client exists with the stored second externalId
# --- A recoverable failure under "<key>" is renamed to "<key>_RE<row id>", leaving "<key>_RE1" alone ---
    When Admin creates an active client with activation date "15 January 2025" and the stored idempotency key
    Then The client creation attempt returned the following error:
      | httpCode | errorMessage                                   |
      | 400      | error.msg.clients.activationDate.in.the.future |
    Then The client creation attempt was not served from cache
# --- Business date catches up, retry under "<key>" executes ---
    When Admin sets the business date to "15 January 2025"
    When Admin retries the stored client request with the stored idempotency key
    Then The client creation attempt returned 200 and was not served from cache
    Then The client creation attempt returned a clientId
    Then Exactly 1 client exists with the stored externalId
# --- "<key>_RE1" still replays its own result ---
    When Admin retries the stored second client request with the stored second idempotency key
    Then The client creation attempt returned 200 served from cache with the same clientId
    Then Exactly 1 client exists with the stored second externalId
