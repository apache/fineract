@WorkingCapital
@WorkingCapitalLoanDetailsFeature
Feature: Working Capital Loan Details

  @TestRailId:C85423
  Scenario: Loan details GET returns the general fields aligned with the Term loan API
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Verify Working Capital loan disbursement was successful
    Then Working capital loan details has the following field values:
      | id                                             | present                      |
      | accountNo                                      | present                      |
      | clientId                                       | present                      |
      | clientAccountNo                                | present                      |
      | clientName                                     | present                      |
      | clientOfficeId                                 | 1                            |
      | loanProductId                                  | present                      |
      | loanProductName                                | WCLP                         |
      | loanProductDescription                         | Working Capital Loan Product |
      | externalId                                     | present                      |
      | clientExternalId                               | present                      |
      | status.value                                   | Active                       |
      | status.id                                      | 300                          |
      | status.code                                    | loanStatusType.active        |
      | status.pendingApproval                         | false                        |
      | status.waitingForDisbursal                     | false                        |
      | status.active                                  | true                         |
      | status.closedObligationsMet                    | false                        |
      | status.closedWrittenOff                        | false                        |
      | status.closedRescheduled                       | false                        |
      | status.closed                                  | false                        |
      | status.overpaid                                | false                        |
      | proposedPrincipal                              | 100.0                        |
      | approvedPrincipal                              | 100.0                        |
      | principal                                      | 100.0                        |
      | netDisbursalAmount                             | 100.0                        |
      | totalPaymentVolume                             | 100.0                        |
      | paymentRate                                    | 1.0                          |
      | periodPaymentAmount                            | 0.0                          |
      | numberOfRepayments                             | 36000                        |
      | dailyEir                                       | 0.0                          |
      | calculatedAnnualEir                            | 0.0                          |
      | proposedDiscountFee                            | 0.0                          |
      | approvedDiscountFee                            | null                         |
      | discountFee                                    | null                         |
      | amortizationType.code                          | EIR                          |
      | amortizationType.value                         | present                      |
      | npvDayCount                                    | 360                          |
      | loanProductCounter                             | 1                            |
      | currency.code                                  | EUR                          |
      | currency.name                                  | Euro                         |
      | currency.decimalPlaces                         | 2                            |
      | currency.inMultiplesOf                         | 1                            |
      | currency.displaySymbol                         | €                            |
      | currency.nameCode                              | currency.EUR                 |
      | currency.displayLabel                          | Euro (€)                     |
      | fundId                                         | null                         |
      | fundName                                       | null                         |
      | repaymentEvery                                 | 30                           |
      | repaymentFrequencyType.code                    | DAYS                         |
      | repaymentFrequencyType.value                   | present                      |
      | delinquencyGraceDays                           | null                         |
      | delinquencyStartType.code                      | null                         |
      | breachGraceDays                                | 0                            |
      | delinquencyBucket                              | present                      |
      | delinquencyBucket.id                           | present                      |
      | delinquencyBucket.name                         | present                      |
      | delinquencyBucket.ranges                       | present                      |
      | delinquencyBucket.bucketType                   | WORKING_CAPITAL              |
      | breach                                         | null                         |
      | nearBreach                                     | null                         |
      | breachStartDate                                | null                         |
      | delinquencyStartDate                           | null                         |
      | lastClosedBusinessDate                         | null                         |
      | balance.principal                              | 100.0                        |
      | balance.principalPaid                          | 0.0                          |
      | balance.principalOutstanding                   | 100.0                        |
      | balance.totalDisbursement                      | 0.0                          |
      | balance.totalRepayment                         | 0.0                          |
      | balance.totalOutstanding                       | 100.0                        |
      | balance.totalExpectedRepayment                 | 100.0                        |
      | balance.id                                     | present                      |
      | balance.fee                                    | 0.0                          |
      | balance.feePaid                                | 0.0                          |
      | balance.feeOutstanding                         | 0.0                          |
      | balance.penalty                                | 0.0                          |
      | balance.penaltyPaid                            | 0.0                          |
      | balance.penaltyOutstanding                     | 0.0                          |
      | balance.realizedIncomeFromDiscountFee          | 0.0                          |
      | balance.unrealizedIncomeFromDiscountFee        | 0.0                          |
      | balance.overpaymentAmount                      | 0.0                          |
      | balance.totalDiscountFee                       | 0.0                          |
      | balance.totalDiscountFeeAdjustment             | 0.0                          |
      | summary.principal                              | 100.0                        |
      | summary.principalOutstanding                   | 100.0                        |
      | summary.totalDisbursement                      | 0.0                          |
      | summary.totalOutstanding                       | 100.0                        |
      | summary.currency.code                          | EUR                          |
      | summary.currency.name                          | Euro                         |
      | summary.currency.decimalPlaces                 | 2                            |
      | summary.currency.inMultiplesOf                 | 1                            |
      | summary.currency.displaySymbol                 | €                            |
      | summary.currency.nameCode                      | currency.EUR                 |
      | summary.currency.displayLabel                  | Euro (€)                     |
      | summary.principalPaid                          | 0.0                          |
      | summary.fee                                    | 0.0                          |
      | summary.feePaid                                | 0.0                          |
      | summary.feeOutstanding                         | 0.0                          |
      | summary.penalty                                | 0.0                          |
      | summary.penaltyPaid                            | 0.0                          |
      | summary.penaltyOutstanding                     | 0.0                          |
      | summary.realizedIncomeFromDiscountFee          | 0.0                          |
      | summary.unrealizedIncomeFromDiscountFee        | 0.0                          |
      | summary.overpayment                            | 0.0                          |
      | summary.totalDiscountFee                       | 0.0                          |
      | summary.totalDiscountFeeAdjustment             | 0.0                          |
      | summary.totalExpectedRepayment                 | 100.0                        |
      | summary.totalRepayment                         | 0.0                          |
      | delinquent.pastDueDays                         | 0                            |
      | delinquent.delinquentDays                      | 0                            |
      | delinquent.delinquentAmount                    | 0.0                          |
      | delinquent.delinquentPrincipal                 | 0.0                          |
      | delinquent.delinquencyPausePeriods.size        | 0                            |
      | delinquent.delinquentDate                      | null                         |
      | delinquent.installmentLevelDelinquency.size    | 0                            |
      | timeline.submittedOnDate                       | 2026-01-01                   |
      | timeline.submittedByUsername                   | mifos                        |
      | timeline.submittedByFirstname                  | App                          |
      | timeline.submittedByLastname                   | Administrator                |
      | timeline.approvedOnDate                        | 2026-01-01                   |
      | timeline.approvedByUsername                    | mifos                        |
      | timeline.approvedByFirstname                   | App                          |
      | timeline.approvedByLastname                    | Administrator                |
      | timeline.rejectedOnDate                        | null                         |
      | timeline.expectedDisbursementDate              | 2026-01-01                   |
      | timeline.actualDisbursementDate                | 2026-01-01                   |
      | timeline.disbursedByUsername                   | mifos                        |
      | timeline.disbursedByFirstname                  | App                          |
      | timeline.disbursedByLastname                   | Administrator                |
      | timeline.expectedMaturityDate                  | null                         |
      | timeline.actualMaturityDate                    | null                         |
      | charges.size                                   | 0                            |
      | disbursementDetails.size                       | 1                            |
      | disbursementDetails.0.id                       | present                      |
      | disbursementDetails.0.loanId                   | present                      |
      | disbursementDetails.0.principal                | 100.0                        |
      | disbursementDetails.0.expectedDisbursementDate | 2026-01-01                   |
      | disbursementDetails.0.actualDisbursementDate   | 2026-01-01                   |
      | paymentAllocation.size                         | 1                            |
      | paymentAllocation.0.transactionType            | present                      |
      | paymentAllocation.0.paymentAllocationOrder     | present                      |
      | originators.size                               | 0                            |
      | enableInstallmentLevelDelinquency              | true                         |
      | fraud                                          | null                         |
      | chargedOff                                     | null                         |
    And Working capital loan details has the auto-generated fields present

  @TestRailId:C85424
  Scenario: Loan details GET returns the fund when the loan is created with one
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with fund and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    Then Working capital loan details has the following field values:
      | fundId   | 1        |
      | fundName | Lender A |

  @TestRailId:C85425
  Scenario: Loan details GET returns the charges added to the loan
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 35.0 transaction amount
    Then Working capital loan details has the following field values:
      | charges.size                          | 1                        |
      | charges.0.id                          | present                  |
      | charges.0.chargeId                    | present                  |
      | charges.0.name                        | Working Capital Loan Fee |
      | charges.0.amount                      | 35.0                     |
      | charges.0.amountOutstanding           | 35.0                     |
      | charges.0.dueDate                     | 2026-01-12               |
      | charges.0.penalty                     | false                    |
      | charges.0.paid                        | false                    |
      | charges.0.loanId                      | present                  |
      | charges.0.externalId                  | present                  |
      | charges.0.externalLoanId              | present                  |
      | charges.0.currency.code               | EUR                      |
      | charges.0.chargeTimeType.value        | Specified due date       |
      | charges.0.chargeCalculationType.value | Flat                     |
      | charges.0.chargePaymentMode.value     | Regular                  |

  @TestRailId:C85444
  Scenario: Loan details GET returns the originator attached to loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID and name "WC Inline Originator"
    And Admin creates a working capital loan with originator attached inline and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    Then Working capital loan details has the originator attached
    Then Working capital loan details has the following field values:
      | originators.size                      | 1                        |
      | originators.0.id                      | present                  |
      | originators.0.externalId              | present                  |
      | originators.0.name                    | WC Inline Originator     |
      | originators.0.status                  | ACTIVE                   |

  @TestRailId:C85445
  Scenario: Loan details GET returns the breach and near breach values
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
    Then Working capital loan details has the following field values:
      | breach.id                                | present                  |
      | breach.name                              | present                  |
      | breach.breachFrequency                   | 70                       |
      | breach.breachFrequencyType.code          | DAYS                     |
      | breach.breachFrequencyType.value         | DAYS                     |
      | breach.breachAmountCalculationType.code  | PERCENTAGE               |
      | breach.breachAmountCalculationType.value | Percentage               |
      | breach.breachAmount                      | present                  |
      | nearBreach.id                            | present                  |
      | nearBreach.name                          | present                  |
      | nearBreach.frequency                     | 4                        |
      | nearBreach.frequencyType.code            | WEEKS                    |
      | nearBreach.frequencyType.value           | WEEKS                    |
      | nearBreach.threshold                     | present                  |
