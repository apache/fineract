@WorkingCapitalBreachEvaluationFeature
Feature: Working Capital Breach Evaluation

  @TestRailId:C76608
  Scenario: Verify that full payment covering minPayment results in breach false after period end
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | MONTHS              | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Admin makes Internal Payment "500.0" on "2026-01-15"
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 500.00           | 0.00              | null       | false  |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 500.00           | 500.00            | null       | null   |

  @TestRailId:C76609
  Scenario: Verify that partial payment less than minPayment results in breach true after period end
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | MONTHS              | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Admin makes Internal Payment "200.0" on "2026-01-15"
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 500.00           | 300.00            | null       | true   |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 500.00           | 500.00            | null       | null   |

  @TestRailId:C76610
  Scenario: Verify that no payment results in breach true after period end
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | MONTHS              | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 500.00           | 500.00            | null       | true   |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 500.00           | 500.00            | null       | null   |

  @TestRailId:C76611
  Scenario: Verify that excess payment in a later period does not retroactively clear previous period breach
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | MONTHS              | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Period 1 passes with no payment
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Now make excess payment of 1500 (3x minPayment) in period 2
    When Admin sets the business date to "10 February 2026"
    And Admin makes Internal Payment "1500.0" on "2026-02-10"
    When Admin sets the business date to "01 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Period 1 stays breach=true (NOT retroactively cleared per NOTE1)
    # Period 2: paid=1500 >= min=500 -> breach=false
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 500.00           | 500.00            | null       | true   |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 500.00           | 0.00              | null       | false  |
      | 3            | 2026-03-01 | 2026-03-31 | 31           | 500.00           | 500.00            | null       | null   |

  @TestRailId:C76612
  Scenario: Verify that multiple partial payments summing to minPayment result in breach false
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | MONTHS              | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "10 January 2026"
    And Admin makes Internal Payment "200.0" on "2026-01-10"
    And Admin makes Internal Payment "150.0" on "2026-01-10"
    When Admin sets the business date to "20 January 2026"
    And Admin makes Internal Payment "150.0" on "2026-01-20"
    # Total paid = 200+150+150 = 500 = minPayment -> NOT a breach
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 500.00           | 0.00              | null       | false  |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 500.00           | 500.00            | null       | null   |

  @TestRailId:C76613
  Scenario: Verify that payment on exact period end date applies to that period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | MONTHS              | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Pay full amount on the LAST day of period 1
    When Admin sets the business date to "31 January 2026"
    And Admin makes Internal Payment "500.0" on "2026-01-31"
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 500.00           | 0.00              | null       | false  |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 500.00           | 500.00            | null       | null   |

  #TODO pending implementation of grace days support for breach management
  @Skip
  @TestRailId:C76614
  Scenario: Verify that breach evaluation matches CSV example with 90 DAY frequency and partial payment
    When Admin sets the business date to "01 January 2019"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 90              | DAYS                | PERCENTAGE                  | 9            | 3                    |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | 01 January 2019 | 01 January 2019          | 9000            | 100000       | 18                | 1000     |
    And Admin successfully approves the working capital loan on "01 January 2019" with "9000" amount and expected disbursement date on "01 January 2019"
    When Admin successfully disburse the Working Capital loan on "01 January 2019" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Period 1: fromDate=Jan04 (disburse+3 grace), 90 days, min=(9000+1000)*9%=900
    # CSV reference: row 29 — Start=1/4/2019, End=4/4/2019, Days=90, Min=900
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2019-01-04 | 2019-04-04 | 90           | 900.00           | 900.00            | null       | null   |
    # Payment of 250 on Jan 5 (within period 1)
    When Admin sets the business date to "05 January 2019"
    And Admin makes Internal Payment "250.0" on "2019-01-05"
    # Outstanding should decrease mid-period
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2019-01-04 | 2019-04-04 | 90           | 900.00           | 650.00            | null       | null   |
    # Advance past period 1 end: 250 < 900 -> breach=true
    # CSV reference: row 30 — Period 2 Start=4/4/2019, End=7/3/2019, Days=90
    When Admin sets the business date to "05 April 2019"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2019-01-04 | 2019-04-04 | 90           | 900.00           | 650.00            | null       | true   |
      | 2            | 2019-04-04 | 2019-07-03 | 90           | 900.00           | 900.00            | null       | null   |

  @TestRailId:C76615
  Scenario: Verify that paidAmount and outstandingAmount update immediately on payment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | MONTHS              | FLAT                        | 1000         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 1000.00          | 1000.00           | null       | null   |
    # First partial payment
    When Admin sets the business date to "10 January 2026"
    And Admin makes Internal Payment "400.0" on "2026-01-10"
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 1000.00          | 600.00            | null       | null   |
    # Second partial payment clears the rest — paidAmount >= minPayment, breach resolves to false immediately
    When Admin sets the business date to "20 January 2026"
    And Admin makes Internal Payment "600.0" on "2026-01-20"
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 1000.00          | 0.00              | null       | false  |
    # After period end -> breach=false since fully paid
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 1000.00          | 0.00              | null       | false  |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 1000.00          | 1000.00           | null       | null   |

  @TestRailId:C76616
  Scenario: Verify that payment exceeding minPayment does not carry over to next period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | MONTHS              | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Pay 800 in period 1 (300 above minPayment=500)
    When Admin sets the business date to "15 January 2026"
    And Admin makes Internal Payment "800.0" on "2026-01-15"
    # Period 2 still starts with full outstanding=500, unaffected by period 1 overpayment
    When Admin sets the business date to "01 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 500.00           | 0.00              | null       | false  |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 500.00           | 500.00            | null       | true   |
      | 3            | 2026-03-01 | 2026-03-31 | 31           | 500.00           | 500.00            | null       | null   |
