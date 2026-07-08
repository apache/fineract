@WorkingCapital
@WorkingCapitalDelinquencyResetActionFeature @WCCOBFeature
Feature: Working Capital Delinquency Reset Action

  @TestRailId:TODO_ADD_1
  Scenario: Verify that Reset Delinquency Action has correct values on the delinquency range schedule and delinquency tags are lifted then verify undo
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount
    When Admin sets the business date to "01 June 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 0          | 300               | false                 |
      | 6            | 31 May 2026     | 29 June 2026    | 300            | 0          | 300               |                       |

    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 5            | 2026-06-01  |              | D00            | 1              | 30             |
      | 4            | 2026-06-01  |              | D30            | 31             | 60             |
      | 3            | 2026-06-01  |              | D60            | 61             | 90             |
      | 2            | 2026-06-01  |              | D90            | 91             | 120            |
      | 1            | 2026-06-01  |              | D120           | 121            | 150            |
    When Admin creates WC delinquency reset action
    Then WC loan last delinquency action has the following data:
      | action | startDate    | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 01 June 2026 |                |                    |           |               |
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   |                |            |                   |                       |
      | 5            | 01 May 2026     | 30 May 2026     |                |            |                   |                       |
      | 6            | 31 May 2026     | 29 June 2026    | 300            | 0          | 300               |                       |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 5            | 2026-06-01  | 2026-06-01   | D00            | 1              | 30             |
      | 4            | 2026-06-01  | 2026-06-01   | D30            | 31             | 60             |
      | 3            | 2026-06-01  | 2026-06-01   | D60            | 61             | 90             |
      | 2            | 2026-06-01  | 2026-06-01   | D90            | 91             | 120            |
      | 1            | 2026-06-01  | 2026-06-01   | D120           | 121            | 150            |

    When Admin sets the business date to "01 July 2026"
    When Admin runs inline COB job for Working Capital Loan

    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 6            | 2026-06-30  |              | D00            | 1              | 30             |
      | 5            | 2026-06-01  | 2026-06-01   | D00            | 1              | 30             |
      | 4            | 2026-06-01  | 2026-06-01   | D30            | 31             | 60             |
      | 3            | 2026-06-01  | 2026-06-01   | D60            | 61             | 90             |
      | 2            | 2026-06-01  | 2026-06-01   | D90            | 91             | 120            |
      | 1            | 2026-06-01  | 2026-06-01   | D120           | 121            | 150            |

    Then Admin creates WC delinquency undo reset action

    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 0          | 300               | false                 |
      | 6            | 31 May 2026     | 29 June 2026    | 300            | 0          | 300               | false                 |
      | 7            | 30 June 2026    | 29 July 2026    | 300            | 0          | 300               |                       |

    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 5            | 2026-07-01  |              | D30            | 31             | 60             |
      | 4            | 2026-07-01  |              | D60            | 61             | 90             |
      | 3            | 2026-07-01  |              | D90            | 91             | 120            |
      | 2            | 2026-07-01  |              | D120           | 121            | 150            |
      | 1            | 2026-07-01  |              | D150           | 151            | 180            |
      | 6            | 2026-06-30  |              | D00            | 1              | 30             |
      | 5            | 2026-06-01  | 2026-06-01   | D00            | 1              | 30             |
      | 4            | 2026-06-01  | 2026-06-01   | D30            | 31             | 60             |
      | 3            | 2026-06-01  | 2026-06-01   | D60            | 61             | 90             |
      | 2            | 2026-06-01  | 2026-06-01   | D90            | 91             | 120            |
      | 1            | 2026-06-01  | 2026-06-01   | D120           | 121            | 150            |
    Then Admin closes the Working Capital loan with a full repayment on "01 July 2026"
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 300        | 0                 | true                  |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 300        | 0                 | true                  |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 300        | 0                 | true                  |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 300        | 0                 | true                  |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 300        | 0                 | true                  |
      | 6            | 31 May 2026     | 29 June 2026    | 300            | 300        | 0                 | true                  |
      | 7            | 30 June 2026    | 29 July 2026    | 300            | 8200       | 0                 | true                  |

  @TestRailId:TODO_ADD_2
  Scenario: Verify multiple Reset Delinquency Action and undo reset
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount

    When Admin sets the business date to "31 January 2026"
    When Admin runs inline COB job for Working Capital Loan

    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               |                       |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-31  |              | D00            | 1              | 30             |

    When Admin creates WC delinquency reset action

    Then WC loan last delinquency action has the following data:
      | action | startDate       | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 31 January 2026 |                |                    |           |               |
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               |                       |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 1            | 2026-01-31  | 2026-01-31   | D00            | 1              | 30             |

    When Admin sets the business date to "2 March 2026"
    When Admin runs inline COB job for Working Capital Loan

    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               |                       |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 2            | 2026-03-02  |              | D00            | 1              | 30             |
      | 1            | 2026-01-31  | 2026-01-31   | D00            | 1              | 30             |

    When Admin creates WC delinquency reset action

    Then WC loan delinquency actions have the following data:
      | action | startDate       | endDate | minimumPayment | minimumPaymentType | frequency | frequencyType |
      | RESET  | 31 January 2026 |         |                |                    |           |               |
      | RESET  | 02 March 2026   |         |                |                    |           |               |
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               |                       |
    And Delinquency Tag History for Working Capital loan has lines:
      | periodNumber | addedOnDate | liftedOnDate | classification | minimumAgeDays | maximumAgeDays |
      | 2            | 2026-03-02  | 2026-03-02   | D00            | 1              | 30             |
      | 1            | 2026-01-31  | 2026-01-31   | D00            | 1              | 30             |

    When Admin sets the business date to "2 April 2026"
    When Admin runs inline COB job for Working Capital Loan

    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               |                       |

    Then Admin creates WC delinquency undo reset action
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               |                       |

    When Admin creates WC delinquency reset action

    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "02 April 2026"
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 10000      | 0                 | true                  |

  @TestRailId:TODO_ADD_3
  Scenario: Verify multiple Reset Delinquency Action and undo reset for 2nd and 4th reset
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 10000           | 10000              | 1                 | 0.0      |
    When Admin successfully approves the working capital loan on "01 January 2026" with "10000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "10000" EUR transaction amount

    When Admin sets the business date to "31 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 | 300            | 0          | 300               | false                 |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               |                       |
    When Admin creates WC delinquency reset action
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               |                       |

    When Admin sets the business date to "02 March 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   | 300            | 0          | 300               | false                 |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               |                       |

    When Admin creates WC delinquency reset action

    When Admin sets the business date to "02 April 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   | 300            | 0          | 300               | false                 |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               |                       |

    Then Admin creates WC delinquency undo reset action
    When Admin creates WC delinquency reset action

    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               |                       |


    When Admin sets the business date to "02 May 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 0          | 300               |                       |

    When Admin creates WC delinquency reset action
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   |                |            |                   |                       |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 0          | 300               |                       |

    Then Admin creates WC delinquency undo reset action

    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 0          | 300               | false                 |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 0          | 300               |                       |
    Then Admin closes the Working Capital loan with a full repayment on "02 May 2026"
    Then WC loan delinquency range schedule has the following periods:
      | periodNumber | fromDate        | toDate          | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet |
      | 1            | 01 January 2026 | 30 January 2026 |                |            |                   |                       |
      | 2            | 31 January 2026 | 01 March 2026   |                |            |                   |                       |
      | 3            | 02 March 2026   | 31 March 2026   |                |            |                   |                       |
      | 4            | 01 April 2026   | 30 April 2026   | 300            | 300        | 0                 | true                  |
      | 5            | 01 May 2026     | 30 May 2026     | 300            | 9700       | 0                 | true                  |
