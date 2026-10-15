@PayoffSchedulePreview
Feature: Payoff Schedule Preview

  # TODO: add TestRail ID
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

  # TODO: add TestRail ID
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

  # TODO: add TestRail ID
  Scenario: Computes a single installment when the term equals the frequency
    When Admin computes a payoff schedule preview with the following parameters:
      | amount | term | frequency | annualNominalInterestRate | currencyCode | startDate  | dateFormat | locale |
      | 100    | P1M  | P1M       | 0                         | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Payoff schedule preview response has the following summary:
      | numberOfInstallments | totalPrincipal | totalInterest |
      | 1                    | 100.00         | 0.00          |

  # TODO: add TestRail ID
  Scenario: Computes a schedule using a fixed payment amount instead of a term
    When Admin computes a payoff schedule preview with the following parameters:
      | amount | fixedPaymentAmount | frequency | annualNominalInterestRate | interestMethod    | currencyCode | decimalPlaces | startDate  | dateFormat | locale |
      | 100    | 20                 | P1M       | 0                         | DECLINING_BALANCE | USD          | 2             | 2024-01-01 | yyyy-MM-dd | en     |
    Then Payoff schedule preview response has the following summary:
      | currencyCode | amount | interestMethod    | term | frequency | startDate  | numberOfInstallments | totalPrincipal | totalInterest |
      | USD          | 100    | DECLINING_BALANCE | null | P1M       | 2024-01-01 | 5                    | 100.00         | 0.00          |

  # TODO: add TestRail ID
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

  # TODO: add TestRail ID
  Scenario: Rejects supplying both term and fixedPaymentAmount
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | term | fixedPaymentAmount | frequency | currencyCode | startDate  | dateFormat | locale |
      | 100    | P6M  | 20                 | P1M       | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName      | code                                                                                          |
      | fixedPaymentAmount | validation.msg.payoffSchedulePreview.fixedPaymentAmount.cannot.also.be.provided.when.term.is.populated |

  # TODO: add TestRail ID
  Scenario: Rejects supplying neither term nor fixedPaymentAmount
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | frequency | currencyCode | startDate  | dateFormat | locale |
      | 100    | P1M       | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName | code                                                                    |
      | term          | validation.msg.payoffSchedulePreview.term.or.fixedPaymentAmount.required |

  # TODO: add TestRail ID
  Scenario: Rejects a term in a different unit than the frequency
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | term | frequency | currencyCode | startDate  | dateFormat | locale |
      | 1000   | P6M  | P10D      | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName | code                                                                   |
      | term          | validation.msg.payoffSchedulePreview.term.not.the.same.unit.as.frequency |

  # TODO: add TestRail ID
  Scenario: Rejects a term that is not an exact multiple of the frequency
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | term | frequency | currencyCode | startDate  | dateFormat | locale |
      | 100    | P5M  | P2M       | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName | code                                                                |
      | term          | validation.msg.payoffSchedulePreview.term.not.a.multiple.of.frequency |

  # TODO: add TestRail ID
  Scenario: Rejects zero-length and year-based periods
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | term | frequency | currencyCode | startDate  | dateFormat | locale |
      | 100    | P1Y  | P0D       | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName | code                                                            |
      | term          | validation.msg.payoffSchedulePreview.term.does.not.match.regexp      |
      | frequency     | validation.msg.payoffSchedulePreview.frequency.does.not.match.regexp |

  # TODO: add TestRail ID
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

  # TODO: add TestRail ID
  Scenario: Rejects a start date without a locale
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | term | frequency | currencyCode | startDate  | dateFormat |
      | 100    | P6M  | P1M       | USD          | 2024-01-01 | yyyy-MM-dd |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName | code                                   |
      | startDate     | validation.msg.missing.locale.parameter |

  # TODO: add TestRail ID
  Scenario: Rejects an amount with more decimal places than the currency
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | term | frequency | currencyCode | startDate  | dateFormat | locale |
      | 0.004  | P3M  | P1M       | USD          | 2024-01-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName | code                                                               |
      | amount        | validation.msg.payoffSchedulePreview.amount.scale.is.greater.than.2 |

  # TODO: add TestRail ID
  Scenario: Rejects a fixed payment that does not cover a later period's interest
    When Admin attempts to compute a payoff schedule preview with the following parameters:
      | amount | fixedPaymentAmount | frequency | annualNominalInterestRate | currencyCode | startDate  | dateFormat | locale |
      | 1000   | 10.10              | P1M       | 12                        | USD          | 2024-02-01 | yyyy-MM-dd | en     |
    Then Computing the payoff schedule preview fails with the following validation errors:
      | parameterName      | code                                                                     |
      | fixedPaymentAmount | validation.msg.payoffSchedulePreview.fixedPaymentAmount.does.not.cover.interest |

  # TODO: add TestRail ID
  @PayoffSchedulePreviewMaxPeriodsScenario
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
