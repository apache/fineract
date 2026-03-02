@WCCOBFeature @WC
Feature: Working Capital COB Job

  Background:
    Given Global configuration "enable-business-date" is enabled

  @TestRailId:C4695
  Scenario: Verify WC COB job registration, default business step, and scheduler metadata
    Then Admin checks that configured business jobs contain "WORKING_CAPITAL_LOAN_CLOSE_OF_BUSINESS"
    Then Admin verifies configured business steps for "WORKING_CAPITAL_LOAN_CLOSE_OF_BUSINESS" match:
      | stepName             | order |
      | DUMMY_BUSINESS_STEP  | 1     |
    Then Admin verifies scheduler job "WC_COB" has display name "Working Capital Loan COB"
    Then Admin verifies scheduler job "WC_COB" has active status "false"

  @TestRailId:C4696
  Scenario: WC COB and Loan COB coexistence — both jobs listed and execute without interference
    Then Admin checks that configured business jobs contain "LOAN_CLOSE_OF_BUSINESS"
    Then Admin checks that configured business jobs contain "WORKING_CAPITAL_LOAN_CLOSE_OF_BUSINESS"
    When Admin sets the business date to "01 January 2024"
    When Admin runs COB job
    When Admin runs WC COB job

  @TestRailId:C4697
  Scenario: WC COB executes on consecutive dates with Loan COB interleaved
    When Admin sets the business date to "01 January 2024"
    When Admin runs WC COB job
    When Admin runs COB job
    When Admin sets the business date to "02 January 2024"
    When Admin runs WC COB job

  @TestRailId:C4698
  Scenario: WC COB updates lastClosedBusinessDate for a single active loan
    # Behavioral test: inserts a WC loan via JDBC, runs COB, verifies lastClosedBusinessDate is set.
    When Admin sets the business date to "01 January 2024"
    Given Admin inserts an active WC loan into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"

  @TestRailId:C4699
  Scenario: WC COB processes multiple loans in a single run
    When Admin sets the business date to "01 January 2024"
    Given Admin inserts 3 active WC loans into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"

  @TestRailId:C4700
  Scenario: WC COB advances lastClosedBusinessDate over consecutive business dates
    When Admin sets the business date to "01 January 2024"
    Given Admin inserts an active WC loan into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    When Admin sets the business date to "02 January 2024"
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "01 January 2024"
    Then Admin verifies all inserted WC loans have version 2

  @TestRailId:C4701
  Scenario: WC COB does not reprocess loans already closed for the business date
    # Verifies idempotency — running COB twice on the same business date doesn't cause errors or duplicate processing.
    When Admin sets the business date to "01 January 2024"
    Given Admin inserts an active WC loan into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    Then Admin verifies all inserted WC loans have version 1

  @TestRailId:C4702
  Scenario: WC COB skips loans with ineligible status (closed obligations met)
    # COB only processes non-closed statuses: SUBMITTED_AND_PENDING_APPROVAL, APPROVED, ACTIVE,
    # TRANSFER_IN_PROGRESS, TRANSFER_ON_HOLD. Closed loans should be skipped.
    When Admin sets the business date to "01 January 2024"
    Given Admin inserts a WC loan with status "CLOSED_OBLIGATIONS_MET" into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have null lastClosedBusinessDate

  @TestRailId:C4703
  Scenario: WC COB processes loans with eligible non-active statuses
    When Admin sets the business date to "01 January 2024"
    Given Admin inserts a WC loan with status "SUBMITTED_AND_PENDING_APPROVAL" into the database
    Given Admin inserts a WC loan with status "APPROVED" into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"

  @TestRailId:C4704
  Scenario: WC COB processes loans with transfer statuses
    When Admin sets the business date to "01 January 2024"
    Given Admin inserts a WC loan with status "TRANSFER_IN_PROGRESS" into the database
    Given Admin inserts a WC loan with status "TRANSFER_ON_HOLD" into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"

  @TestRailId:C4705
  Scenario: WC COB skips loans already closed for the current business date
    When Admin sets the business date to "01 January 2024"
    Given Admin inserts a WC loan with status "ACTIVE" and lastClosedBusinessDate "31 December 2023" into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    Then Admin verifies all inserted WC loans have version 0

  @TestRailId:C4706
  Scenario: WC COB advances a loan that is exactly one day behind
    When Admin sets the business date to "02 January 2024"
    Given Admin inserts a WC loan with status "ACTIVE" and lastClosedBusinessDate "31 December 2023" into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "01 January 2024"

  @TestRailId:C4707
  Scenario: WC COB releases all account locks after completion
    # After COB completes, no lingering account locks should remain for processed loans.
    When Admin sets the business date to "01 January 2024"
    Given Admin inserts an active WC loan into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    Then Admin verifies all inserted WC loans have no account locks

  @TestRailId:C4708
  Scenario: WC COB handles a batch of 10 loans
    When Admin sets the business date to "01 January 2024"
    Given Admin inserts 10 active WC loans into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"

  @TestRailId:C4709
  Scenario: WC COB increments loan version after processing
    When Admin sets the business date to "01 January 2024"
    Given Admin inserts an active WC loan into the database
    Then Admin verifies all inserted WC loans have version 0
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    Then Admin verifies all inserted WC loans have version 1

  @TestRailId:C4710
  Scenario: WC COB processes eligible loans and skips ineligible ones in the same batch
    # Mix of eligible (ACTIVE) and ineligible (CLOSED_OBLIGATIONS_MET) loans — only eligible should be updated.
    When Admin sets the business date to "01 January 2024"
    Given Admin inserts an active WC loan into the database
    Given Admin inserts a WC loan with status "CLOSED_OBLIGATIONS_MET" into the database
    When Admin runs WC COB job
    Then Admin verifies inserted WC loan 1 has lastClosedBusinessDate "31 December 2023"
    Then Admin verifies inserted WC loan 2 has null lastClosedBusinessDate
