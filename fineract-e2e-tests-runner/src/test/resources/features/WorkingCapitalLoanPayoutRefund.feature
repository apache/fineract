@WorkingCapital
@WorkingCapitalLoanPayoutRefundFeature
Feature: Working Capital Loan Payout Refund

  @TestRailId:TODO_ADD_1
  Scenario: Verify working capital loan Payout Refund transaction - UC1: simple Payout Refund transaction then undo
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
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes "PAYOUT_REFUND" transaction on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund | 270.0             | 270.0            | 0.0               | 0.0                   | false    |

    When Customer undo "1"th "PAYOUT_REFUND" transaction made on "10 January 2026" on Working Capital loan
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund | 270.0             | 270.0            | 0.0               | 0.0                   | true     |

  @TestRailId:TODO_ADD_2
  Scenario: Verify working capital loan Payout Refund transaction - UC2: Payout Refund transaction with charges then undo
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
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "10 January 2026" due date and 25.0 transaction amount
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes "PAYOUT_REFUND" transaction on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 230.0      | 40.0              | null                  | null             | null           |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund | 270.0             | 230.0            | 15.0              | 25.0                  | false    |

    When Customer undo "1"th "PAYOUT_REFUND" transaction made on "10 January 2026" on Working Capital loan
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund | 270.0             | 230.0            | 15.0              | 25.0                  | true     |


  @TestRailId:TODO_ADD_3
  Scenario: Verify working capital loan Payout Refund transaction - UC3: overpay with Payout Refund transaction with charges then undo and verify Journal Entries
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name        | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | WCLP_ACC_DEF_REV_AM | 2026-01-01      | 2026-01-01               | Approved | 9000.0            | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "10 January 2026" due date and 25.0 transaction amount
    And Working capital loan account has the correct data:
      | product.name        | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes "PAYOUT_REFUND" transaction on "10 January 2026" with 9140.0 transaction amount on Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund | 9140.0            | 9000.0           | 15.0              | 25.0                  | false    |
    Then Working Capital loan status will be "OVERPAID"
    Then Working Capital Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 9140.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 9000.0 |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 15.0   |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 25.0   |
      | LIABILITY | 245000       | Other Credit Liability    |        | 100.0  |

    When Customer undo "1"th "PAYOUT_REFUND" transaction made on "10 January 2026" on Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Payout Refund | 9140.0            | 9000.0           | 15.0              | 25.0                  | true     |
    Then Working Capital loan status will be "ACTIVE"

    Then Working Capital Loan Transactions tab has a reversed "PAYOUT_REFUND" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 9140.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 9000.0 |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 15.0   |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 25.0   |
      | LIABILITY | 245000       | Other Credit Liability    |        | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 9140.0 |
      | ASSET     | 112601       | Loans Receivable          | 9000.0 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 15.0   |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 25.0   |        |
      | LIABILITY | 245000       | Other Credit Liability    | 100.0  |        |

  @TestRailId:TODO_ADD_4
  Scenario: Verify working capital loan Payout Refund transaction - UC4: Payout Refund + repayments with charges then undo Payout Refound
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
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "05 January 2026" due date and 25.0 transaction amount
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |
    When Admin sets the business date to "5 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes "PAYOUT_REFUND" transaction on "5 January 2026" with 123.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | product.name | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 83.0               | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Payout Refund | 123.0             | 83.0             | 15.0              | 25.0                  | false    |

    When Admin sets the business date to "7 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes "REPAYMENT" transaction on "7 January 2026" with 73.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | product.name | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 156.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Payout Refund | 123.0             | 83.0             | 15.0              | 25.0                  | false    |
      | 07 January 2026 | Repayment     | 73.0              | 73.0             | 0.0               | 0.0                   | false    |

    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes "REPAYMENT" transaction on "10 January 2026" with 89.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | product.name | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 245.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Payout Refund | 123.0             | 83.0             | 15.0              | 25.0                  | false    |
      | 07 January 2026 | Repayment     | 73.0              | 73.0             | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment     | 89.0              | 89.0             | 0.0               | 0.0                   | false    |

    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Customer undo "1"th "PAYOUT_REFUND" transaction made on "05 January 2026" on Working Capital loan
    And Working capital loan account has the correct data:
      | product.name | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 122.0              | 0.0               |
    # verify that the charges allocation is correct
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Payout Refund | 123.0             | 83.0             | 15.0              | 25.0                  | true     |
      | 07 January 2026 | Repayment     | 73.0              | 33.0             | 15.0              | 25.0                  | false    |
      | 10 January 2026 | Repayment     | 89.0              | 89.0             | 0.0               | 0.0                   | false    |
    Then Working Capital loan status will be "ACTIVE"

  @TestRailId:TODO_ADD_5
  Scenario: Verify working capital loan Payout Refund transaction - UC4: Backdated Payout Refund + repayments with charges then undo Payout Refound
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
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "05 January 2026" due date and 25.0 transaction amount
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     |

    When Admin sets the business date to "7 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes "REPAYMENT" transaction on "7 January 2026" with 73.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | product.name | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 33.0               | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 07 January 2026 | Repayment    | 73.0              | 33.0             | 15.0              | 25.0                  | false    |

    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes "REPAYMENT" transaction on "10 January 2026" with 89.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | product.name | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 122.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 07 January 2026 | Repayment    | 73.0              | 33.0             | 15.0              | 25.0                  | false    |
      | 10 January 2026 | Repayment    | 89.0              | 89.0             | 0.0               | 0.0                   | false    |

    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes "PAYOUT_REFUND" transaction on "5 January 2026" with 123.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | product.name | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 245.0              | 0.0               |
    # Verify allocation is updated
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Payout Refund | 123.0             | 83.0             | 15.0              | 25.0                  | false    |
      | 07 January 2026 | Repayment     | 73.0              | 73.0             | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment     | 89.0              | 89.0             | 0.0               | 0.0                   | false    |

    When Admin sets the business date to "13 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Customer undo "1"th "PAYOUT_REFUND" transaction made on "05 January 2026" on Working Capital loan
    And Working capital loan account has the correct data:
      | product.name | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | totalPaidPrincipal | overpaymentAmount |
      | WCLP         | 9000.0    | 9000.0            | 100000.0           | 18.0              | null     | 122.0              | 0.0               |
    # verify that the charges allocation is correct
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Payout Refund | 123.0             | 83.0             | 15.0              | 25.0                  | true     |
      | 07 January 2026 | Repayment     | 73.0              | 33.0             | 15.0              | 25.0                  | false    |
      | 10 January 2026 | Repayment     | 89.0              | 89.0             | 0.0               | 0.0                   | false    |
    Then Working Capital loan status will be "ACTIVE"