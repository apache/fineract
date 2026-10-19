@PayoffSchedulePreview
Feature: Payoff Schedule Preview

  @TestRailId:C115304
  Scenario: Computes a term schedule with actual day counts
    When Admin computes a payoff schedule preview with the following parameters:
      | amount | term | frequency | annualNominalInterestRate | interestMethod    | currencyCode | decimalPlaces | startDate  | dateFormat | locale |
      | 100    | P6M  | P1M       | 7.0                       | DECLINING_BALANCE | USD          | 2             | 2024-01-01 | yyyy-MM-dd | en     |
    Then Payoff schedule preview response has the following summary:
      | currencyCode | amount | annualNominalInterestRate | interestMethod    | term | frequency | startDate  | numberOfInstallments | totalPrincipal | totalInterest | totalRepayment |
      | USD          | 100    | 7.0                       | DECLINING_BALANCE | P6M  | P1M       | 2024-01-01 | 6                    | 100.00         | 2.04          | 102.04         |
    And Payoff schedule preview installment 1 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-01-01 | 2024-02-01 | 16.42        | 0.59        | 17.01    | 83.58                       |
    And Payoff schedule preview installment 6 has the following data:
      | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 16.89        | 0.10        | 16.99    | 0                           |

  @TestRailId:C115305
  Scenario: Computes the same term schedule as the regular progressive schedule generator on 360/30 day counts
    When Admin computes a payoff schedule preview with the following parameters:
      | amount | term | frequency | annualNominalInterestRate | currencyCode | decimalPlaces | startDate  | dateFormat | locale | daysInYearType | daysInMonthType |
      | 100    | P6M  | P1M       | 7.0                       | USD          | 2             | 2024-01-01 | yyyy-MM-dd | en     | 360            | 30              |
    Then Payoff schedule preview response has the following summary:
      | numberOfInstallments | totalPrincipal | totalInterest | totalRepayment |
      | 6                    | 100.00         | 2.05          | 102.05         |
    And Payoff schedule preview installment 1 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-01-01 | 2024-02-01 | 16.43        | 0.58        | 17.01    | 83.57                       |
    And Payoff schedule preview installment 6 has the following data:
      | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 16.90        | 0.10        | 17.00    | 0                           |

  @TestRailId:C115306
  Scenario: Computes a single installment when the term equals the frequency
    When Admin computes a payoff schedule preview with the following parameters:
      | amount | term | frequency | annualNominalInterestRate | currencyCode | startDate  | dateFormat | locale |
      | 100    | P1M  | P1M       | 0                         | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Payoff schedule preview response has the following summary:
      | numberOfInstallments | totalPrincipal | totalInterest |
      | 1                    | 100.00         | 0.00          |

  @TestRailId:C115307
  Scenario: Computes a schedule using a fixed payment amount instead of a term
    When Admin computes a payoff schedule preview with the following parameters:
      | amount | fixedPaymentAmount | frequency | annualNominalInterestRate | interestMethod    | currencyCode | decimalPlaces | startDate  | dateFormat | locale |
      | 100    | 20                 | P1M       | 0                         | DECLINING_BALANCE | USD          | 2             | 2024-01-01 | yyyy-MM-dd | en     |
    Then Payoff schedule preview response has the following summary:
      | currencyCode | amount | interestMethod    | term | frequency | startDate  | numberOfInstallments | totalPrincipal | totalInterest |
      | USD          | 100    | DECLINING_BALANCE | null | P1M       | 2024-01-01 | 5                    | 100.00         | 0.00          |

  @TestRailId:C115308
  Scenario: Computes a fixed payment schedule with interest
    When Admin computes a payoff schedule preview with the following parameters:
      | amount | fixedPaymentAmount | frequency | annualNominalInterestRate | currencyCode | startDate       | dateFormat   | locale | daysInYearType | daysInMonthType |
      | 1000   | 200                | P1M       | 12                        | USD          | 01 January 2024 | dd MMMM yyyy | en     | 360            | 30              |
    Then Payoff schedule preview response has the following summary:
      | numberOfInstallments | totalPrincipal | totalInterest | totalRepayment |
      | 6                    | 1000.00        | 31.12         | 1031.12        |
    And Payoff schedule preview installment 1 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-01-01 | 2024-02-01 | 190.00       | 10.00       | 200.00   | 810.00                      |
    And Payoff schedule preview installment 6 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-06-01 | 2024-07-01 | 30.81        | 0.31        | 31.12    | 0                           |

  @TestRailId:C115309
  Scenario: Rejects supplying both term and fixedPaymentAmount
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | term | fixedPaymentAmount | frequency | currencyCode | startDate  | dateFormat | locale |
      | 100    | P6M  | 20                 | P1M       | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName      | code                                                                                          |
      | fixedPaymentAmount | validation.msg.payoffSchedulePreview.fixedPaymentAmount.cannot.also.be.provided.when.term.is.populated |

  @TestRailId:C115310
  Scenario: Rejects supplying neither term nor fixedPaymentAmount
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | frequency | currencyCode | startDate  | dateFormat | locale |
      | 100    | P1M       | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName | code                                                                    |
      | term          | validation.msg.payoffSchedulePreview.term.or.fixedPaymentAmount.required |

  @TestRailId:C115311
  Scenario: Rejects a term in a different unit than the frequency
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | term | frequency | currencyCode | startDate  | dateFormat | locale |
      | 1000   | P6M  | P10D      | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName | code                                                                   |
      | term          | validation.msg.payoffSchedulePreview.term.not.the.same.unit.as.frequency |

  @TestRailId:C115312
  Scenario: Rejects a term that is not an exact multiple of the frequency
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | term | frequency | currencyCode | startDate  | dateFormat | locale |
      | 100    | P5M  | P2M       | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName | code                                                                |
      | term          | validation.msg.payoffSchedulePreview.term.not.a.multiple.of.frequency |

  @TestRailId:C115313
  Scenario: Rejects zero-length and year-based periods
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | term | frequency | currencyCode | startDate  | dateFormat | locale |
      | 100    | P1Y  | P0D       | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName | code                                                            |
      | term          | validation.msg.payoffSchedulePreview.term.does.not.match.regexp      |
      | frequency     | validation.msg.payoffSchedulePreview.frequency.does.not.match.regexp |

  @TestRailId:C115314
  Scenario: Reports all validation errors together
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | term | frequency | interestMethod | currencyCode | decimalPlaces |
      | 0      | P6M  | P1M       | FLAT           | USD          | 9             |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName  | code                                                                             |
      | amount         | validation.msg.payoffSchedulePreview.amount.not.greater.than.zero                 |
      | interestMethod | validation.msg.payoffSchedulePreview.interestMethod.is.not.one.of.expected.enumerations |
      | decimalPlaces  | validation.msg.payoffSchedulePreview.decimalPlaces.is.not.within.expected.range   |
      | startDate      | validation.msg.payoffSchedulePreview.startDate.cannot.be.blank                    |

  @TestRailId:C115315
  Scenario: Rejects a start date without a locale
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | term | frequency | currencyCode | startDate  | dateFormat |
      | 100    | P6M  | P1M       | USD          | 2024-01-01 | yyyy-MM-dd |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName | code                                   |
      | startDate     | validation.msg.missing.locale.parameter |

  @TestRailId:C115316
  Scenario: Rejects an amount with more decimal places than the currency
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | term | frequency | currencyCode | startDate  | dateFormat | locale |
      | 0.004  | P3M  | P1M       | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName | code                                                               |
      | amount        | validation.msg.payoffSchedulePreview.amount.scale.is.greater.than.2 |

  @TestRailId:C115317
  Scenario: Rejects a fixed payment that does not cover a later period's interest
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | fixedPaymentAmount | frequency | annualNominalInterestRate | currencyCode | startDate  | dateFormat | locale |
      | 1000   | 10.10              | P1M       | 12                        | USD          | 2024-02-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName      | code                                                                     |
      | fixedPaymentAmount | validation.msg.payoffSchedulePreview.fixedPaymentAmount.does.not.cover.interest |

  @PayoffSchedulePreviewMaxPeriodsScenario
  @TestRailId:C115318
  Scenario: Rejects schedules longer than the configured maximums
    When Global config "payoff-schedule-preview-max-payoff-periods" value set to "5" through DefaultApi
    And Global config "payoff-schedule-preview-max-term-periods" value set to "5" through DefaultApi
    And Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | fixedPaymentAmount | frequency | currencyCode | startDate  | dateFormat | locale |
      | 100    | 10                 | P1M       | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName      | code                                                                       |
      | fixedPaymentAmount | validation.msg.payoffSchedulePreview.fixedPaymentAmount.exceeds.max.payoff.periods |
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | term | frequency | currencyCode | startDate  | dateFormat | locale |
      | 100    | P6M  | P1M       | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName | code                                                          |
      | term          | validation.msg.payoffSchedulePreview.term.exceeds.max.term.periods |

  @TestRailId:C115319
  Scenario: Preview term schedule matches the repayment schedule of a real progressive loan with the same parameters on actual day counts
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.58           | 16.42         | 0.59     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.03           | 16.55         | 0.46     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.42           | 16.61         | 0.4      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.7            | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.89           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.89         | 0.1      | 0.0  | 0.0       | 16.99 | 0.0  | 0.0        | 0.0  | 16.99       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 0.0  | 0.0       | 102.04 | 0.0  | 0.0        | 0.0  | 102.04      |
    When Admin computes a payoff schedule preview with the following parameters:
      | amount | term | frequency | annualNominalInterestRate | currencyCode | startDate  | dateFormat | locale |
      | 100    | P6M  | P1M       | 7                         | EUR          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Payoff schedule preview response has the following summary:
      | currencyCode | numberOfInstallments | totalPrincipal | totalInterest | totalRepayment |
      | EUR          | 6                    | 100.00         | 2.04          | 102.04         |
    And Payoff schedule preview installment 1 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-01-01 | 2024-02-01 | 16.42        | 0.59        | 17.01    | 83.58                       |
    And Payoff schedule preview installment 2 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-02-01 | 2024-03-01 | 16.55        | 0.46        | 17.01    | 67.03                       |
    And Payoff schedule preview installment 6 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-06-01 | 2024-07-01 | 16.89        | 0.10        | 16.99    | 0                           |

  @TestRailId:C115320
  Scenario: Computes a bi-weekly term schedule across a year boundary on a 365-day year
    When Admin computes a payoff schedule preview with the following parameters:
      | amount | term | frequency | annualNominalInterestRate | currencyCode | startDate  | dateFormat | locale | daysInYearType |
      | 1000   | P4W  | P2W       | 10                        | USD          | 2023-12-20 | yyyy-MM-dd | en     | 365            |
    Then Payoff schedule preview response has the following summary:
      | term | frequency | numberOfInstallments | totalPrincipal | totalInterest | totalRepayment |
      | P4W  | P2W       | 2                    | 1000.00        | 5.76          | 1005.76        |
    And Payoff schedule preview installment 1 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2023-12-20 | 2024-01-03 | 499.04       | 3.84        | 502.88   | 500.96                      |
    And Payoff schedule preview installment 2 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-01-03 | 2024-01-17 | 500.96       | 1.92        | 502.88   | 0                           |

  @TestRailId:C115321
  Scenario: Computes a fixed payment schedule with a daily frequency on a 365-day year
    When Admin computes a payoff schedule preview with the following parameters:
      | amount | fixedPaymentAmount | frequency | annualNominalInterestRate | currencyCode | startDate  | dateFormat | locale | daysInYearType |
      | 1000   | 120                | P10D      | 9.5                       | USD          | 2023-12-25 | yyyy-MM-dd | en     | 365            |
    Then Payoff schedule preview response has the following summary:
      | term | frequency | numberOfInstallments | totalPrincipal | totalInterest | totalRepayment |
      | null | P10D      | 9                    | 1000.00        | 12.37         | 1012.37        |
    And Payoff schedule preview installment 1 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2023-12-25 | 2024-01-04 | 117.40       | 2.60        | 120.00   | 882.60                      |
    And Payoff schedule preview installment 8 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-03-04 | 2024-03-14 | 119.55       | 0.45        | 120.00   | 52.23                       |
    And Payoff schedule preview installment 9 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-03-14 | 2024-03-24 | 52.23        | 0.14        | 52.37    | 0                           |

  @TestRailId:C115322
  Scenario: Keeps month-end due dates for a month-end start date with interest
    When Admin computes a payoff schedule preview with the following parameters:
      | amount | term | frequency | annualNominalInterestRate | currencyCode | startDate  | dateFormat | locale |
      | 1000   | P3M  | P1M       | 12                        | USD          | 2024-01-31 | yyyy-MM-dd | en     |
    Then Payoff schedule preview response has the following summary:
      | numberOfInstallments | totalPrincipal | totalInterest | totalRepayment |
      | 3                    | 1000.00        | 19.63         | 1019.63        |
    And Payoff schedule preview installment 1 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-01-31 | 2024-02-29 | 330.37       | 9.51        | 339.88   | 669.63                      |
    And Payoff schedule preview installment 2 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-02-29 | 2024-03-31 | 333.07       | 6.81        | 339.88   | 336.56                      |
    And Payoff schedule preview installment 3 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-03-31 | 2024-04-30 | 336.56       | 3.31        | 339.87   | 0                           |

  @TestRailId:C115323
  Scenario: Computes a weekly term schedule on a 364-day year
    When Admin computes a payoff schedule preview with the following parameters:
      | amount | term | frequency | annualNominalInterestRate | currencyCode | startDate  | dateFormat | locale | daysInYearType |
      | 1000   | P4W  | P1W       | 36                        | USD          | 2024-03-01 | yyyy-MM-dd | en     | 364            |
    Then Payoff schedule preview response has the following summary:
      | numberOfInstallments | totalPrincipal | totalInterest | totalRepayment |
      | 4                    | 1000.00        | 17.37         | 1017.37        |
    And Payoff schedule preview installment 1 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-03-01 | 2024-03-08 | 247.42       | 6.92        | 254.34   | 752.58                      |
    And Payoff schedule preview installment 4 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-03-22 | 2024-03-29 | 252.60       | 1.75        | 254.35   | 0                           |

  @TestRailId:C115324
  Scenario: Rounds the whole schedule to whole units when decimalPlaces is 0
    When Admin computes a payoff schedule preview with the following parameters:
      | amount | term | frequency | annualNominalInterestRate | currencyCode | decimalPlaces | startDate  | dateFormat | locale |
      | 100    | P6M  | P1M       | 7                         | USD          | 0             | 2024-01-01 | yyyy-MM-dd | en     |
    Then Payoff schedule preview response has the following summary:
      | numberOfInstallments | totalPrincipal | totalInterest | totalRepayment |
      | 6                    | 100            | 1             | 101            |
    And Payoff schedule preview installment 1 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-01-01 | 2024-02-01 | 16           | 1           | 17       | 84                          |
    And Payoff schedule preview installment 2 has the following data:
      | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 17           | 0           | 17       | 67                          |
    And Payoff schedule preview installment 6 has the following data:
      | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 16           | 0           | 16       | 0                           |

  @TestRailId:C115325
  Scenario: Rejects an unknown or lowercase currency code
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | term | frequency | currencyCode | startDate  | dateFormat | locale |
      | 100    | P6M  | P1M       | XYZ          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName | code                                                           |
      | currencyCode  | validation.msg.payoffSchedulePreview.currencyCode.does.not.exist |
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | term | frequency | currencyCode | startDate  | dateFormat | locale |
      | 100    | P6M  | P1M       | usd          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName | code                                                           |
      | currencyCode  | validation.msg.payoffSchedulePreview.currencyCode.does.not.exist |

  @TestRailId:C115326
  Scenario: Requires a currency code when the organisation has more than one currency
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | term | frequency | startDate  | dateFormat | locale |
      | 100    | P6M  | P1M       | 2024-01-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName | code                                                           |
      | currencyCode  | validation.msg.payoffSchedulePreview.currencyCode.cannot.be.blank |

  @TestRailId:C115327
  Scenario: Rejects a fixed payment amount with more decimal places than the requested decimalPlaces
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | fixedPaymentAmount | frequency | decimalPlaces | currencyCode | startDate  | dateFormat | locale |
      | 100    | 10.5               | P1M       | 0             | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName      | code                                                                       |
      | fixedPaymentAmount | validation.msg.payoffSchedulePreview.fixedPaymentAmount.scale.is.greater.than.0 |

  @PayoffSchedulePreviewMaxPeriodsScenario
  @TestRailId:C115328
  Scenario: Accepts schedules with exactly the configured maximum number of installments
    When Global config "payoff-schedule-preview-max-payoff-periods" value set to "5" through DefaultApi
    And Global config "payoff-schedule-preview-max-term-periods" value set to "5" through DefaultApi
    And Admin computes a payoff schedule preview with the following parameters:
      | amount | term | frequency | currencyCode | startDate  | dateFormat | locale |
      | 100    | P5M  | P1M       | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Payoff schedule preview response has the following summary:
      | numberOfInstallments | totalPrincipal |
      | 5                    | 100.00         |
    When Admin computes a payoff schedule preview with the following parameters:
      | amount | fixedPaymentAmount | frequency | currencyCode | startDate  | dateFormat | locale |
      | 100    | 20                 | P1M       | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Payoff schedule preview response has the following summary:
      | numberOfInstallments | totalPrincipal |
      | 5                    | 100.00         |

  @TestRailId:C115329
  Scenario: Rejects a user without the READ_LOAN permission
    When Admin creates new user with "PAYOFF_PREVIEW_NO_LOAN" username, "PAYOFF_PREVIEW_NO_LOAN_ROLE" role name and given permissions:
      | READ_CLIENT |
    And Created user attempts to compute a payoff schedule preview with the following parameters:
      | amount | term | frequency | currencyCode | startDate  | dateFormat | locale |
      | 100    | P6M  | P1M       | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview is rejected with HTTP 403 and error code "error.msg.not.authorized"

  @TestRailId:C115330
  Scenario: Allows a user with only the READ_LOAN permission
    When Admin creates new user with "PAYOFF_PREVIEW_READ_LOAN" username, "PAYOFF_PREVIEW_READ_LOAN_ROLE" role name and given permissions:
      | READ_LOAN |
    And Created user computes a payoff schedule preview with the following parameters:
      | amount | term | frequency | currencyCode | startDate  | dateFormat | locale |
      | 100    | P6M  | P1M       | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Payoff schedule preview response has the following summary:
      | numberOfInstallments | totalPrincipal |
      | 6                    | 100.00         |

  @TestRailId:C115331
  Scenario: Computes a term schedule with the default maximum of 1200 installments
    When Admin computes a payoff schedule preview with the following parameters:
      | amount | term   | frequency | annualNominalInterestRate | currencyCode | startDate  | dateFormat | locale |
      | 100000 | P1200M | P1M       | 7                         | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Payoff schedule preview response has the following summary:
      | numberOfInstallments | totalPrincipal |
      | 1200                 | 100000.00      |

  @TestRailId:C115332
  Scenario: Computes the specification's zero-interest term sample with a month-name start date
    When Admin computes a payoff schedule preview with the following parameters:
      | amount | term | frequency | annualNominalInterestRate | interestMethod    | currencyCode | decimalPlaces | startDate       | dateFormat   | locale |
      | 100    | P6M  | P1M       | 0                         | DECLINING_BALANCE | USD          | 2             | 01 January 2024 | dd MMMM yyyy | en     |
    Then Payoff schedule preview response has the following summary:
      | currencyCode | amount | annualNominalInterestRate | interestMethod    | term | frequency | startDate  | numberOfInstallments | totalPrincipal | totalInterest | totalRepayment |
      | USD          | 100    | 0                         | DECLINING_BALANCE | P6M  | P1M       | 2024-01-01 | 6                    | 100.00         | 0.00          | 100.00         |
    And Payoff schedule preview installment 1 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-01-01 | 2024-02-01 | 16.67        | 0.00        | 16.67    | 83.33                       |
    And Payoff schedule preview installment 5 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-05-01 | 2024-06-01 | 16.67        | 0.00        | 16.67    | 16.65                       |
    And Payoff schedule preview installment 6 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-06-01 | 2024-07-01 | 16.65        | 0.00        | 16.65    | 0                           |

  @TestRailId:C115333
  Scenario: Computes the specification's 9.4822 percent sample on 360/30 day counts
    When Admin computes a payoff schedule preview with the following parameters:
      | amount | term | frequency | annualNominalInterestRate | currencyCode | decimalPlaces | startDate  | dateFormat | locale | daysInYearType | daysInMonthType |
      | 100    | P6M  | P1M       | 9.4822                    | USD          | 2             | 2024-01-01 | yyyy-MM-dd | en     | 360            | 30              |
    Then Payoff schedule preview response has the following summary:
      | numberOfInstallments | totalPrincipal | totalInterest | totalRepayment |
      | 6                    | 100.00         | 2.78          | 102.78         |
    And Payoff schedule preview installment 1 has the following data:
      | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 16.34        | 0.79        | 17.13    | 83.66                       |
    And Payoff schedule preview installment 2 has the following data:
      | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 16.47        | 0.66        | 17.13    | 67.19                       |
    And Payoff schedule preview installment 3 has the following data:
      | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 16.60        | 0.53        | 17.13    | 50.59                       |
    And Payoff schedule preview installment 4 has the following data:
      | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 16.73        | 0.40        | 17.13    | 33.86                       |
    And Payoff schedule preview installment 5 has the following data:
      | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 16.86        | 0.27        | 17.13    | 17.00                       |
    And Payoff schedule preview installment 6 has the following data:
      | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 17.00        | 0.13        | 17.13    | 0                           |

  @TestRailId:C115334
  Scenario: Applies the documented defaults for interest rate, interest method and decimal places
    When Admin computes a payoff schedule preview with the following parameters:
      | amount | term | frequency | currencyCode | startDate  | dateFormat | locale |
      | 100    | P6M  | P1M       | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Payoff schedule preview response has the following summary:
      | currencyCode | annualNominalInterestRate | interestMethod    | numberOfInstallments | totalPrincipal | totalInterest | totalRepayment |
      | USD          | 0                         | DECLINING_BALANCE | 6                    | 100.00         | 0.00          | 100.00         |
    And Payoff schedule preview installment 6 has the following data:
      | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 16.65        | 0.00        | 16.65    | 0                           |

  @TestRailId:C115335
  Scenario: Computes the specification's zero-interest weekly sample
    When Admin computes a payoff schedule preview with the following parameters:
      | amount | term | frequency | annualNominalInterestRate | currencyCode | startDate  | dateFormat | locale |
      | 400    | P8W  | P2W       | 0                         | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Payoff schedule preview response has the following summary:
      | term | frequency | numberOfInstallments | totalPrincipal | totalInterest | totalRepayment |
      | P8W  | P2W       | 4                    | 400.00         | 0.00          | 400.00         |
    And Payoff schedule preview installment 1 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-01-01 | 2024-01-15 | 100.00       | 0.00        | 100.00   | 300.00                      |
    And Payoff schedule preview installment 4 has the following data:
      | fromDate   | dueDate    | principalDue | interestDue | totalDue | outstandingPrincipalBalance |
      | 2024-02-12 | 2024-02-26 | 100.00       | 0.00        | 100.00   | 0                           |

  @TestRailId:C115336
  Scenario: Rejects unsupported day count types
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | term | frequency | currencyCode | startDate  | dateFormat | locale | daysInYearType | daysInMonthType |
      | 100    | P6M  | P1M       | USD          | 2024-01-01 | yyyy-MM-dd | en     | 999            | 999             |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName   | code                                                                                     |
      | daysInYearType  | validation.msg.payoffSchedulePreview.daysInYearType.is.not.one.of.expected.enumerations  |
      | daysInMonthType | validation.msg.payoffSchedulePreview.daysInMonthType.is.not.one.of.expected.enumerations |

  @TestRailId:C115337
  Scenario: Rejects a start date that cannot be parsed with the given date format
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | term | frequency | currencyCode | startDate   | dateFormat   | locale |
      | 100    | P6M  | P1M       | USD          | 40 Foo 2024 | dd MMMM yyyy | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName | code                                  |
      | startDate     | validation.msg.invalid.dateFormat.format |
