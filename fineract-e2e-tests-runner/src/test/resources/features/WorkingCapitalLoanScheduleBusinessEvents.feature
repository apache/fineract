@WorkingCapital
@WorkingCapitalLoanScheduleBusinessEventsFeature @WCCOBFeature
Feature: Working Capital Loan Schedule Business Events

  @TestRailId:C94069
  Scenario: Working Capital loan raises Period Payment Rate Changed business event when the rate is updated
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "12.5" value
    Then a Working Capital Loan Period Payment Rate Changed business event is raised
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  @TestRailId:C94070
  Scenario: Working Capital loan raises Delinquency Pause, Resume, Disable and Enable business events on the matching delinquency actions
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "15 January 2026" and endDate "25 January 2026"
    Then a Working Capital Loan Delinquency Pause business event is raised
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency resume with startDate "20 January 2026"
    Then a Working Capital Loan Delinquency Resume business event is raised
    When Admin sets the business date to "26 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency disable with startDate "26 January 2026"
    Then a Working Capital Loan Delinquency Disable business event is raised
    When Admin sets the business date to "28 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency enable with startDate "28 January 2026"
    Then a Working Capital Loan Delinquency Enable business event is raised
    Then Admin closes the Working Capital loan with a full repayment on "28 January 2026"

  @TestRailId:C94071
  Scenario: Working Capital loan raises Breach Pause, Resume, Disable and Enable business events on the matching breach actions
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause with startDate "15 January 2026" and endDate "25 January 2026"
    Then a Working Capital Loan Breach Pause business event is raised
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach resume with startDate "20 January 2026"
    Then a Working Capital Loan Breach Resume business event is raised
    When Admin sets the business date to "26 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach disable with startDate "26 January 2026"
    Then a Working Capital Loan Breach Disable business event is raised
    When Admin sets the business date to "28 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach enable with startDate "28 January 2026"
    Then a Working Capital Loan Breach Enable business event is raised
    Then Admin closes the Working Capital loan with a full repayment on "28 January 2026"

  @TestRailId:C94072
  Scenario: Working Capital loan raises Delinquency Schedule Changed and Breach Schedule Changed business events from the COB run that opens the schedules
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 3 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket and custom breach config:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount |
      | 6               | DAYS                | PERCENTAGE                  | 50           |
    And Admin creates a working capital loan with the following data:
      | LoanProduct      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DELINQUENCY | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then a Working Capital Loan Delinquency Schedule Changed business event is raised
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And a Working Capital Loan Breach Schedule Changed business event is raised
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C98184
  Scenario: Working Capital loan raises Breach Past Due Change business event from the COB run that closes an unpaid period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 7               | DAYS                | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "0"
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "500"
    And a Working Capital Loan Breach Schedule Changed business event is raised
    And a Working Capital Loan Breach Change business event is raised with breach flag "true"
    And a Working Capital Loan Breach Past Due Change business event is raised with "500" past due amount
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "500"
    And no Working Capital Loan Breach Past Due Change business event is raised
    And no Working Capital Loan Breach Change business event is raised
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C98185
  Scenario: Working Capital loan raises Breach Past Due Change business event when a backdated repayment lowers the past due amount
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 7               | DAYS                | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "09 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "500"
    And a Working Capital Loan Breach Schedule Changed business event is raised
    And a Working Capital Loan Breach Change business event is raised with breach flag "true"
    And a Working Capital Loan Breach Past Due Change business event is raised with "500" past due amount
    When Customer makes repayment on "05 January 2026" with 500.0 transaction amount on Working Capital loan
    Then Working Capital loan balance has breach past due amount "0"
    And a Working Capital Loan Breach Past Due Change business event is raised with "0" past due amount
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan balance has breach past due amount "0"
    And no Working Capital Loan Breach Past Due Change business event is raised
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C94073
  Scenario: Working Capital loan raises Breach Schedule Changed business event when a near breach reschedule action is created

  Scenario: Working Capital loan raises Breach Reschedule business event when a near breach reschedule action is created
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 3               | MONTHS              | FLAT                        | 900          | 60                  | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates a near breach reschedule action with threshold "50" frequency 6 frequencyType "DAYS"
    Then a Working Capital Loan Breach Reschedule business event is raised
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C98186
  Scenario: Working Capital loan raises Near Breach Change business event from the COB run that reaches a near breach evaluation date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 3               | MONTHS              | FLAT                        | 900          | 60                  | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then a Working Capital Loan Near Breach Change business event is raised with near breach flag "true"
    When Admin sets the business date to "04 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then no Working Capital Loan Near Breach Change business event is raised
    Then Admin closes the Working Capital loan with a full repayment on "04 March 2026"

  @TestRailId:C98187
  Scenario: Working Capital loan raises Breach Reschedule business event when a breach reschedule action is created
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "01 June 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin creates WC breach reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType |
      | 1              | PERCENTAGE         |
    Then a Working Capital Loan Breach Reschedule business event is raised
    Then Admin closes the Working Capital loan with a full repayment on "01 June 2026"

  @TestRailId:C98188
  Scenario: Working Capital loan raises Breach Reset and Breach Undo Reset business events on the matching breach actions
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 60              | DAYS                | PERCENTAGE                  | 50           |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 800             | 10000              | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "800" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "800" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 April 2026"
    And Admin creates WC breach reset action
    Then a Working Capital Loan Breach Reset business event is raised
    When Admin creates WC breach undo reset action
    Then a Working Capital Loan Breach Undo Reset business event is raised
    Then Admin closes the Working Capital loan with a full repayment on "15 April 2026"

  @TestRailId:C98189
  Scenario: Working Capital loan raises Delinquency Reschedule business event when a delinquency reschedule action is created
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "01 June 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin creates WC delinquency reschedule action with the following parameters:
      | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | 1              | PERCENTAGE         | 30        | DAYS          |
    Then a Working Capital Loan Delinquency Reschedule business event is raised
    Then Admin closes the Working Capital loan with a full repayment on "01 June 2026"

  @TestRailId:C98190
  Scenario: Working Capital loan raises Delinquency Reset and Delinquency Undo Reset business events on the matching delinquency actions
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates WC Delinquency Bucket with frequency 30 DAYS and minimumPayment 3 PERCENTAGE
    And Admin creates a new Working Capital Loan Product with delinquency bucket
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    And Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "01 June 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin creates a Working Capital delinquency reset
    Then a Working Capital Loan Delinquency Reset business event is raised
    When Admin creates Working Capital delinquency reset undo
    Then a Working Capital Loan Delinquency Undo Reset business event is raised
    Then Admin closes the Working Capital loan with a full repayment on "01 June 2026"
