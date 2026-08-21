@WorkingCapital
@WorkingCapitalNearBreachEvaluationFeature
Feature: Working Capital Near Breach Evaluation

  @TestRailId:C76635
  Scenario: Verify near breach detected when outstanding exceeds threshold at evaluation date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 3               | MONTHS              | FLAT                        | 900          | 60                  | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Period 1: 01-01 -> 03-31, freq=60d -> 1 eval at 03-02 (cumulative required = 33.33% of 900 = 299.97)
    # No payment by 03-02 -> cumulative paid=0 < 299.97 -> trigger Y
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-31 | 900.00           | 900.00            | true       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "03 March 2026"

  @TestRailId:C76636
  Scenario: Verify near breach not triggered when payment brings outstanding below threshold before evaluation date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 3               | MONTHS              | FLAT                        | 900          | 60                  | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Period 1: 01-01 -> 03-31, freq=60d -> 1 eval at 03-02 (cumulative required = 299.97)
    # Pay 700 on 15 Feb -> cumulative paid by 03-02 = 700 >= 299.97 -> not trigger
    # After period end (31 Mar) -> nearBreach=false; outstanding=200>0 -> breach=true
    When Admin sets the business date to "15 February 2026"
    And Customer makes repayment on "15 February 2026" with 700.0 transaction amount on Working Capital loan
    When Admin sets the business date to "01 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-31 | 900.00           | 200.00            | false      | true   |
      | 2            | 2026-04-01 | 2026-06-30 | 900.00           | 900.00            | null       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "01 April 2026"

  @TestRailId:C76637
  Scenario: Verify near breach null when no near breach config on product
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | MONTHS              | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 500.00           | 500.00            | null       | true   |
      | 2            | 2026-02-01 | 2026-02-28 | 500.00           | 500.00            | null       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "01 February 2026"

  @TestRailId:C76638
  Scenario: Verify near breach is immutable - stays true after subsequent payment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 3               | MONTHS              | FLAT                        | 900          | 60                  | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # No payment by 03-02 -> cumulative paid=0 < 299.97 -> trigger Y at eval 03-02
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-31 | 900.00           | 900.00            | true       | null   |
    # Now pay full amount - near breach must stay true (immutable)
    When Admin sets the business date to "15 March 2026"
    And Customer makes repayment on "15 March 2026" with 900.0 transaction amount on Working Capital loan
    When Admin sets the business date to "01 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-31 | 900.00           | 0.00              | true       | false  |
      | 2            | 2026-04-01 | 2026-06-30 | 900.00           | 900.00            | null       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "01 April 2026"

  @TestRailId:C76640
  Scenario: Verify near breach evaluation before eval date - near breach stays null
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 3               | MONTHS              | FLAT                        | 900          | 60                  | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # freq=60d -> 1 eval at 03-01. COB on 28 Feb -> evalDate not yet passed -> nearBreach stays null
    When Admin sets the business date to "28 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-31 | 900.00           | 900.00            | null       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "28 February 2026"

  @TestRailId:C76641
  Scenario: Verify near breach with PERCENTAGE breach amount and WEEKS near breach frequency
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 2               | MONTHS              | PERCENTAGE                  | 10           | 2                   | WEEKS                   | 50                  |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Period 1: 01-01 -> 02-28 (2 months -1 day), minPayment=10% of 9000=900
    # freq=2 weeks -> 4 evals: 01-15, 01-29, 02-12, 02-26 (step required = 50% of 900 = 450)
    # No payment by eval#1 -> cumulative paid=0 < 450 -> trigger Y at eval#1
    When Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 900.00           | 900.00            | true       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "16 January 2026"

  @TestRailId:C76642
  Scenario: Verify near breach not triggered when outstanding equals threshold exactly - strict greater than
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 3               | MONTHS              | FLAT                        | 900          | 60                  | DAYS                    | 50                  |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # freq=60d -> 1 eval at 03-02 (cumulative required = 50% of 900 = 450)
    # Pay 450 on 15 Jan -> cumulative paid=450; strict less-than means 450 is NOT below 450 -> not trigger
    # After period end -> nearBreach=false; outstanding=450>0 -> breach=true
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 450.0 transaction amount on Working Capital loan
    When Admin sets the business date to "01 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-31 | 900.00           | 450.00            | false      | true   |
      | 2            | 2026-04-01 | 2026-06-30 | 900.00           | 900.00            | null       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "01 April 2026"

  @TestRailId:C76643
  Scenario: Verify near breach evaluated independently per breach period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 1               | MONTHS              | FLAT                        | 500          | 15                  | DAYS                    | 50                  |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Period 1: 01-01 -> 01-31, freq=15d -> 1 applicable eval at 01-16; 01-31 is the breach due date and excluded.
    # No payment in P1 by eval#1 -> cumulative paid=0 < 250 -> nearBreach=true at eval#1
    # Period 2: 02-01 -> 02-28, 1 eval at 02-16; pay 300 in P2 -> cumulative paid=300 >= 250 -> not trigger
    # Run COB first so period 2 is generated, then pay 300 in period 2
    When Admin sets the business date to "05 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "05 February 2026" with 300.0 transaction amount on Working Capital loan
    When Admin sets the business date to "01 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 500.00           | 500.00            | true       | true   |
      | 2            | 2026-02-01 | 2026-02-28 | 500.00           | 200.00            | false      | true   |
      | 3            | 2026-03-01 | 2026-03-31 | 500.00           | 500.00            | null       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "01 March 2026"

  @TestRailId:C76644
  Scenario: Verify near breach with non-zero grace days shifts breach period and eval dates
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 3               | MONTHS              | FLAT                        | 900          | 10              | 60                  | DAYS                    | 33.33               | 10                   |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # graceDays=10 -> Period 1: 01-01 -> 04-10; freq=60d -> 1 eval at 03-12 (cumulative required = 33.33% of 900 = 299.97)
    # No payment by 03-12 -> cumulative paid=0 < 299.97 -> trigger Y
    When Admin sets the business date to "13 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-04-10 | 900.00           | 900.00            | true       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "13 March 2026"

  @TestRailId:C76645
  Scenario: Verify near breach with PERCENTAGE breach amount and non-zero discount
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 2               | MONTHS              | PERCENTAGE                  | 10           | 30                  | DAYS                    | 50                  |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 500      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "500" discount amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "500" discount amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # minPayment = 10% of (9000 + 500 discount) = 950; freq=30d -> 1 eval at 01-31 (cumulative required = 50% of 950 = 475)
    # No payment by 01-31 -> cumulative paid=0 < 475 -> trigger Y
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 950.00           | 950.00            | true       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "01 February 2026"

  @TestRailId:C76646
  Scenario: Verify near breach stays null when eval date falls outside period due to February short month
    # Near breach freq=29 DAYS passes validation vs 1 MONTH (29 < 30 in comparator)
    # But in February (28 days), eval date = Feb 1 + 29 = Mar 2 which is outside the period
    When Admin sets the business date to "01 February 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 1               | MONTHS              | FLAT                        | 500          | 29                  | DAYS                    | 50                  |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 February 2026 | 01 February 2026         | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 February 2026" with "9000" amount and expected disbursement date on "01 February 2026"
    When Admin successfully disburse the Working Capital loan on "01 February 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "01 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-02-01 | 2026-02-28 | 500.00           | 500.00            | null       | true   |
      | 2            | 2026-03-01 | 2026-03-31 | 500.00           | 500.00            | true       | true   |
      | 3            | 2026-04-01 | 2026-04-30 | 500.00           | 500.00            | null       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "01 April 2026"

  @TestRailId:C76647
  Scenario: Verify near breach is not evaluated when eval date falls exactly on breach due date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 2               | MONTHS              | FLAT                        | 500          | 59                  | DAYS                    | 50                  |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # P1: 01-01 -> 02-28 (2 months). freq=58d -> candidate eval at 02-28 == toDate (breach due date).
    # Per spec: no near-breach evaluation on breach due date -> eval excluded. Period has zero applicable eval points.
    # Close-out at period end: no near-breach detected -> nearBreach=false (last-value contract; never null after period end).
    # Breach evaluation at period end: outstanding=500>0 -> breach=true.
    When Admin sets the business date to "01 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 500.00           | 500.00            | false      | true   |
      | 2            | 2026-03-01 | 2026-04-30 | 500.00           | 500.00            | null       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "01 March 2026"

  @TestRailId:C76648
  Scenario: Verify near breach not triggered with multiple partial payments bringing outstanding below threshold
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 3               | MONTHS              | FLAT                        | 900          | 60                  | DAYS                    | 50                  |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # freq=60d -> 1 eval at 03-02 (cumulative required = 50% of 900 = 450)
    # 3 payments by 03-02: 200(10 Jan) + 150(25 Jan) + 200(15 Feb) = 550 >= 450 -> not trigger
    # After period end -> nearBreach=false; outstanding=350>0 -> breach=true
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "25 January 2026"
    And Customer makes repayment on "25 January 2026" with 150.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 February 2026"
    And Customer makes repayment on "15 February 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "01 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-31 | 900.00           | 350.00            | false      | true   |
      | 2            | 2026-04-01 | 2026-06-30 | 900.00           | 900.00            | null       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "01 April 2026"

  @TestRailId:C76649
  Scenario: Verify near breach false and breach false when full payment made before first eval date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 3               | MONTHS              | FLAT                        | 900          | 60                  | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # freq=60d -> 1 eval at 03-02 (cumulative required = 33.33% of 900 = 299.97)
    # Pay 900 on 15 Jan (full) -> cumulative paid by 03-02 = 900 >= 299.97 -> not trigger
    # After period end -> nearBreach=false; outstanding=0 -> breach=false (immediate via applyRepayment)
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 900.0 transaction amount on Working Capital loan
    When Admin sets the business date to "01 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-31 | 900.00           | 0.00              | false      | false  |
      | 2            | 2026-04-01 | 2026-06-30 | 900.00           | 900.00            | null       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "01 April 2026"

  @TestRailId:C76650
  Scenario: Verify near breach evaluated correctly across 4 consecutive breach periods with mixed results
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 1               | MONTHS              | FLAT                        | 300          | 15                  | DAYS                    | 50                  |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 300.00           | 300.00            | true       | true   |
      | 2            | 2026-02-01 | 2026-02-28 | 300.00           | 300.00            | null       | null   |
    # --- P2: pay 200 -> cumulative paid by eval#1 (02-16) = 200 >= 150 -> nearBreach=false; outstanding=100>0 -> breach=true ---
    When Admin sets the business date to "05 February 2026"
    And Customer makes repayment on "05 February 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "01 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 300.00           | 300.00            | true       | true   |
      | 2            | 2026-02-01 | 2026-02-28 | 300.00           | 100.00            | false      | true   |
      | 3            | 2026-03-01 | 2026-03-31 | 300.00           | 300.00            | null       | null   |
    # --- P3: no payment by eval#1 (03-16) -> cumulative paid=0 < 150 -> nearBreach=true; breach=true ---
    When Admin sets the business date to "01 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- P4: pay 300 on 04-01 -> cumulative paid by eval#1 (04-16) = 300 >= 150 -> nearBreach=false; outstanding=0 -> breach=false (immediate via applyRepayment) ---
    And Customer makes repayment on "01 April 2026" with 300.0 transaction amount on Working Capital loan
    When Admin sets the business date to "01 May 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 300.00           | 300.00            | true       | true   |
      | 2            | 2026-02-01 | 2026-02-28 | 300.00           | 100.00            | false      | true   |
      | 3            | 2026-03-01 | 2026-03-31 | 300.00           | 300.00            | true       | true   |
      | 4            | 2026-04-01 | 2026-04-30 | 300.00           | 0.00              | false      | false  |
      | 5            | 2026-05-01 | 2026-05-31 | 300.00           | 300.00            | null       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "01 May 2026"

  @TestRailId:C76651
  Scenario: Verify non-disbursed loan has no breach schedule and no near breach evaluation
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 3               | MONTHS              | FLAT                        | 900          | 60                  | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    # Loan is approved but NOT disbursed - no breach schedule should exist
    Then Working Capital loan breach schedule has no data
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    When Admin rejects the working capital loan on "01 January 2026"
    Then Working capital loan rejection was successful

  @TestRailId:C80947
  Scenario: Verify near breach eval#1 OK, eval#2 fails with cumulative stepped threshold
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 33                  |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # P1: 01-01 -> 01-09 (9d), freq=3d -> 2 evals: 01-04, 01-07; step required = 33% of 90 = 29.7, cumulative required = N * 29.7.
    # Pay 30 on 02 Jan: cumulative paid by eval#1 (01-04) = 30 >= 1 * 29.7 -> not trigger.
    # Pay 10 on 05 Jan: cumulative paid by eval#2 (01-07) = 40 < 2 * 29.7 = 59.4 -> trigger Y at eval#2.
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 30.0 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 10.0 transaction amount on Working Capital loan
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 50.00             | true       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "08 January 2026"

  @TestRailId:C80948
  Scenario: Verify near breach not triggered when full minimum is paid front-loaded - cumulative requirement satisfied at every eval
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 50                  |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # P1: 01-01 -> 01-09 (9d), freq=3d -> 2 evals: 01-04, 01-07; step required = 50% of 90 = 45, cumulative required = N * 45.
    # Pay 90 on 02 Jan (full minimum upfront).
    # Eval#1 (01-04): cumulative paid = 90 >= 1 * 45 -> not trigger.
    # Eval#2 (01-07): cumulative paid = 90 vs 2 * 45 = 90; strict less-than (90 < 90 is false) -> not trigger.
    # After period end (01-10): all evals passed without trigger -> close-out sets nearBreach=false; outstanding=0 -> breach=false.
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 90.0 transaction amount on Working Capital loan
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 0.00              | false      | false  |
      | 2            | 2026-01-10 | 2026-01-18 | 90.00            | 90.00             | null       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "02 January 2026"

  @TestRailId:C80949
  Scenario: Verify near breach false when cumulative payments meet stepped requirements across multi-eval period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 33                  |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # P1: 01-01 -> 01-09 (9d), evals at 01-04, 01-07; step required = 33% of 90 = 29.7, cumulative required = N * 29.7.
    # Pay 30 on 02 Jan: cumulative paid by eval#1 (01-04) = 30 >= 29.7 -> not trigger.
    # Pay 30 on 05 Jan: cumulative paid by eval#2 (01-07) = 60 >= 59.4 -> not trigger.
    # After period end (01-10) -> close-out sets nearBreach=false; outstanding=30>0 -> breach=true.
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 30.0 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 30.0 transaction amount on Working Capital loan
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 30.00             | false      | true   |
      | 2            | 2026-01-10 | 2026-01-18 | 90.00            | 90.00             | null       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C80950
  Scenario: Verify that near breach is detected by cumulative paid falling below stepped requirement across two consecutive breach periods - UC1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | PERCENTAGE                  | 50           | 3                   | DAYS                    | 33                  |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # P1: 01-01 -> 01-09 (9d), freq=3d -> 2 evals: 01-04, 01-07; minPayment = 50% of 800 = 400; step required = 33% of 400 = 132, cumulative required = N * 132.
    # Pay 200 on 02 Jan: cumulative paid by eval#1 (01-04) = 200 >= 132 -> not trigger.
    # Pay 50 on 05 Jan: cumulative paid by eval#2 (01-07) = 250 < 264 -> trigger Y at eval#2.
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 50.0 transaction amount on Working Capital loan
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 400.00           | 150.00            | true       | null   |
    # Advance past P1 end (01-09) and P2 eval#1 (01-13).
    # P1 breach: paid=250 < min=400 -> breach=true. P1 nearBreach remains true (immutable).
    # P2: 01-10 -> 01-18, 2 evals: 01-13, 01-16. Step required = 132.
    # No payment in P2 -> cumulative at eval#1 (01-13) = 0 < 132 -> trigger Y.
    When Admin sets the business date to "14 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 400.00           | 150.00            | true       | true   |
      | 2            | 2026-01-10 | 2026-01-18 | 400.00           | 400.00            | true       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "14 January 2026"

  @TestRailId:C80951
  Scenario: Verify that near breach evaluation is idempotent across multiple COB runs on the same business date - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # P1: 01-01 -> 01-09, freq=3d -> 2 evals: 01-04, 01-07. Step required = 33.33% of 90 = 29.997.
    # No payment -> cum at eval#1 (01-04) = 0 < 29.997 -> trigger Y at eval#1.
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 90.00             | true       | null   |
    # Re-run COB on the same business date. State must remain unchanged (immutability gate via nearBreach != null).
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 90.00             | true       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "05 January 2026"

  @TestRailId:C80952
  Scenario: Verify that near breach stays immutable when backdated repayment with transaction date inside window is posted later - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # P1: 01-01 -> 01-09, eval#1 at 01-04 (cumulative required = 29.997). No payment -> trigger Y at eval#1.
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 90.00             | true       | null   |
    # Now post a backdated repayment of 50 dated 02 Jan (before eval#1).
    # If re-evaluated, paid 50 >= 29.997 would NOT trigger. But nearBreach is immutable -> stays true.
    # paidAmount/outstanding update synchronously via applyRepayment.
    When Admin sets the business date to "06 January 2026"
    And Customer makes repayment on "02 January 2026" with 50.0 transaction amount on Working Capital loan
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 40.00             | true       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "06 January 2026"

  @TestRailId:C80953
  ### Breach start date is no longer shifted for the first period, but near-breach evaluation should still consider grace days
  Scenario: Verify that grace days shift breach period start and near breach is evaluated at shifted eval dates - UC4
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays | breachGraceDays |
      | 9               | DAYS                | PERCENTAGE                  | 50           | 3                   | DAYS                    | 33                  | 3                    | 3               |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Grace=3 -> P1: 01-01 -> 01-12 (9d from 01-01 minus 1 day + 3 grace days). minPayment = 50% of 800 = 400.
    # near-breach freq=3 -> without grace the first eval is 01-03; grace=3 shifts evals to 01-06 (#1), 01-09 (#2).
    # step required = 33% of 400 = 132. Pay 100 on 05 Jan -> cumulative paid by eval#1 is 100.
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 100.0 transaction amount on Working Capital loan
    # Phase A: current date 05 Jan (ON eval#1 at 01-06) -> nearBreach=null.
    When Admin sets the business date to "06 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-12 | 400.00           | 300.00            | null       | null   |
    # Phase B: advance past eval#1 (01-06) -> cumulative paid by 01-06 = 100 < 132 -> trigger Y at eval#1.
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-12 | 400.00           | 300.00            | true       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "07 January 2026"

  @TestRailId:C80954
  Scenario: Verify that near breach stays null between evals and is detected when cumulative paid falls short of stepped requirement at a later eval - UC5
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | PERCENTAGE                  | 10           | 3                   | DAYS                    | 33                  |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 10000           | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # P1: 01-01 -> 01-09 (9d). minPayment = 10% of 10000 = 1000.
    # near-breach freq=3 -> eval points: 01-04 (#1), 01-07 (#2); 01-10 lands on/after toDate (01-09) and is excluded.
    # Step required = 33% of 1000 = 330. Cumulative required at eval#N = N * 330 (330, 660).
    # Pay 400 on 02 Jan -> cumulative at 01-04 = 400 >= 330 -> not trigger.
    # No further payment -> cumulative at 01-07 = 400 < 660 -> trigger Y at eval#2.
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 400.0 transaction amount on Working Capital loan
    # Phase A: COB on 05 Jan (AFTER eval#1, BEFORE eval#2) -> #1 passed without trigger, #2 not yet evaluated, period not ended -> nearBreach stays null.
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 1000.00          | 600.00            | null       | null   |
    # Phase B: COB on 08 Jan (AFTER eval#2) -> eval#2 triggers (400 < 660) -> nearBreach=true.
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 1000.00          | 600.00            | true       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "08 January 2026"

  @TestRailId:C80955
  Scenario: Verify credit balance refund does not mutate already evaluated near breach schedule - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # No payment by eval#1 (01-04) -> nearBreach=true.
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 90.00             | true       | null   |
    # Overpay the loan and post CBR. CBR must not clear or recalculate the already evaluated nearBreach flag.
    When Admin sets the business date to "06 January 2026"
    And Customer makes repayment on "06 January 2026" with 9500.0 transaction amount on Working Capital loan
    And Customer makes credit balance refund on "06 January 2026" with 500.0 transaction amount on Working Capital loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 0.00              | true       | false  |
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C77001
  Scenario: Verify near breach evaluation window shifts when breachGraceDays is set on the product
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | breachGraceDays |
      | 3               | MONTHS              | FLAT                        | 900          | 60                  | DAYS                    | 33.33               | 5               |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # breachGraceDays=5 -> Period 1: 01-01 -> 04-05; freq=60d -> 1 eval at 03-07 (cumulative required = 33.33% of 900 = 299.97)
    # No payment by 03-07 -> cumulative paid=0 < 299.97 -> trigger Y
    When Admin sets the business date to "08 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-04-05 | 900.00           | 900.00            | true       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "08 January 2026"

  @TestRailId:C85315
  Scenario: Verify near breach RESCHEDULE action - UC1: threshold raised so period that would have triggered no longer triggers
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Default: freq=3 DAYS -> eval#1 at 04-Jan. Threshold=33.33% of 90=29.997. No payment -> would trigger.
    # Pre-change check: schedule exists with nearBreach=null (eval not yet reached), no action history yet.
    Then Near breach action history has 0 entries
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 90.00             | null       | null   |
    # Post RESCHEDULE: raise threshold to 99% (=89.1) and shift frequency to 14 DAYS -> new eval#1 at 15-Jan.
    # COB on 05-Jan: past old eval date (04-Jan), before new eval date (15-Jan) -> nearBreach stays null.
    When Admin sets the business date to "02 January 2026"
    And Admin creates a near breach reschedule action with threshold "99" frequency 14 frequencyType "DAYS"
    Then Near breach action history has the following data:
      | action     | threshold | frequency | frequencyType |
      | RESCHEDULE | 99        | 14        | DAYS          |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 90.00             | null       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "05 January 2026"

  @TestRailId:C85316
  Scenario: Verify near breach RESCHEDULE action - UC2: threshold lowered so period that would not have triggered now triggers
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Pay 28 on 02-Jan: below 33.33% bar (29.997). Default: would NOT trigger at eval#1 (04-Jan).
    # Pre-change check: no action history yet, schedule shows nearBreach=null.
    Then Near breach action history has 0 entries
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 90.00             | null       | null   |
    # Post RESCHEDULE: raise threshold to 50% (=45). Pay 28 < 45 -> triggers nearBreach=true with new threshold.
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 28.0 transaction amount on Working Capital loan
    And Admin creates a near breach reschedule action with threshold "50" frequency 3 frequencyType "DAYS"
    Then Near breach action history has the following data:
      | action     | threshold | frequency | frequencyType |
      | RESCHEDULE | 50        | 3         | DAYS          |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 62.00             | true       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "05 January 2026"

  @TestRailId:C85317
  Scenario: Verify near breach RESCHEDULE action - UC3: frequency change shifts evaluation date forward
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Default eval#1 at 04-Jan. Post RESCHEDULE: freq=6 DAYS -> new eval#1 at 07-Jan.
    # Pre-change check: no action history yet, schedule shows nearBreach=null.
    Then Near breach action history has 0 entries
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 90.00             | null       | null   |
    # COB on 05-Jan: past old eval date (04-Jan), before new eval date (07-Jan) -> still null.
    When Admin sets the business date to "02 January 2026"
    And Admin creates a near breach reschedule action with threshold "33.33" frequency 6 frequencyType "DAYS"
    Then Near breach action history has the following data:
      | action     | threshold | frequency | frequencyType |
      | RESCHEDULE | 33.33     | 6         | DAYS          |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 90.00             | null       | null   |
    # COB on 08-Jan: past new eval#1 (07-Jan), no payment -> cumPaid=0 < 29.997 -> triggers.
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 90.00             | true       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "08 January 2026"

  @TestRailId:C85318
  Scenario: Verify near breach RESCHEDULE action - UC4: multiple actions posted; only the latest governs evaluation
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Action 1: threshold=50%, freq=3 DAYS -> would trigger at eval#1 (04-Jan) since cumPaid=0 < 45.
    # Action 2: threshold=99%, freq=14 DAYS -> latest wins; eval#1 at 15-Jan; cumPaid=0 < 89.1 but no eval before 15-Jan.
    # Pre-change check: no action history yet, schedule shows nearBreach=null.
    Then Near breach action history has 0 entries
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 90.00             | null       | null   |
    When Admin sets the business date to "02 January 2026"
    And Admin creates a near breach reschedule action with threshold "50" frequency 3 frequencyType "DAYS"
    And Admin creates a near breach reschedule action with threshold "99" frequency 14 frequencyType "DAYS"
    Then Near breach action history has the following data:
      | action     | threshold | frequency | frequencyType |
      | RESCHEDULE | 99        | 14        | DAYS          |
      | RESCHEDULE | 50        | 3         | DAYS          |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 90.00             | null       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "05 January 2026"

  @TestRailId:C85319
  Scenario: Verify near breach RESCHEDULE action - UC5: posting action after nearBreach already true does not clear it
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # No payment -> COB 05-Jan: eval#1 (04-Jan) triggers -> nearBreach=true.
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 90.00             | true       | null   |
    # Post RESCHEDULE with threshold that would NOT have triggered. Immutability must protect already-set nearBreach=true.
    When Admin creates a near breach reschedule action with threshold "99" frequency 14 frequencyType "DAYS"
    Then Near breach action history has the following data:
      | action     | threshold | frequency | frequencyType |
      | RESCHEDULE | 99        | 14        | DAYS          |
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 90.00             | true       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "05 January 2026"

  @TestRailId:C85320
  Scenario: Verify near breach RESCHEDULE action - UC6: action posted after period end does not affect closed period; applies to next
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Pay 90 on 02-Jan: period 1 fully settled -> nearBreach=false, breach=false.
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 90.0 transaction amount on Working Capital loan
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 0.00              | false      | false  |
      | 2            | 2026-01-10 | 2026-01-18 | 90.00            | 90.00             | null       | null   |
    # Post RESCHEDULE: threshold=50%, freq=3 DAYS -> applies from now on for period 2.
    # Period 1 already closed (nearBreach=false, breach=false) -> must not change.
    # Period 2 eval#1 at 13-Jan: no new payment -> cumPaid=0 < 45 -> triggers nearBreach=true.
    When Admin creates a near breach reschedule action with threshold "50" frequency 3 frequencyType "DAYS"
    Then Near breach action history has the following data:
      | action     | threshold | frequency | frequencyType |
      | RESCHEDULE | 50        | 3         | DAYS          |
    When Admin sets the business date to "14 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 0.00              | false      | false  |
      | 2            | 2026-01-10 | 2026-01-18 | 90.00            | 90.00             | true       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "14 January 2026"

  @TestRailId:C85321
  Scenario: Verify near breach RESCHEDULE action - UC7: repayment after action keeps nearBreach=false at new eval date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Post RESCHEDULE: threshold=50% (=45), freq=6 DAYS -> eval#1 at 07-Jan.
    # Pre-change check: no action history yet, schedule shows nearBreach=null.
    Then Near breach action history has 0 entries
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 90.00             | null       | null   |
    # Post RESCHEDULE on 02-Jan: threshold=50% (=45), freq=6 DAYS -> eval#1 at 07-Jan (01-Jan + 6 days).
    # Pay 50 on 04-Jan: outstanding=40 which is <= threshold amount (45) -> nearBreach not triggered (stays null).
    When Admin sets the business date to "02 January 2026"
    And Admin creates a near breach reschedule action with threshold "50" frequency 6 frequencyType "DAYS"
    Then Near breach action history has the following data:
      | action     | threshold | frequency | frequencyType |
      | RESCHEDULE | 50        | 6         | DAYS          |
    When Admin sets the business date to "04 January 2026"
    And Customer makes repayment on "04 January 2026" with 50.0 transaction amount on Working Capital loan
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 40.00             | null       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "08 January 2026"

  @TestRailId:C85322
  Scenario: Verify near breach RESCHEDULE action - UC8: backdated repayment does not clear immutable nearBreach set under new config
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Post RESCHEDULE: threshold=50% (=45), freq=3 DAYS -> eval#1 at 04-Jan.
    # Pre-change check: no action history yet, schedule shows nearBreach=null.
    Then Near breach action history has 0 entries
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 90.00             | null       | null   |
    # No payment -> COB 05-Jan: cumPaid=0 < 45 -> nearBreach=true.
    When Admin sets the business date to "02 January 2026"
    And Admin creates a near breach reschedule action with threshold "50" frequency 3 frequencyType "DAYS"
    Then Near breach action history has the following data:
      | action     | threshold | frequency | frequencyType |
      | RESCHEDULE | 50        | 3         | DAYS          |
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 90.00             | true       | null   |
    # Backdate repayment of 50 to 02-Jan. Immutability must keep nearBreach=true.
    When Admin sets the business date to "06 January 2026"
    And Customer makes repayment on "02 January 2026" with 50.0 transaction amount on Working Capital loan
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 40.00             | true       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "06 January 2026"

  @TestRailId:C85323
  Scenario: Verify near breach RESCHEDULE action - UC9: goodwill credit after action keeps nearBreach=false at eval date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Post RESCHEDULE: threshold=50% (=45), freq=6 DAYS -> eval#1 at 07-Jan.
    # Pre-change check: no action history yet, schedule shows nearBreach=null.
    Then Near breach action history has 0 entries
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 90.00             | null       | null   |
    # Goodwill credit of 50 on 04-Jan: outstanding=40 which is <= threshold amount (45) -> nearBreach not triggered (stays null).
    When Admin sets the business date to "02 January 2026"
    And Admin creates a near breach reschedule action with threshold "50" frequency 6 frequencyType "DAYS"
    Then Near breach action history has the following data:
      | action     | threshold | frequency | frequencyType |
      | RESCHEDULE | 50        | 6         | DAYS          |
    When Admin sets the business date to "04 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "04 January 2026" with 50.0 transaction amount on Working Capital loan
    When Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 90.00            | 40.00             | null       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "08 January 2026"

  @TestRailId:C85324
  Scenario: Verify near breach RESCHEDULE action - UC10: action on loan with no near breach config fails (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # No near breach config on product -> RESCHEDULE action must fail.
    When Admin sets the business date to "02 January 2026"
    When Admin creates a near breach reschedule action with threshold "33.33" frequency 3 frequencyType "DAYS" expecting error:
      | httpCode | errorMessage                                                                                         |
      | 400      | Failed data validation due to: near.breach.action.not.allowed.loan.has.no.near.breach.configuration. |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "02 January 2026"

  @TestRailId:C85325
  Scenario: Verify near breach RESCHEDULE action - UC11: action on non-active loan (submitted, not approved) fails (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    # Loan is in SUBMITTED state (not approved) -> action must fail.
    Then Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status                         | approvedPrincipal | proposedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 0.0               | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin creates a near breach reschedule action with threshold "50" frequency 3 frequencyType "DAYS" expecting error:
      | httpCode | errorMessage                                                                       |
      | 400      | Failed data validation due to: near.breach.action.not.allowed.for.non.active.loan. |
    When Admin rejects the working capital loan on "01 January 2026"
    Then Working capital loan rejection was successful

  @TestRailId:C85326
  Scenario: Verify near breach RESCHEDULE action - UC12: action on approved but not yet disbursed loan fails (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    # Loan is APPROVED but not yet disbursed (not ACTIVE) -> action must fail.
    Then Working capital loan account has the correct data:
      | submittedOnDate | expectedDisbursementDate | status   | approvedPrincipal | proposedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin creates a near breach reschedule action with threshold "50" frequency 3 frequencyType "DAYS" expecting error:
      | httpCode | errorMessage                                                                       |
      | 400      | Failed data validation due to: near.breach.action.not.allowed.for.non.active.loan. |
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    When Admin rejects the working capital loan on "01 January 2026"
    Then Working capital loan rejection was successful

  @TestRailId:C85327
  Scenario: Verify near breach RESCHEDULE action - UC13: threshold exceeding 100 percent fails validation (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin creates a near breach reschedule action with threshold "101" frequency 3 frequencyType "DAYS" expecting error:
      | httpCode | errorMessage                                                |
      | 400      | Failed data validation due to: must.not.exceed.100.percent. |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "01 January 2026"

  @TestRailId:C98174
  Scenario: Verify that breach Id is overridable and applied while nearBreach Id not changing - UC1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold |
      | 1               | MONTHS              | FLAT                        | 500          | 5               | 20                  | DAYS                    | 33.33               |
    And Admin creates a new Working Capital Near Breach Configuration:
      | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold |
      | 2                   | WEEKS                   | 17.25               |
    And Admin creates a working capital loan using created product with breachGraceDays 11 and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount | nearBreachId |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        | LAST_CREATED |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-11 | 42           | 500.00           | 500.00            | null       | null   |
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-11 | 42           | 500.00           | 500.00            | null       | null   |
    When Admin sets the business date to "30 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-11 | 42           | 500.00           | 500.00            | true       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "30 January 2026"

  @TestRailId:C98175
  Scenario: Verify that nearBreach Id is overridable and applied while breach Id isn't changed - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold |
      | 1               | MONTHS              | FLAT                        | 900          | 5               | 20                  | DAYS                    | 33.33               |
    And Admin creates a new Working Capital Breach Configuration:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount |
      | 2               | MONTHS              | FLAT                        | 250          |
    And Admin creates a working capital loan using created product with breachGraceDays 11 and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount | breachId     |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        | LAST_CREATED |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-11 | 70           | 250.00           | 250.00            | null       | null   |
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-11 | 70           | 250.00           | 250.00            | null       | null   |
    When Admin sets the business date to "15 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-11 | 70           | 250.00           | 250.00            | true       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "15 February 2026"

  @TestRailId:C98176
  Scenario: Verify that breach Id and nearBreach Id are overridable - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold |
      | 3               | MONTHS              | FLAT                        | 900          | 5               | 3                   | DAYS                    | 33.33               |
    And Admin creates a new Working Capital Breach Configuration:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount |
      | 1               | MONTHS              | FLAT                        | 250          |
    And Admin creates a new Working Capital Near Breach Configuration:
      | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold |
      | 1                   | WEEKS                   | 17.25               |
    And Admin creates a working capital loan using created product with breachGraceDays 11 and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount | breachId     | nearBreachId |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        | LAST_CREATED | LAST_CREATED |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-11 | 42           | 250.00           | 250.00            | null       | null   |
    When Admin sets the business date to "06 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-11 | 42           | 250.00           | 250.00            | true       | null   |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "06 February 2026"

