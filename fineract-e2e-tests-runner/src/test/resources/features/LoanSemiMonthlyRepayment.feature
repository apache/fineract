@LoanSemiMonthlyRepaymentFeature
Feature: Loan semi-monthly repayment frequency

  # A cumulative equal-installment loan with daily interest derives its EMI from a nominal period of a 24th of the
  # year (1000 at 24% over 24 repayments: 47.07), whatever the configured due days. Each period is still charged
  # the interest of its actual days, so only the last installment absorbs the difference.
  @TestRailId:CTBD
  Scenario Outline: Cumulative semi-monthly loan with daily interest has equal installments for due days <firstDay> and <secondDay>
    When Admin sets the business date to "<disbursementDate>"
    When Admin creates a client with random data
    When Admin creates a new cumulative semi-monthly Loan Product with due days <firstDay> and <secondDay>, 24.0% yearly interest calculated daily and up to 24 repayments
    When Admin creates a semi-monthly Loan of the created product submitted on "<disbursementDate>" with principal "1000", 24.0% interest and 24 repayments
    And Admin successfully approves the loan on "<disbursementDate>" with "1000" amount and expected disbursement date on "<disbursementDate>"
    And Admin successfully disburse the loan on "<disbursementDate>" with "1000" EUR transaction amount
    Then The semi-monthly Loan has 24 installments, the first due on "<firstDueDate>" and the last due on "<lastDueDate>"
    Then Every installment of the semi-monthly Loan except the last one has a total due of "47.07", and the last one differs from it by at most "1.00"

    Examples:
      | firstDay | secondDay | disbursementDate | firstDueDate    | lastDueDate     |
      | 10       | 25        | 10 January 2023  | 25 January 2023 | 10 January 2024 |
      | 2        | 17        | 02 January 2023  | 17 January 2023 | 02 January 2024 |
      | 15       | 31        | 15 January 2023  | 31 January 2023 | 15 January 2024 |

  @TestRailId:CTBD
  Scenario: Loan product template returns the semi-monthly due days
    When Admin sets the business date to "01 January 2023"
    When Admin creates a new cumulative semi-monthly Loan Product with due days 1 and 25, 24.0% yearly interest calculated daily and up to 24 repayments
    Then The created Loan Product template has semi-monthly due days 1 and 25

  @TestRailId:CTBD
  Scenario: Switching a semi-monthly loan product to monthly drops the due days
    When Admin sets the business date to "01 January 2023"
    When Admin creates a new cumulative semi-monthly Loan Product with due days 10 and 25, 24.0% yearly interest calculated daily and up to 24 repayments
    When Admin changes the repayment frequency of the created Loan Product to "MONTHS"
    Then The created Loan Product has repayment frequency "MONTHS" and no semi-monthly due days

  # Re-aging derives each due date from a period length, so it only accepts DAYS, WEEKS, MONTHS and YEARS. The request
  # is rejected as a bad request before any business rule is checked.
  @TestRailId:CTBD
  Scenario: Re-aging and its preview reject the semi-monthly frequency
    When Admin sets the business date to "02 January 2023"
    When Admin creates a client with random data
    When Admin creates a new cumulative semi-monthly Loan Product with due days 15 and 31, 24.0% yearly interest calculated daily and up to 24 repayments
    When Admin creates a semi-monthly Loan of the created product submitted on "02 January 2023" with principal "1000", 24.0% interest and 4 repayments
    Then Admin fails to re-age the Loan with frequency type "SEMI_MONTHLY" starting on "15 March 2023", with status code 400 and error "validation.msg.loan.reAge.frequencyType.is.not.one.of.expected.enumerations"
    Then Admin fails to preview the re-aging of the Loan with frequency type "SEMI_MONTHLY" starting on "15 March 2023", with status code 400 and error "validation.msg.loan.reAge.frequencyType.is.not.one.of.expected.enumerations"
