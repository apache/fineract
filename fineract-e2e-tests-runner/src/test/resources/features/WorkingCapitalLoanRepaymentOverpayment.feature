@WorkingCapital
@WorkingCapitalLoanRepaymentOverpaymentFeature
Feature: Working Capital Loan Repayment - Overpayment

  @TestRailId:TODO_ADD_3
  Scenario: Verify fully repaid Working Capital loan accepts repayment and its status goes to overpaid
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |

    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "20 January 2026" with 9000.0 transaction amount on Working Capital loan

    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Closed (obligations met) | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 9000.0                | 0.0               |

    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

    When Admin sets the business date to "21 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "21 January 2026" with 199.0 transaction amount on Working Capital loan

    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Overpaid | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 9000.0                | 199.0               |
    Then Working Capital loan status will be "OVERPAID"


  @TestRailId:TODO_ADD_4
  Scenario: Verify overpaid Working Capital loan accepts repayment and its status is overpaid
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |

    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "20 January 2026" with 9200.0 transaction amount on Working Capital loan

    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Overpaid | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 9000.0                | 200.0               |

    Then Working Capital loan status will be "OVERPAID"

    When Admin sets the business date to "21 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "21 January 2026" with 199.0 transaction amount on Working Capital loan

    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Overpaid | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 9000.0                | 399.0               |

    Then Working Capital loan status will be "OVERPAID"
