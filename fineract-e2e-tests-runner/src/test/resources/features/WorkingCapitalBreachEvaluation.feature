@WorkingCapital
@WorkingCapitalBreach
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
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 500.0 transaction amount on Working Capital loan
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 500.00           | 0.00              | null       | false  |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 500.00           | 500.00            | null       | null   |
    When Customer undo "1"th working capital transaction made on "15 January 2026"
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 500.00           | 500.00            | null       | true   |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 500.00           | 500.00            | null       | null   |

  @TestRailId:C76609
  Scenario: Verify that partial payment less than minPayment results in breach true after period end
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | MONTHS              | FLAT                        | 500          |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 200.0 transaction amount on Working Capital loan
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
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
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
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Period 1 passes with no payment
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Now make excess payment of 1500 (3x minPayment) in period 2
    When Admin sets the business date to "10 February 2026"
    And Customer makes repayment on "10 February 2026" with 1500.0 transaction amount on Working Capital loan
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
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 200.0 transaction amount on Working Capital loan
    And Customer makes repayment on "10 January 2026" with 150.0 transaction amount on Working Capital loan
    When Admin sets the business date to "20 January 2026"
    And Customer makes repayment on "20 January 2026" with 150.0 transaction amount on Working Capital loan
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
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Pay full amount on the LAST day of period 1
    When Admin sets the business date to "31 January 2026"
    And Customer makes repayment on "31 January 2026" with 500.0 transaction amount on Working Capital loan
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 500.00           | 0.00              | null       | false  |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 500.00           | 500.00            | null       | null   |

  @TestRailId:C76614
  Scenario: Verify that breach evaluation matches CSV example with 90 DAY frequency and partial payment
    When Admin sets the business date to "01 January 2019"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays | breachGraceDays |
      | 90              | DAYS                | PERCENTAGE                  | 9            | 3                    | 3               |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2019 | 01 January 2019          | 9000            | 100000       | 18                | 1000     |
    And Admin successfully approves the working capital loan on "01 January 2019" with "9000" amount and expected disbursement date on "01 January 2019"
    When Admin successfully disburse the Working Capital loan on "01 January 2019" with "9000" EUR transaction amount and "1000" discount amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Period 1: fromDate=Jan04 (disburse+3 breach grace days), 90 days, min=(9000+1000)*9%=900
    # CSV reference: row 29 — Start=1/4/2019, 90 days, Min=900 (CSV end 4/4/2019 is exclusive; schedule stores inclusive toDate 4/3/2019)
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2019-01-04 | 2019-04-03 | 90           | 900.00           | 900.00            | null       | null   |
    # Payment of 250 on Jan 5 (within period 1)
    When Admin sets the business date to "05 January 2019"
    And Customer makes repayment on "05 January 2019" with 250.0 transaction amount on Working Capital loan
    # Outstanding should decrease mid-period
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2019-01-04 | 2019-04-03 | 90           | 900.00           | 650.00            | null       | null   |
    # Advance past period 1 end: 250 < 900 -> breach=true
    # CSV reference: row 30 — Period 2 Start=4/4/2019, 90 days (inclusive toDate 7/2/2019)
    When Admin sets the business date to "05 April 2019"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2019-01-04 | 2019-04-03 | 90           | 900.00           | 650.00            | null       | true   |
      | 2            | 2019-04-04 | 2019-07-02 | 90           | 900.00           | 900.00            | null       | null   |

  @TestRailId:C76615
  Scenario: Verify that paidAmount and outstandingAmount update immediately on payment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | delinquencyGraceDays |
      | 1               | MONTHS              | FLAT                        | 1000         |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 1000.00          | 1000.00           | null       | null   |
    # First partial payment
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 400.0 transaction amount on Working Capital loan
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 1000.00          | 600.00            | null       | null   |
    # Second partial payment clears the rest — paidAmount >= minPayment, breach resolves to false immediately
    When Admin sets the business date to "20 January 2026"
    And Customer makes repayment on "20 January 2026" with 600.0 transaction amount on Working Capital loan
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
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Pay 800 in period 1 (300 above minPayment=500)
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 800.0 transaction amount on Working Capital loan
    # Period 2 still starts with full outstanding=500, unaffected by period 1 overpayment
    When Admin sets the business date to "01 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-01-31 | 31           | 500.00           | 0.00              | null       | false  |
      | 2            | 2026-02-01 | 2026-02-28 | 28           | 500.00           | 500.00            | null       | true   |
      | 3            | 2026-03-01 | 2026-03-31 | 31           | 500.00           | 500.00            | null       | null   |
