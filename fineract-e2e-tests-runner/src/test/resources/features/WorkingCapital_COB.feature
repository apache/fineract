@WorkingCapital
@WCCOBFeature
Feature: Working Capital COB Job

  Background:
    Given Global configuration "enable-business-date" is enabled

  @TestRailId:C4695
  Scenario: Verify WC COB job registration, default business step, and scheduler metadata
    Then Admin checks that configured business jobs contain "WORKING_CAPITAL_LOAN_CLOSE_OF_BUSINESS"
    Then Admin verifies configured business steps for "WORKING_CAPITAL_LOAN_CLOSE_OF_BUSINESS" match:
      | stepName                           | order |
      | DUMMY_BUSINESS_STEP                | 1     |
      | WC_DELINQUENCY_RANGE_SCHEDULE      | 2     |
      | WC_LOAN_DELINQUENCY_CLASSIFICATION | 3     |
      | WC_BREACH_SCHEDULE                 | 4     |
      | WC_NEAR_BREACH_EVALUATION          | 5     |
      | WC_DISCOUNT_FEE_AMORTIZATION       | 6     |
      | WC_CHARGE_ACCRUAL                  | 7     |
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

  @TestRailId:C70320
  Scenario: WC COB updates lastClosedBusinessDate for a single active loan
    # Behavioral test: inserts a WC loan via JDBC, runs COB, verifies lastClosedBusinessDate is set.
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts an active WC loan into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"

  @TestRailId:C70231
  Scenario: WC COB processes multiple loans in a single run
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts 3 active WC loans into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"

  @TestRailId:C70232
  Scenario: WC COB advances lastClosedBusinessDate over consecutive business dates
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts an active WC loan into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    When Admin sets the business date to "02 January 2024"
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "01 January 2024"
    Then Admin verifies all inserted WC loans have version 2

  @TestRailId:C70233
  Scenario: WC COB does not reprocess loans already closed for the business date
    # Verifies idempotency — running COB twice on the same business date doesn't cause errors or duplicate processing.
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts an active WC loan into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    Then Admin verifies all inserted WC loans have version 1

  @TestRailId:C70234
  Scenario: WC COB skips loans with ineligible status (closed obligations met)
    # COB only processes non-closed statuses: SUBMITTED_AND_PENDING_APPROVAL, APPROVED, ACTIVE,
    # TRANSFER_IN_PROGRESS, TRANSFER_ON_HOLD. Closed loans should be skipped.
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts a WC loan with status "CLOSED_OBLIGATIONS_MET" into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have null lastClosedBusinessDate

  @TestRailId:C70235
  Scenario: WC COB processes loans with eligible non-active statuses
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts a WC loan with status "SUBMITTED_AND_PENDING_APPROVAL" into the database
    Given Admin inserts a WC loan with status "APPROVED" into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"

  @TestRailId:C70236
  Scenario: WC COB processes loans with transfer statuses
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts a WC loan with status "TRANSFER_IN_PROGRESS" into the database
    Given Admin inserts a WC loan with status "TRANSFER_ON_HOLD" into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"

  @TestRailId:C70237
  Scenario: WC COB skips loans already closed for the current business date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts a WC loan with status "ACTIVE" and lastClosedBusinessDate "31 December 2023" into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    Then Admin verifies all inserted WC loans have version 0

  @TestRailId:C70238
  Scenario: WC COB advances a loan that is exactly one day behind
    When Admin sets the business date to "02 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts a WC loan with status "ACTIVE" and lastClosedBusinessDate "31 December 2023" into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "01 January 2024"

  @TestRailId:C70239
  Scenario: WC COB releases all account locks after completion
    # After COB completes, no lingering account locks should remain for processed loans.
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts an active WC loan into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    Then Admin verifies all inserted WC loans have no account locks

  @TestRailId:C70240
  Scenario: WC COB handles a batch of 10 loans
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts 10 active WC loans into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"

  @TestRailId:C70241
  Scenario: WC COB increments loan version after processing
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts an active WC loan into the database
    Then Admin verifies all inserted WC loans have version 0
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    Then Admin verifies all inserted WC loans have version 1

  @TestRailId:C70242
  Scenario: WC COB processes eligible loans and skips ineligible ones in the same batch
    # Mix of eligible (ACTIVE) and ineligible (CLOSED_OBLIGATIONS_MET) loans — only eligible should be updated.
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts an active WC loan into the database
    Given Admin inserts a WC loan with status "CLOSED_OBLIGATIONS_MET" into the database
    When Admin runs WC COB job
    Then Admin verifies inserted WC loan 1 has lastClosedBusinessDate "31 December 2023"
    Then Admin verifies inserted WC loan 2 has null lastClosedBusinessDate

    #----------#
  @TestRailId:C70243
  Scenario: Inline WC COB processes a single loan and releases locks
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts an active WC loan into the database
    When Admin runs inline COB job for Working Capital Loan
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    Then Admin verifies all inserted WC loans have no account locks
    Then Admin verifies all inserted WC loans have version 1

  @TestRailId:C70244
  Scenario: Inline WC COB processes multiple loans in a single request
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts 3 active WC loans into the database
    When Admin runs inline COB job for all Working Capital Loans
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    Then Admin verifies all inserted WC loans have version 1

  @TestRailId:C70245
  Scenario: Inline WC COB and batch WC COB coexistence
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts an active WC loan into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    Then Admin verifies all inserted WC loans have version 1
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Working Capital Loan
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "01 January 2024"
    Then Admin verifies all inserted WC loans have version 2

  @TestRailId:C70246
  Scenario: Inline WC COB advances lastClosedBusinessDate over consecutive business dates
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts an active WC loan into the database
    When Admin runs inline COB job for Working Capital Loan
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    When Admin sets the business date to "02 January 2024"
    When Admin runs inline COB job for Working Capital Loan
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "01 January 2024"
    Then Admin verifies all inserted WC loans have version 2

  @TestRailId:C70247
  Scenario: Inline WC COB advances lastClosedBusinessDate over skipped business dates
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts an active WC loan into the database
    When Admin runs inline COB job for Working Capital Loan
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    When Admin sets the business date to "05 January 2024"
    When Admin runs inline COB job for Working Capital Loan
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "04 January 2024"
    Then Admin verifies all inserted WC loans have version 5

  @TestRailId:C70248
  Scenario: Working Capital COB catch up advances lastClosedBusinessDate over skipped business dates
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts an active WC loan into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    When Admin sets the business date to "05 January 2024"
    When Admin runs Working Capital COB catch up
    When Admin runs COB catch up
    Then Admin checks that WC Loan COB is running until the current business date
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "04 January 2024"
    Then Admin verifies all inserted WC loans have version 5

  @TestRailId:C70249
  Scenario: WC COB catch-up is skipped when loans are already up to date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts an active WC loan into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    Then Admin verifies all inserted WC loans have version 1
    When Admin runs Working Capital COB catch up
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    Then Admin verifies all inserted WC loans have version 1

  Scenario: WC COB removes an orphaned lock with no error on a loan already processed for that COB date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts an active WC loan into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    When Admin places a chunk-processing lock without an error message on the last inserted WC loan
    Then Admin verifies all inserted WC loans have at least one account lock
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have no account locks

  Scenario: WC COB keeps a lock that carries an error message
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts an active WC loan into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    When Admin places a chunk-processing lock with error "ERROR" on the last inserted WC loan
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have at least one account lock

  Scenario: WC COB keeps a lock when the loan's last closed business date does not match the lock's COB date
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts an active WC loan into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    # Skip a few days so the lock's cob_date is far ahead of the loan's last_closed (31 Dec 2023).
    # Even if WC COB advances last_closed by one day during the next run, it won't reach the lock's cob_date.
    When Admin sets the business date to "05 January 2024"
    When Admin places a chunk-processing lock without an error message on the last inserted WC loan
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have at least one account lock

  Scenario: WC COB removes an orphaned inline-COB lock with no error on a processed loan
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts an active WC loan into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    When Admin places an inline-COB lock without an error message on the last inserted WC loan
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have no account locks

  Scenario: WC COB keeps an inline-COB lock that carries an error message
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts an active WC loan into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    When Admin places an inline-COB lock with error "ERROR" on the last inserted WC loan
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have at least one account lock

  Scenario: WC COB keeps a lock with NULL cob business date
    # SQL filter requires `lock_placed_on_cob_business_date IS NOT NULL` — a lock with NULL date must never be removed.
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts an active WC loan into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    When Admin places a chunk-processing lock without an error message and null cob business date on the last inserted WC loan
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have at least one account lock

  Scenario: WC COB keeps a lock when cob business date is in the past relative to last closed date
    # SQL uses strict equality on dates — a stale lock whose cob_date already lags behind last_closed must remain.
    When Admin sets the business date to "02 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts a WC loan with status "ACTIVE" and lastClosedBusinessDate "31 December 2023" into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "01 January 2024"
    # Place a lock claiming cob_date = 31 Dec 2023 — earlier than the loan's current last_closed (01 Jan 2024).
    When Admin places a chunk-processing lock without an error message and cob business date "31 December 2023" on the last inserted WC loan
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have at least one account lock

  Scenario: WC COB removes only orphaned locks among multiple loans in the same run
    # Two loans share the same COB run: one carries an orphaned lock (no error) and must be unlocked;
    # the other carries a lock with an error message and must remain locked. Verifies DELETE selectivity.
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a new Working Capital Loan Product
    Given Admin inserts 2 active WC loans into the database
    When Admin runs WC COB job
    Then Admin verifies all inserted WC loans have lastClosedBusinessDate "31 December 2023"
    When Admin places a chunk-processing lock without an error message on WC loan 1
    When Admin places a chunk-processing lock with error "ERROR" on WC loan 2
    When Admin runs WC COB job
    Then Admin verifies inserted WC loan 1 has no account locks
    Then Admin verifies inserted WC loan 2 has at least one account lock

  Scenario: WC COB run properly uses .before/after listeners
    When Admin clears internal working capital cob last run data
    Then Admin verifies internal working capital cob last run data values are empty "true"
    When Admin runs WC COB job
    Then Admin verifies internal working capital cob last run data values are empty "false"
