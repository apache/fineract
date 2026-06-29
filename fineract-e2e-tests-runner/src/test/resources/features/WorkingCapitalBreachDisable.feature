@WorkingCapital
@WorkingCapitalBreachDisableFeature
Feature: Working Capital Breach Disable

  Scenario: Disable stops breach evaluation and enable re-triggers a recompute as of the enable date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "28 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach disable with startDate "28 February 2026"
    Then Working Capital loan breach disable action has the following data:
      | action  | startDate  | endDate |
      | DISABLE | 2026-02-28 |         |
    When Admin sets the business date to "01 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | null   |
      | 2            | 2026-03-01 | 2026-04-30 | 61           | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "15 March 2026"
    And Admin initiate a Working Capital loan breach enable with startDate "15 March 2026"
    Then Working Capital loan breach disable action has the following data:
      | action  | startDate  | endDate |
      | DISABLE | 2026-02-28 |         |
      | ENABLE  | 2026-03-15 |         |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-03-01 | 2026-04-30 | 61           | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "15 March 2026"

  Scenario: Breach disable is allowed only once until it is reversed
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach disable with startDate "15 January 2026"
    Then Initiating a Working Capital loan breach disable with startDate "15 January 2026" results an error with the following data:
      | httpCode | message                                                |
      | 400      | Failed data validation due to: breach.already.disabled |
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  Scenario: Breach enable is rejected when there is no active disable
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a Working Capital loan breach enable with startDate "15 January 2026" results an error with the following data:
      | httpCode | message                                                           |
      | 400      | Failed data validation due to: no.active.breach.disable.to.enable |
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  Scenario: Breach disable and enable are rejected when backdated or given an end date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a Working Capital loan breach disable with startDate "10 January 2026" results an error with the following data:
      | httpCode | message                                                      |
      | 400      | Failed data validation due to: must.be.current.business.date |
    And Initiating a Working Capital loan breach enable with startDate "10 January 2026" results an error with the following data:
      | httpCode | message                                                      |
      | 400      | Failed data validation due to: must.be.current.business.date |
    And Initiating a Working Capital loan breach disable with startDate "15 January 2026" and endDate "20 January 2026" results an error with the following data:
      | httpCode | message                                                                   |
      | 400      | Failed data validation due to: must.not.be.provided.for.disable.or.enable |
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  Scenario: While disabled the pause and reschedule breach actions are not allowed
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach disable with startDate "15 January 2026"
    Then Initiating a Working Capital loan breach pause with startDate "16 January 2026" and endDate "20 January 2026" results an error with the following data:
      | httpCode | message                                           |
      | 400      | Failed data validation due to: breach.is.disabled |
    And Initiating a Working Capital loan breach reschedule with minimumPayment "50" "FLAT" results an error with the following data:
      | httpCode | message                                           |
      | 400      | Failed data validation due to: breach.is.disabled |
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  Scenario: While disabled the near breach reschedule action is not allowed
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
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach disable with startDate "15 January 2026"
    Then Admin creates a near breach reschedule action with threshold "50" frequency 3 frequencyType "DAYS" expecting error:
      | httpCode | errorMessage                                      |
      | 400      | Failed data validation due to: breach.is.disabled |
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  Scenario: While disabled the reset breach action is not allowed
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach disable with startDate "15 January 2026"
    Then Admin fails to create WC breach reset action with error containing "breach.is.disabled"
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  Scenario: Resume of a pause made before disable is not allowed while breach is disabled
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach pause with startDate "15 January 2026" and endDate "20 January 2026"
    And Admin initiate a Working Capital loan breach disable with startDate "15 January 2026"
    Then Initiating a Working Capital loan breach resume with startDate "15 January 2026" results an error with the following data:
      | httpCode | message                                           |
      | 400      | Failed data validation due to: breach.is.disabled |
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  Scenario: Undo reset is allowed while breach is disabled
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates WC breach reset action
    And Admin initiate a Working Capital loan breach disable with startDate "15 January 2026"
    When Admin creates WC breach undo reset action
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  Scenario: Repayment during disable does not touch the breach schedule and is applied by enable
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach disable with startDate "15 January 2026"
    When Admin sets the business date to "20 January 2026"
    And Customer makes repayment on "20 January 2026" with 110.7 transaction amount on Working Capital loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "15 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach enable with startDate "15 March 2026"
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 110.70           | 0                 | null       | false  |
      | 2            | 2026-03-01 | 2026-04-30 | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "15 March 2026"

  Scenario: Repayment undo during disable does not touch the breach schedule and the reversal is honoured by enable
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach disable with startDate "15 January 2026"
    When Admin sets the business date to "20 January 2026"
    And Customer makes repayment on "20 January 2026" with 110.7 transaction amount on Working Capital loan
    When Admin sets the business date to "15 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer undo "1"th "REPAYMENT" transaction made on "20 January 2026" on Working Capital loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 110.70           | 110.70            | null       | null   |
      | 2            | 2026-03-01 | 2026-04-30 | 110.70           | 110.70            | null       | null   |
    When Admin initiate a Working Capital loan breach enable with startDate "15 March 2026"
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 110.70           | 110.70            | null       | true   |
      | 2            | 2026-03-01 | 2026-04-30 | 110.70           | 110.70            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "15 March 2026"

  Scenario: Disable suppresses near breach evaluation
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
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach disable with startDate "01 February 2026"
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-31 | 900.00           | 900.00            | null       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "03 March 2026"

  Scenario: Enable resumes near breach evaluation on the next COB
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
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach disable with startDate "01 February 2026"
    When Admin sets the business date to "02 March 2026"
    And Admin initiate a Working Capital loan breach enable with startDate "02 March 2026"
    When Admin sets the business date to "03 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-03-31 | 900.00           | 900.00            | true       | null   |
    Then Admin closes the Working Capital loan with a full repayment on "03 March 2026"

  Scenario: A loan can be disabled again after a previous disable was enabled
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach disable with startDate "10 January 2026"
    When Admin sets the business date to "20 January 2026"
    And Admin initiate a Working Capital loan breach enable with startDate "20 January 2026"
    And Admin initiate a Working Capital loan breach disable with startDate "20 January 2026"
    Then Working Capital loan breach disable action has the following data:
      | action  | startDate  | endDate |
      | DISABLE | 2026-01-10 |         |
      | ENABLE  | 2026-01-20 |         |
      | DISABLE | 2026-01-20 |         |
    Then Admin closes the Working Capital loan with a full repayment on "20 January 2026"

  Scenario: Breach disable is rejected for a loan without breach configuration
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a Working Capital loan breach disable with startDate "01 January 2026" results an error with the following data:
      | httpCode | message                                                |
      | 400      | Failed data validation due to: no.breach.configuration |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  Scenario: Breach disable is rejected for a not yet active loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Initiating a Working Capital loan breach disable with startDate "01 January 2026" results an error with the following data:
      | httpCode | message                                           |
      | 400      | Failed data validation due to: loan.is.not.active |

  Scenario: Breach disable can be applied and retrieved by loan external id
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan breach disable by external ID with startDate "15 January 2026"
    Then Working Capital loan breach disable action by external ID has the following data:
      | action  | startDate  | endDate |
      | DISABLE | 2026-01-15 |         |
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  Scenario: Breach disable requires the CREATE_WC_BREACH_DISABLE permission
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin creates new user with "NO_CREATE_WC_BREACH_DISABLE_USER" username, "NO_CREATE_WC_BREACH_DISABLE_ROLE" role name and given permissions:
      | REPAYMENT_LOAN |
    Then Created user with no CREATE_WC_BREACH_DISABLE permission gets an error when initiate a Working Capital loan breach disable with startDate "15 January 2026"
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"
