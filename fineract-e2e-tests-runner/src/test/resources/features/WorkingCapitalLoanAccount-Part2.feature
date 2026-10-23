@WorkingCapital
@WorkingCapitalLoanAccountFeature
Feature: WorkingCapitalLoanAccount - Part2

  @TestRailId:C72392
  Scenario: Verify WC Loan creation with invalid delinquencyStartType results in error
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with delinquencyGraceDays 3 and delinquencyStartType "LOAN_CREATION" for loan test
    Then Creating a working capital loan with invalid delinquencyStartType "INVALID" will result with status code 400

  @TestRailId:C72368
  Scenario: Create Working Capital Loan account - UC13: Attempt to modify loan in DISBURSED state (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Verify Working Capital loan disbursement was successful
    Then Modifying the working capital loan that is Disbursed in Active state results in an error

  @TestRailId:C72371
  Scenario: Disburse WCL loan account that is not approved failure - UC1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    Then Admin fails to disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount because of not approved

  @TestRailId:C72372
  Scenario: Disburse WCL loan account successful use case - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0             | 100.0             | 100.0              | 1.0               | null     |

  @TestRailId:C74514
  Scenario: Disburse WCL loan account with classification use case - UC2.1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and valid classification
    Then Verify Working Capital loan disbursement transaction has classification
    Then Working Capital loan status will be "ACTIVE"

  @TestRailId:C74515
  Scenario: Verify that multi-disbursement is not allowed on WCL loan account - UC2.2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Admin fails to disburse the Working Capital loan on "01 January 2026" with "50" EUR transaction amount because of loan status "ACTIVE" with status code 400

  @TestRailId:C72373
  Scenario Outline: Disburse WCL loan account with invalid data outcomes with an error - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 10 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-10               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    When Admin sets the business date to "05 January 2026"
    Then Admin successfully approves the working capital loan on "05 January 2026" with "100" amount and expected disbursement date on "10 January 2026"
    Then Admin fails to disburse the Working Capital loan on "<wcp_disburse_date>" with "<wcp_disburse_amount>" EUR transaction amount with invalid data outcomes with error message <wcp_error_message>

    Examples:
      | wcp_disburse_date | wcp_disburse_amount | wcp_error_message                          |
      | 05 January 2027   | 100                 | "cannot.be.a.future.date."                 |
      | 05 January 2025   | 100                 | "cannot.be.before.submitted.date."         |
      | 02 January 2026   | 100                 | "cannot.be.before.approval.date."          |
      | 10 January 2026   | 1000                | "amount.cannot.exceed.approved.principal." |

  @TestRailId:C72374
  Scenario Outline: Disburse WCL loan account without mandatory data outcomes with an error - UC4
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 10 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-10               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null     |
    When Admin sets the business date to "05 January 2026"
    Then Admin successfully approves the working capital loan on "05 January 2026" with "100" amount and expected disbursement date on "10 January 2026"
    Then Admin fails to disburse the Working Capital loan on <wcp_disburse_date> with "<wcp_disburse_amount>" EUR transaction amount without mandatory data outcomes with error message <wcp_error_message>

    Examples:
      | wcp_disburse_date | wcp_disburse_amount | wcp_error_message                                           |
      | ""                | 100                 | "The parameter `actualDisbursementDate` is mandatory."      |
      | "05 January 2025" | 0                   | "The parameter `transactionAmount` must be greater than 0." |

  @TestRailId:C72375
  Scenario: Undo Disbursal of WCL account successful use case - UC5
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Admin successfully undo Working Capital disbursal
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed | discountApproved |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | 0.0              | null             |

  @TestRailId:C74516
  Scenario: Verify that undo disbursal of WCL account reverts all balance fields - UC5.1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Admin successfully undo Working Capital disbursal
    Then Working Capital loan status will be "APPROVED"
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | realizedIncome | unrealizedIncome |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null     | 0.0                | 0.0            | 0.0              |

  @TestRailId:C72376
  Scenario: Undo disbursal of WCL account that is submitted or approved is failed - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    Then Admin fails to undo disbursal the Working Capital loan due to loan status "SUBMITTED_AND_PENDING_APPROVAL"
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin fails to undo disbursal the Working Capital loan due to loan status "APPROVED"

  @TestRailId:C72377
  Scenario: Disburse WCL account and undo disbursal via externalId successful use case - UC7
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan by externalId on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital disbursal transaction business event is raised with "100" amount and reversed "false"
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0             | 100.0             | 100.0              | 1.0               | null     |
    Then Admin successfully undo Working Capital disbursal by externalId
    Then Working Capital undo disbursal transaction business event is raised with "100" amount and reversed "true"
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null     |

  @TestRailId:C74517
  Scenario: Verify that WCL disbursement with classification triggers correct events on disburse and undo - UC7.1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount and valid classification
    Then Verify Working Capital loan disbursement transaction has classification
    Then Working Capital disbursal transaction business event is raised with "100" amount and reversed "false"
    Then Working Capital loan status will be "ACTIVE"
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0             | 100.0             | 100.0              | 1.0               | null     |
    Then Admin successfully undo Working Capital disbursal
    Then Working Capital undo disbursal transaction business event is raised with "100" amount and reversed "true"
    Then Working Capital loan status will be "APPROVED"
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | realizedIncome | unrealizedIncome |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 100.0              | 1.0               | null     | 0.0                | 0.0            | 0.0              |

  @TestRailId:C111076
  Scenario: Verify undo disbursal discards the breach and delinquency schedules, every action, reverses every rate change and re-projects the amortization schedule - UC10
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 33.33               |                      |
    And Admin creates WC Delinquency Bucket With Values:
      | frequency | frequencyType | minimumPaymentType | minimumPayment |
      | 2         | WEEKS         | FLAT               | 248            |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount | delinquencyBucketId | delinquencyGraceDays |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        | LAST_CREATED        | 13                   |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has 1 period
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 9            | 90.0             | 90.0              | null       | null   |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-27 | 248.0          | 0.0        | 248.0             | null                  | null             | null           |
    # --- everything an active loan can accumulate on top of its schedules --- #
    When Admin initiate a Working Capital loan delinquency pause with startDate "02 January 2026" and endDate "05 January 2026"
    And Admin creates a near breach reschedule action with threshold "40" frequency 4 frequencyType "DAYS"
    And Admin initiate a Working Capital loan breach disable with startDate "02 January 2026"
    And Admin update Working Capital period payment rate with "12.5" value
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 02 January 2026 | 18.0          | 12.5     | false    |
    # --- undo: back to Approved on a clean slate --- #
    When Admin successfully undo Working Capital disbursal
    Then Working Capital loan status will be "APPROVED"
    And Working Capital loan breach schedule has no data
    And Working Capital loan delinquency range schedule has no data on a not yet disbursed loan
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    And Working Capital loan has no delinquency actions
    And Working Capital loan has no breach actions
    And Working Capital loan has no near breach actions
    And Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 02 January 2026 | 18.0          | 12.5     | true     |
    And Working capital loan details has the following field values:
      | status.value         | Approved |
      | delinquencyStartDate | null     |
      | breachStartDate      | null     |
    When Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount |
      | 0.00              | 9000.00               | 100000.00          | 18                | 360         |
    # --- a later re-disbursement builds its schedules from its own date, not the undone one --- #
    When Admin successfully disburse the Working Capital loan on "02 January 2026" with "9000" EUR transaction amount
    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has 1 period
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-02 | 2026-01-10 | 9            | 90.0             | 90.0              | null       | null   |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-02 | 2026-01-28 | 248.0          | 0.0        | 248.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "03 January 2026"

  @TestRailId:C111077
  Scenario: Verify undo approval deletes the amortization projection and a later approval and disbursement rebuild the schedules from their own date - UC11
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 33.33               |                      |
    And Admin creates WC Delinquency Bucket With Values:
      | frequency | frequencyType | minimumPaymentType | minimumPayment |
      | 2         | WEEKS         | FLAT               | 248            |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount | delinquencyBucketId | delinquencyGraceDays |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 800      | LAST_CREATED        | 13                   |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has 1 period
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-09 | 9            | 90.0             | 90.0              | null       | null   |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-27 | 248.0          | 0.0        | 248.0             | null                  | null             | null           |
    # --- everything an active loan can accumulate on top of its schedules --- #
    When Admin initiate a Working Capital loan delinquency pause with startDate "02 January 2026" and endDate "05 January 2026"
    And Admin creates a near breach reschedule action with threshold "40" frequency 4 frequencyType "DAYS"
    And Admin initiate a Working Capital loan breach disable with startDate "02 January 2026"
    # --- undo disbursal clears what the active loan accumulated, then undo approval goes back to submitted --- #
    When Admin successfully undo Working Capital disbursal
    Then Working Capital loan status will be "APPROVED"
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    Then Working Capital loan status will be "SUBMITTED_AND_PENDING_APPROVAL"
    And Working Capital loan breach schedule has no data
    And Working Capital loan delinquency range schedule has no data on a not yet disbursed loan
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
    And Working Capital loan has no delinquency actions
    And Working Capital loan has no breach actions
    And Working Capital loan has no near breach actions
    And Working capital loan details has the following field values:
      | status.value         | Submitted and pending approval |
      | delinquencyStartDate | null                           |
      | breachStartDate      | null                           |
    # --- a submitted loan has no projection: the approval-time one went with the approval --- #
    Then Working Capital loan has no projected amortization schedule
    # --- a later re-approve and re-disbursement builds its schedules from its own date, not the undone one --- #
    And Admin successfully approves the working capital loan on "02 January 2026" with "8000" amount and expected disbursement date on "02 January 2026"
    When Admin successfully disburse the Working Capital loan on "02 January 2026" with "8000" EUR transaction amount and "500" discount amount
    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has 1 period
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-02 | 2026-01-10 | 9            | 90.0             | 90.0              | null       | null   |
    And Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-02 | 2026-01-28 | 248.0          | 0.0        | 248.0             | null                  | null             | null           |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "03 January 2026"

  @TestRailId:C74471
  Scenario Outline: Verify WC Loan creation with invalid breachId results in error - UC1
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    Then Creating a working capital loan with breachId <breach_id> on "01 January 2027" will result with status code <expected_status_code>
    Examples:
      | breach_id           | expected_status_code |
      | 0                   | 400                  |
      | 9223372036854775807 | 403                  |

  @TestRailId:C74472
  Scenario Outline: Verify WC Loan breach override on application depends on product config - UC2
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    Then Creating a working capital loan with breach override allowed "<breach_override_allowed>" on "01 January 2027" will result with status code <expected_status_code>
    Examples:
      | breach_override_allowed | expected_status_code |
      | true                    | 200                  |
      | false                   | 400                  |

  @TestRailId:C76687
  Scenario: Verify WC Loan account creation with breach override value while breach override is allowed on WCLP is successful - UC3
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates working capital loan with 2 "YEARS" breach override and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | WCLP_BREACH | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |
    Then Verify working capital loan account has been created with correct breach override data

  @TestRailId:C76688
  Scenario: Verify WC Loan account creation with breach value from WCLP is successful - UC4
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_BREACH_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2027 | 01 January 2027          | 100             | 100                | 1                 |          |
    Then Verify working capital loan account has been created with correct breach data inherited from WCLP level

  @TestRailId:C76689
  Scenario: Verify WC Loan account creation with breach value override while breach override is disallowed on WCLP is failed - UC5
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin failed to create working capital loan while breach override disallowed with breach override and default following data:
      | LoanProduct                              | submittedOnDate |
      | WCLP_BREACH_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2026 |
    When Admin deletes WC Breach override

  @TestRailId:C76747
  Scenario: Verify WC Loan account modify with breach override value while breach override is allowed on WCLP is successful - UC6
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates working capital loan with breach from WCLP while override is allowed and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | WCLP_BREACH | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |
    Then Verify working capital loan account has been created with correct breach data
    And Admin modifies the working capital loan with 3 "WEEKS" breach override data
    Then Verify working capital loan account has been created with correct breach override data
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP_BREACH  | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |

  @TestRailId:C76748
  Scenario: Verify WC Loan account modify with breach value from WCLP is successful - UC7
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_BREACH_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2027 | 01 January 2027          | 100             | 100                | 1                 |          |
    Then Verify working capital loan account has been created with correct breach data inherited from WCLP level
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 |                          | 80.0            |                    |                   |          |
    And Admin creates a working capital loan with the following data:
      | LoanProduct                              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_BREACH_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2027 | 01 January 2027          | 100             | 100                | 1                 |          |
    Then Verify working capital loan account has been created with correct breach data inherited from WCLP level

  @TestRailId:C76749
  Scenario: Verify WC Loan account modify with breach value override while breach override is disallowed on WCLP is failed - UC8
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_BREACH_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2027 | 01 January 2027          | 100             | 100                | 1                 |          |
    Then Verify working capital loan account has been created with correct breach data inherited from WCLP level
    And Admin failed to modify working capital loan while breach override disallowed with breach override
    When Admin deletes WC Breach override

  @TestRailId:C76750
  Scenario Outline: Verify WC Loan modify with invalid breachId results in error - UC9
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    Then Modify a working capital loan with breachId <breach_id> will result with <response_status_code> and <response_message> error message

    Examples:
      | breach_id           | response_status_code | response_message                                                    |
      | 0                   | 400                  | "The parameter `breachId` must be greater than 0."                  |
      | 9223372036854775807 | 403                  | "Working Capital Breach with id 9223372036854775807 was not found." |

  @TestRailId:C76690
  Scenario: Verify WC Loan account creation with nearBreach override value while breach override is allowed on WCLP is successful - UC1
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates working capital loan with 2 "MONTHS" breach and 1 "MONTHS" near breach override and the following data:
      | LoanProduct             | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | WCLP_BREACH_NEAR_BREACH | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |
    Then Verify working capital loan account has been created with correct breach and near breach override data

  @TestRailId:C76691
  Scenario: Verify WC Loan account creation with nearBreach override value while breach override is allowed on WCLP is successful - UC2
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates working capital loan with breach override allowed with 2 "YEARS" breach and 10 "DAYS" near breach and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |
    Then Verify working capital loan account has been created with correct breach and near breach data

  @TestRailId:C76692
  Scenario: Verify WC Loan account creation with nearBreach value from WCLP level is successful - UC3
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                                          | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_BREACH_NEAR_BREACH_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2027 | 01 January 2027          | 100             | 100                | 1                 |          |
    Then Verify working capital loan account has been created with correct breach and near breach data inherited from WCLP level

  @TestRailId:C76693
  Scenario: Verify WC Loan account creation with near breach value override while breach override is disallowed on WCLP is failed - UC4
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin failed to create working capital loan while breach override disallowed with breach and near breach override and default following data:
      | LoanProduct                       | submittedOnDate |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2026 |
    When Admin deletes WC Breach override
    When Admin deletes WC Near Breach override

  @TestRailId:C76694
  Scenario Outline: Verify WC Loan account creation with with breach frequency lower then near breach frequency failed - UC5
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin failed to create WC loan account on "01 January 2027" with breach <breachFrequency> "<breachFrequencyType>" frequency lower then near breach <nearBreachFrequency> "<nearBreachFrequencyType>" frequency
    When Admin deletes WC Breach With Values
    When Admin deletes WC Near Breach With Values

    Examples:
      | breachFrequency | breachFrequencyType | nearBreachFrequency | nearBreachFrequencyType |
      | 1               | MONTHS              | 3                   | MONTHS                  |
      | 2               | MONTHS              | 2                   | MONTHS                  |
      | 10              | DAYS                | 12                  | DAYS                    |
      | 1               | YEARS               | 3                   | YEARS                   |
      | 2               | MONTHS              | 70                  | DAYS                    |
      | 10              | MONTHS              | 1                   | YEARS                   |
      | 4               | WEEKS               | 1                   | MONTHS                  |
      | 10              | DAYS                | 3                   | WEEKS                   |

  @TestRailId:C76695
  Scenario: Admin failed to create Working Capital without breach but with near breach failure - U6
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin failed to create WC loan account on "01 January 2027" without breach, but with near breach

  @TestRailId:C76696
  Scenario Outline: Verify WC Loan creation with invalid nearBreachId results in error - UC7
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    Then Creating a working capital loan with near breachId <near_breach_id> on "01 January 2027" will result with error
    Examples:
      | near_breach_id      |
      | 0                   |
      | 9223372036854775807 |

  @TestRailId:C76751
  Scenario: Verify WC Loan account modify with breach and near breach values successful - UC8
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    And Admin modifies the working capital loan with 70 "DAYS" breach and 4 "WEEKS" near breach override data
    Then Verify working capital loan account has been created with correct breach and near breach override data
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |

  @TestRailId:C76752
  Scenario: Verify WC Loan account modify with breach and near breach values successful - UC9
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates working capital loan with breach and near breach from WCLP while override is allowed and the following data:
      | LoanProduct             | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate |
      | WCLP_BREACH_NEAR_BREACH | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |
    Then Verify working capital loan account has been created with correct breach and near breach data
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name            | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP_BREACH_NEAR_BREACH | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    And Admin modifies the working capital loan with 1 "YEARS" breach and 10 "MONTHS" near breach override data
    Then Verify working capital loan account has been created with correct breach and near breach override data
    Then Working capital loan account has the correct data:
      | product.name            | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP_BREACH_NEAR_BREACH | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |

  @TestRailId:C76753
  Scenario: Verify WC Loan account modify with nearBreach value from WCLP level is successful - UC10
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                                          | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_BREACH_NEAR_BREACH_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2027 | 01 January 2027          | 100             | 100                | 1                 |          |
    Then Verify working capital loan account has been created with correct breach and near breach data inherited from WCLP level

    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 |                          | 80.0            |                    |                   |          |
    Then Working capital loan account has the correct data:
      | product.name                                         | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_BREACH_NEAR_BREACH_DISALLOW_ATTRIBUTES_OVERRIDE | 2027-01-01      | 2027-01-01               | Submitted and pending approval | 80.0              | 0.0               | 100.0              | 1.0               | null     |
    Then Verify working capital loan account has been created with correct breach and near breach data inherited from WCLP level

  @TestRailId:C76754
  Scenario: Verify WC Loan account modify with near breach value override while breach override is disallowed on WCLP is failed - UC11
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                       | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2027 | 01 January 2027          | 100             | 100                | 1                 |          |
    Then Verify working capital loan account has been created with none breach nor near breach data
    And Admin failed to modify working capital loan while breach override disallowed with breach and near breach override
    When Admin deletes WC Breach override
    When Admin deletes WC Near Breach override

  @TestRailId:C76755
  Scenario Outline: Verify WC Loan account modify with with breach frequency lower then near breach frequency failed - UC12
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    And Admin failed to modify WC loan account with breach <breachFrequency> "<breachFrequencyType>" frequency lower then near breach <nearBreachFrequency> "<nearBreachFrequencyType>" frequency
    When Admin deletes WC Breach With Values
    When Admin deletes WC Near Breach With Values

    Examples:
      | breachFrequency | breachFrequencyType | nearBreachFrequency | nearBreachFrequencyType |
      | 1               | MONTHS              | 3                   | MONTHS                  |
      | 2               | MONTHS              | 2                   | MONTHS                  |
      | 10              | DAYS                | 12                  | DAYS                    |
      | 1               | YEARS               | 3                   | YEARS                   |
      | 2               | MONTHS              | 70                  | DAYS                    |
      | 10              | MONTHS              | 1                   | YEARS                   |
      | 4               | WEEKS               | 1                   | MONTHS                  |
      | 10              | DAYS                | 3                   | WEEKS                   |

  @TestRailId:C76756
  Scenario: Admin failed to modify Working Capital without breach but with near breach failure - U13
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    And Admin failed to modify WC loan account without breach, but with near breach

  @TestRailId:C76757
  Scenario Outline: Verify WC Loan creation with invalid nearBreachId results in error - UC14
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    Then Modify a working capital loan with near breachId <near_breach_id> will result with an error

    Examples:
      | near_breach_id      |
      | 0                   |
      | 9223372036854775807 |


  @TestRailId:C78813
  Scenario Outline: Period payment rate on create of WCL account failed with outranged from loan product level rate change value - UC1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin failed to create Working Capital with period payment rate "<rate_change_value>" value and outcomes with <rate_change_error_message> error message with default following data:
      | LoanProduct              | submittedOnDate |
      | WCLP_PERIOD_PAYMENT_RATE | 01 January 2026 |

    Examples:
      | rate_change_value | rate_change_error_message                                            |
      | 0.5               | Failed data validation due to: must.be.greater.than.or.equal.to.min. |
      | 99.5              | Failed data validation due to: must.be.less.than.or.equal.to.max.    |

  @TestRailId:C78814
  Scenario Outline: Period payment rate on create of WCL account failed with invalid rate change value - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin failed to create Working Capital on "01 January 2026" with period payment rate "<rate_change_value>" value and outcomes with <rate_change_error_message> error message

    Examples:
      | rate_change_value | rate_change_error_message                                             |
      | -1                | The parameter `periodPaymentRate` must be greater than or equal to 0. |

  @TestRailId:C78815
  Scenario Outline: Period payment rate on modify of WCL account failed with with outranged from loan product level rate change value - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_PERIOD_PAYMENT_RATE | 01 January 2026 | 01 January 2026          | 100             | 100                | 12.5              | 15       |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP_PERIOD_PAYMENT_RATE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 12.5              | 15.0             |
    Then Admin failed to modify WC loan account with period payment rate "<rate_change_value>" value and outcomes with "<rate_change_error_message>" error message

    Examples:
      | rate_change_value | rate_change_error_message                                            |
      | 0.5               | Failed data validation due to: must.be.greater.than.or.equal.to.min. |
      | 99.5              | Failed data validation due to: must.be.less.than.or.equal.to.max.    |

  @TestRailId:C78816
  Scenario Outline: Period payment rate on modify of WCL account failed with invalid rate change value - UC4
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 12.5              | 15       |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 12.5              | 15.0             |
    Then Admin failed to modify WC loan account with period payment rate "<rate_change_value>" value and outcomes with "<rate_change_error_message>" error message

    Examples:
      | rate_change_value | rate_change_error_message                                             |
      | -1                | The parameter `periodPaymentRate` must be greater than or equal to 0. |

  @TestRailId:C89784
  Scenario: Verify loan creation with input values with discount that cause unable to calculate a valid EIR leads to error - UC1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    Then Creating a working capital loan with input values that cause unable to calculate a valid EIR will result into an error:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 17           | 18                | 1000     |

  @TestRailId:C89785
  Scenario: Verify loan creation with input values without discount that cause unable to calculate a valid EIR leads to error - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    Then Creating a working capital loan with input values that cause unable to calculate a valid EIR will result into an error:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 17           | 18                | 0        |

  @TestRailId:C89786
  Scenario: Verify loan creation with input values that allows to calculate a valid EIR leads to creating Working capital loan account - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 5000            | 5500         | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "5000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "5000" EUR transaction amount
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields with positive effectiveInterestRate value:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount | originalPaymentNumber |
      | 0.00              | 5000.00               | 5500.00            | 18                | 360         | 2.75                  | 1819                  |

  @TestRailId:C89787
  Scenario: Verify loan modification without input values with discount that cause unable to calculate a valid EIR leads to error - UC4
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 5000            | 5500         | 18                | 0        |
    Then Admin failed to modify working capital loan with total payment value "17" that cause unable to calculate EIR
    Then Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 5000.0            | 0.0               | 5500.0             | 18.0              | 0.0              |

  @TestRailId:C89788
  Scenario: Verify loan update with input value as change period payment rate cause unable to calculate a valid EIR and leads to error - UC5
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 1000     |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 9000.0            | 9000.0            | 100000.0           | 18.0              | 1000.0   |
    Then Admin update Working Capital period payment rate failed with "0.01" value cause unable to calculate EIR
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 9000.0            | 9000.0            | 100000.0           | 18.0              | 1000.0   |

  @TestRailId:C89789
  Scenario: Verify loan creation with input values with non-overridable pre-defined discount that unable to calculate a valid EIR leads to error - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with discount value "5000" that is forbidden to be overridden
    Then Creating a working capital loan using created product with input values that cause unable to calculate a valid EIR will result into an error:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 5000            | 55           | 18                |          |

  @TestRailId:C89790
  Scenario: Verify loan creation with input values with overridden discount to none value that allows to calculate a valid EIR leads creating Working Capital loan account- UC7
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct   | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DISCOUNT | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    Then Working capital loan account has the correct data:
      | product.name  | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP_DISCOUNT | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 9000.0            | 0.0               | 100000.0           | 18.0              | 0.0              |


  @TestRailId:C110979
  Scenario: Verify loan creation with non-overridable pre-defined discount above the principal leads to error - UC8
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with discount value "300000" that is forbidden to be overridden
    Then Creating a working capital loan using created product with discount that exceeds principal amount will result into an error:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 5000            | 5500         | 18                |          |
