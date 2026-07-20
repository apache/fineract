@WorkingCapital
@WorkingCapitalBreach
@WorkingCapitalBreachPastDueAmountFeature
Feature: Working Capital Breach Past Due Amount

  @TestRailId:C85561
  Scenario: Verify that breach past due amount accumulates when a period ends unpaid
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 7               | DAYS                | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "0"
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-07 | 7            | 500.00           | 500.00            | null       | true   |
      | 2            | 2026-01-08 | 2026-01-14 | 7            | 500.00           | 500.00            | null       | null   |
    Then Working Capital loan balance has breach past due amount "500"

  @TestRailId:C85562
  Scenario: Verify that partial payment within the period reduces breach past due amount
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 7               | DAYS                | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Period 1 past due = MAX(0, 500 - 200) = 300
    Then Working Capital loan balance has breach past due amount "300"

  @TestRailId:C85563
  Scenario: Verify that a fully paid period contributes zero to breach past due amount
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 7               | DAYS                | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 500.0 transaction amount on Working Capital loan
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "0"

  @TestRailId:C85564
  Scenario: Verify that breach past due amount accumulates cumulatively across multiple unpaid periods
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 7               | DAYS                | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Both P1 (Jan 01-07) and P2 (Jan 08-14) end unpaid; P3 (Jan 15-21) is open
    When Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "1000"
    # Same value exposed through the loan account data table
    And Working capital loan account has the correct data:
      | breachPastDueAmount |
      | 1000.0              |

  @TestRailId:C85565
  Scenario: Verify that a period contributes to breach past due amount from the COB run after its toDate (boundary)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 7               | DAYS                | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # COB on P1.toDate (Jan 07) evaluates with COB date Jan 06 - period not completed yet
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "0"
    # COB on Jan 08 evaluates with COB date Jan 07 (= P1.toDate) - period now counts
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "500"

  @TestRailId:C85566
  Scenario: Verify that a forward repayment into the open period does not reduce past due while a backdated repayment recalculates it immediately
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 7               | DAYS                | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "500"
    # Forward repayment lands in the still-open P2 - past due of the completed P1 is untouched
    When Customer makes repayment on "09 January 2026" with 300.0 transaction amount on Working Capital loan
    Then Working Capital loan balance has breach past due amount "500"
    # Backdated repayment dated inside P1 settles it - recalculated immediately, no COB needed (AC3)
    When Customer makes repayment on "05 January 2026" with 500.0 transaction amount on Working Capital loan
    Then Working Capital loan balance has breach past due amount "0"

  @TestRailId:C85567
  Scenario: Verify that undoing a repayment restores breach past due amount immediately
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 7               | DAYS                | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 500.0 transaction amount on Working Capital loan
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "0"
    When Customer undo "1"th working capital transaction made on "05 January 2026"
    Then Working Capital loan balance has breach past due amount "500"

  @TestRailId:C85568
  Scenario: Verify that breach reschedule drives past due of subsequent periods with the new minimum payment
    # AC4: after a breach reschedule the past due amount is computed on the revised schedule
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 7               | DAYS                | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "500"
    # Reschedule lowers the minimum payment for the current (P2) and future periods to 250
    When Admin creates WC breach reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType |
      | 250            | FLAT               |
    # P1 keeps its pre-reschedule 500; P2 (Jan 08-14) ends unpaid with the NEW minimum 250
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "750"

  @TestRailId:C85569
  Scenario: Verify that breach pause retains existing past due and defers the paused period contribution
    # AC5: pause has no impact on already accumulated past due; the paused period contributes
    # only once its extended toDate has passed. Pause/resume now also refresh the value directly.
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 7               | DAYS                | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "500"
    # 5 paused days (Jan 09-13) extend P2 toDate from Jan 14 to Jan 19
    When Admin initiate a Working Capital loan breach pause with startDate "09 January 2026" and endDate "13 January 2026"
    Then Working Capital loan balance has breach past due amount "500"
    # Original P2 end has passed but the extended period is still open - no new contribution
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "500"
    # Extended P2 (toDate Jan 19) has now completed unpaid
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "1000"

  @TestRailId:C85570
  Scenario: Verify that breach disable freezes past due calculation and enable resumes it
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 7               | DAYS                | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "500"
    When Admin sets the business date to "10 January 2026"
    And Admin initiate a Working Capital loan breach disable with startDate "10 January 2026"
    # P2 (Jan 08-14) completes unpaid during the disabled window - value stays frozen
    When Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "500"
    When Admin sets the business date to "17 January 2026"
    And Admin initiate a Working Capital loan breach enable with startDate "17 January 2026"
    When Admin sets the business date to "18 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "1000"

  @TestRailId:C85571
  Scenario: Verify that breach reset clears past due amount immediately
    # Note 2: past due carries forward until fully resolved or reset. The reset action refreshes
    # the value directly - no COB run needed.
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 7               | DAYS                | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "500"
    When Admin sets the business date to "10 January 2026"
    And Admin creates WC breach reset action
    # Reset periods are excluded from the calculation and the balance refreshes immediately
    Then Working Capital loan balance has breach past due amount "0"
    # Value stays 0 after the next COB as well
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "0"

  @TestRailId:C85572
  Scenario: Verify that an excess forward repayment does not retroactively settle completed periods
    # payment settles ONLY the period containing its transaction date.
    # The excess above the current period's minimum is not carried to past
    # periods, and a backdated payment reduces exactly the period it is dated in.
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 7               | DAYS                | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "500"
    # Excess forward repayment (800 > P2 minimum 500) - completed P1 stays past due
    When Customer makes repayment on "09 January 2026" with 800.0 transaction amount on Working Capital loan
    Then Working Capital loan balance has breach past due amount "500"
    # Backdated repayment of 10 into P1 reduces exactly P1: 500 - 10 = 490
    When Customer makes repayment on "05 January 2026" with 10.0 transaction amount on Working Capital loan
    Then Working Capital loan balance has breach past due amount "490"

  @TestRailId:C85573
  Scenario: Verify breach past due amount with percentage based breach amount calculation
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 7               | DAYS                | PERCENTAGE                  | 9            |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Minimum payment = (9000 principal + 1000 discount) * 9% = 900
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "900"
