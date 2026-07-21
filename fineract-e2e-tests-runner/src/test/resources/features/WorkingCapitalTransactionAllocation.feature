@SerialChargeAccrualConfig
@WorkingCapital
@WorkingCapitalTransactionAllocationFeature
Feature: Working Capital Transaction Allocation

  # Pin the charge accrual date to due-date so the scenarios do not depend on the global config value left behind by a
  # previously executed feature. The transaction assertions below expect the charge accrual to be posted by the COB on
  # the charge due date; a leaked submitted-date value would instead accrue at charge-add time and break them.
  Background:
    Given Global config "charge-accrual-date" value set to "due-date"

  @TestRailId:C85412
  Scenario: Verify Working Capital Repayment transaction with fee and penalty added with DUE_FEE_PENALTY_PRINCIPAL allocation - UC1
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct                    | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DUE_FEE_PENALTY_PRINCIPAL | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "12 January 2026" due date and 25.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 12 January 2026 | 15.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 12 January 2026 | 25.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "12 January 2026" with 270.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 230.0              | 100000.0           | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 15.0       | 0.0             | 15.0     | 25.0           | 0.0                 | 25.0         |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Repayment    | 270.0             | 230.0            | 15.0              | 25.0                  | false    |

  @TestRailId:C85413
  Scenario: Verify Working Capital Repayment transaction with fee and penalty added with DUE_PENALTY_FEE_PRINCIPAL allocation - UC2
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct                | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "12 January 2026" due date and 25.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 12 January 2026 | 15.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 12 January 2026 | 25.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "12 January 2026" with 270.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 230.0              | 100000.0           | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 15.0       | 0.0             | 15.0     | 25.0           | 0.0                 | 25.0         |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Repayment    | 270.0             | 230.0            | 15.0              | 25.0                  | false    |

  @TestRailId:C85414
  Scenario: Verify Working Capital Repayment transaction with fee and penalty added with DUE_PRINCIPAL_FEE_PENALTY allocation - UC3
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct                    | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DUE_PRINCIPAL_FEE_PENALTY | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "12 January 2026" due date and 25.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 12 January 2026 | 15.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 12 January 2026 | 25.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "12 January 2026" with 270.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 270.0              | 100000.0           | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 15.0       | 15.0            | 0.0      | 25.0           | 25.0                | 0.0          |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Repayment    | 270.0             | 270.0            | 0.0               | 0.0                   | false    |

  @TestRailId:C85415
  Scenario: Verify Working Capital Repayment transaction with fee and penalty added with DUE_FEE_PRINCIPAL_PENALTY allocation - UC4
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct                    | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DUE_FEE_PRINCIPAL_PENALTY | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "12 January 2026" due date and 25.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 12 January 2026 | 15.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 12 January 2026 | 25.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "12 January 2026" with 270.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 255.0              | 100000.0           | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 15.0       | 0.0             | 15.0     | 25.0           | 25.0                | 0.0          |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Repayment    | 270.0             | 255.0            | 15.0              | 0.0                   | false    |

  @TestRailId:C85416
  Scenario: Verify Working Capital Repayment transaction with fee and penalty added with IN_ADVANCE_PENALTY_FEE_PRINCIPAL allocation - UC5
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct                           | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_IN_ADVANCE_PENALTY_FEE_PRINCIPAL | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "12 January 2026" due date and 25.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 12 January 2026 | 15.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 12 January 2026 | 25.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    And Customer makes repayment on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 230.0              | 100000.0           | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 15.0       | 0.0             | 15.0     | 25.0           | 0.0                 | 25.0         |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 270.0             | 230.0            | 15.0              | 25.0                  | false    |

  @TestRailId:C85417
  Scenario: Verify Working Capital Repayment transaction that closes loan with fee and penalty added with DUE_PRINCIPAL_FEE_PENALTY allocation - UC6
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct                    | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DUE_PRINCIPAL_FEE_PENALTY | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "12 January 2026" due date and 25.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 12 January 2026 | 15.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 12 January 2026 | 25.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "12 January 2026" with 9040.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 100000.0           | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 15.0       | 0.0             | 15.0     | 25.0           | 0.0                 | 25.0         |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Repayment    | 9040.0            | 9000.0           | 15.0              | 25.0                  | false    |
      | 12 January 2026 | Accrual      | 15.0              | 0.0              | 15.0              | 0.0                   | false    |
      | 12 January 2026 | Accrual      | 25.0              | 0.0              | 0.0               | 25.0                  | false    |

  @TestRailId:C85418
  Scenario: Verify Working Capital Repayment transaction that overpays loon with following CBR trn and with fee and penalty added with DUE_PRINCIPAL_FEE_PENALTY allocation - UC7
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 15.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "12 January 2026" due date and 25.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 12 January 2026 | 15.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 12 January 2026 | 25.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "12 January 2026" with 9200.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 100000.0           | 0.0            | 0.0              | 160.0             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 15.0       | 0.0             | 15.0     | 25.0           | 0.0                 | 25.0          |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Repayment    | 9200.0            | 9000.0           | 15.0              | 25.0                  | false    |
      | 12 January 2026 | Accrual      | 15.0              | 0.0              | 15.0              | 0.0                   | false    |
      | 12 January 2026 | Accrual      | 25.0              | 0.0              | 0.0               | 25.0                  | false    |
# --- make CBR trn to refund overpaid amount --- #
    And Customer makes credit balance refund on "12 January 2026" with 160.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 9000.0             | 100000.0           | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                  | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement          | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Repayment             | 9200.0            | 9000.0           | 15.0              | 25.0                  | false    |
      | 12 January 2026 | Accrual               | 15.0              | 0.0              | 15.0              | 0.0                   | false    |
      | 12 January 2026 | Accrual               | 25.0              | 0.0              | 0.0               | 25.0                  | false    |
      | 12 January 2026 | Credit Balance Refund | 160.0             | 160.0            | 0.0               | 0.0                   | false    |

  @TestRailId:C85419
  Scenario: Verify Working Capital Repayment transaction allocation with charges has been reprocessed successfully after additional backdated repayment - UC8
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct                    | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DUE_FEE_PENALTY_PRINCIPAL | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 100.0 transaction amount
    Then Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 100.0           | 0.0      | 0.0            | 0.0                 | 0.0          |
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "20 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Accrual           | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
      | 20 January 2026 | Repayment         | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 10 January 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 100.0    | 0.0            | 0.0                 | 0.0          |
# --- backdated repayment on Jan, 10, 20206 --- #
    And Customer makes repayment on "10 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Accrual           | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
      | 10 January 2026 | Repayment         | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
      | 20 January 2026 | Repayment         | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 10 January 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 100.0    | 0.0            | 0.0                 | 0.0          |

  @TestRailId:C85420
  Scenario: Verify Working Capital fee charge adjustment transaction allocation with full fee charge adjustment is processed successfully - UC9
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 100.0 transaction amount
    Then Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 100.0           | 0.0      | 0.0            | 0.0                 | 0.0          |
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin makes a charge adjustment for the last added charge with 70.0 amount on working capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Accrual           | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
      | 11 January 2026 | Charge Adjustment | 70.0              | 0.0              | 70.0              | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee | 10 January 2026 | 100.0  | EUR      | false     | Specified due date | Flat                    | Regular             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 30.0            | 70.0     | 0.0            | 0.0                 | 0.0          |

  @TestRailId:C85421
  Scenario: Verify Working Capital Repayment transaction with charge within amortization schedule with DUE_PENALTY_FEE_PRINCIPAL allocation - UC10
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct               | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "10 January 2026" due date and 35.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Penalty | 10 January 2026 | 35.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    Then Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    And Customer makes repayment on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 235.0              | 100000.0           | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 270.0             | 235.0            | 0.0               | 35.0                  | false    |
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan amortization schedule has 186 periods, with the following data for periods:
      | paymentNo | paymentDate      | expectedPaymentAmount | actualPaymentAmount | expectedBalance | actualBalance | expectedAmortizationAmount | actualAmortizationAmount | expectedDiscountFeeBalance |
      | 0         | 01 January 2026  | -9000.00              |                     | 9000.00         | 9000.00       |                            |                          | 0.00                       |
      | 1         | 02 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 2         | 03 January 2026  | 50.00                 | 0.00                | 8900.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 3         | 04 January 2026  | 50.00                 | 0.00                | 8850.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 4         | 05 January 2026  | 50.00                 | 0.00                | 8800.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 5         | 06 January 2026  | 50.00                 | 0.00                | 8750.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 6         | 07 January 2026  | 50.00                 | 0.00                | 8700.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 7         | 08 January 2026  | 50.00                 | 0.00                | 8650.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 8         | 09 January 2026  | 50.00                 | 0.00                | 8600.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 9         | 10 January 2026  | 50.00                 | 235.00              | 8550.00         | 8765.00       | 0.00                       | 0.00                     | 0.00                       |
      | 10        | 11 January 2026  | 50.00                 |                     | 8500.00         |               | 0.00                       |                          | 0.00                       |
      | 11        | 12 January 2026  | 50.00                 |                     | 8450.00         |               | 0.00                       |                          | 0.00                       |
      | 12        | 13 January 2026  | 50.00                 |                     | 8400.00         |               | 0.00                       |                          | 0.00                       |
      | 13        | 14 January 2026  | 50.00                 |                     | 8350.00         |               | 0.00                       |                          | 0.00                       |
      | 14        | 15 January 2026  | 50.00                 |                     | 8300.00         |               | 0.00                       |                          | 0.00                       |
      | 15        | 16 January 2026  | 50.00                 |                     | 8250.00         |               | 0.00                       |                          | 0.00                       |
      | 16        | 17 January 2026  | 50.00                 |                     | 8200.00         |               | 0.00                       |                          | 0.00                       |
      | 17        | 18 January 2026  | 50.00                 |                     | 8150.00         |               | 0.00                       |                          | 0.00                       |
      | 18        | 19 January 2026  | 50.00                 |                     | 8100.00         |               | 0.00                       |                          | 0.00                       |
      | 19        | 20 January 2026  | 50.00                 |                     | 8050.00         |               | 0.00                       |                          | 0.00                       |
      | 20        | 21 January 2026  | 50.00                 |                     | 8000.00         |               | 0.00                       |                          | 0.00                       |
      | 21        | 22 January 2026  | 50.00                 |                     | 7950.00         |               | 0.00                       |                          | 0.00                       |
      | 22        | 23 January 2026  | 50.00                 |                     | 7900.00         |               | 0.00                       |                          | 0.00                       |
      | 23        | 24 January 2026  | 50.00                 |                     | 7850.00         |               | 0.00                       |                          | 0.00                       |
      | 24        | 25 January 2026  | 50.00                 |                     | 7800.00         |               | 0.00                       |                          | 0.00                       |
      | 25        | 26 January 2026  | 50.00                 |                     | 7750.00         |               | 0.00                       |                          | 0.00                       |
      | 26        | 27 January 2026  | 50.00                 |                     | 7700.00         |               | 0.00                       |                          | 0.00                       |
      | 27        | 28 January 2026  | 50.00                 |                     | 7650.00         |               | 0.00                       |                          | 0.00                       |
      | 28        | 29 January 2026  | 50.00                 |                     | 7600.00         |               | 0.00                       |                          | 0.00                       |
      | 29        | 30 January 2026  | 50.00                 |                     | 7550.00         |               | 0.00                       |                          | 0.00                       |
      | 30        | 31 January 2026  | 50.00                 |                     | 7500.00         |               | 0.00                       |                          | 0.00                       |
      | 31        | 01 February 2026 | 50.00                 |                     | 7450.00         |               | 0.00                       |                          | 0.00                       |
      | 32        | 02 February 2026 | 50.00                 |                     | 7400.00         |               | 0.00                       |                          | 0.00                       |
      | 33        | 03 February 2026 | 50.00                 |                     | 7350.00         |               | 0.00                       |                          | 0.00                       |
      | 34        | 04 February 2026 | 50.00                 |                     | 7300.00         |               | 0.00                       |                          | 0.00                       |
      | 35        | 05 February 2026 | 50.00                 |                     | 7250.00         |               | 0.00                       |                          | 0.00                       |
      | 36        | 06 February 2026 | 50.00                 |                     | 7200.00         |               | 0.00                       |                          | 0.00                       |
      | 37        | 07 February 2026 | 50.00                 |                     | 7150.00         |               | 0.00                       |                          | 0.00                       |
      | 38        | 08 February 2026 | 50.00                 |                     | 7100.00         |               | 0.00                       |                          | 0.00                       |
      | 39        | 09 February 2026 | 50.00                 |                     | 7050.00         |               | 0.00                       |                          | 0.00                       |
      | 40        | 10 February 2026 | 50.00                 |                     | 7000.00         |               | 0.00                       |                          | 0.00                       |
      | 41        | 11 February 2026 | 50.00                 |                     | 6950.00         |               | 0.00                       |                          | 0.00                       |
      | 42        | 12 February 2026 | 50.00                 |                     | 6900.00         |               | 0.00                       |                          | 0.00                       |
      | 43        | 13 February 2026 | 50.00                 |                     | 6850.00         |               | 0.00                       |                          | 0.00                       |
      | 44        | 14 February 2026 | 50.00                 |                     | 6800.00         |               | 0.00                       |                          | 0.00                       |
      | 45        | 15 February 2026 | 50.00                 |                     | 6750.00         |               | 0.00                       |                          | 0.00                       |
      | 46        | 16 February 2026 | 50.00                 |                     | 6700.00         |               | 0.00                       |                          | 0.00                       |
      | 47        | 17 February 2026 | 50.00                 |                     | 6650.00         |               | 0.00                       |                          | 0.00                       |
      | 48        | 18 February 2026 | 50.00                 |                     | 6600.00         |               | 0.00                       |                          | 0.00                       |
      | 49        | 19 February 2026 | 50.00                 |                     | 6550.00         |               | 0.00                       |                          | 0.00                       |
      | 50        | 20 February 2026 | 50.00                 |                     | 6500.00         |               | 0.00                       |                          | 0.00                       |
      | 51        | 21 February 2026 | 50.00                 |                     | 6450.00         |               | 0.00                       |                          | 0.00                       |
      | 52        | 22 February 2026 | 50.00                 |                     | 6400.00         |               | 0.00                       |                          | 0.00                       |
      | 53        | 23 February 2026 | 50.00                 |                     | 6350.00         |               | 0.00                       |                          | 0.00                       |
      | 54        | 24 February 2026 | 50.00                 |                     | 6300.00         |               | 0.00                       |                          | 0.00                       |
      | 55        | 25 February 2026 | 50.00                 |                     | 6250.00         |               | 0.00                       |                          | 0.00                       |
      | 56        | 26 February 2026 | 50.00                 |                     | 6200.00         |               | 0.00                       |                          | 0.00                       |
      | 57        | 27 February 2026 | 50.00                 |                     | 6150.00         |               | 0.00                       |                          | 0.00                       |
      | 58        | 28 February 2026 | 50.00                 |                     | 6100.00         |               | 0.00                       |                          | 0.00                       |
      | 59        | 01 March 2026    | 50.00                 |                     | 6050.00         |               | 0.00                       |                          | 0.00                       |
      | 60        | 02 March 2026    | 50.00                 |                     | 6000.00         |               | 0.00                       |                          | 0.00                       |
      | 61        | 03 March 2026    | 50.00                 |                     | 5950.00         |               | 0.00                       |                          | 0.00                       |
      | 62        | 04 March 2026    | 50.00                 |                     | 5900.00         |               | 0.00                       |                          | 0.00                       |
      | 63        | 05 March 2026    | 50.00                 |                     | 5850.00         |               | 0.00                       |                          | 0.00                       |
      | 64        | 06 March 2026    | 50.00                 |                     | 5800.00         |               | 0.00                       |                          | 0.00                       |
      | 65        | 07 March 2026    | 50.00                 |                     | 5750.00         |               | 0.00                       |                          | 0.00                       |
      | 66        | 08 March 2026    | 50.00                 |                     | 5700.00         |               | 0.00                       |                          | 0.00                       |
      | 67        | 09 March 2026    | 50.00                 |                     | 5650.00         |               | 0.00                       |                          | 0.00                       |
      | 68        | 10 March 2026    | 50.00                 |                     | 5600.00         |               | 0.00                       |                          | 0.00                       |
      | 69        | 11 March 2026    | 50.00                 |                     | 5550.00         |               | 0.00                       |                          | 0.00                       |
      | 70        | 12 March 2026    | 50.00                 |                     | 5500.00         |               | 0.00                       |                          | 0.00                       |
      | 71        | 13 March 2026    | 50.00                 |                     | 5450.00         |               | 0.00                       |                          | 0.00                       |
      | 72        | 14 March 2026    | 50.00                 |                     | 5400.00         |               | 0.00                       |                          | 0.00                       |
      | 73        | 15 March 2026    | 50.00                 |                     | 5350.00         |               | 0.00                       |                          | 0.00                       |
      | 74        | 16 March 2026    | 50.00                 |                     | 5300.00         |               | 0.00                       |                          | 0.00                       |
      | 75        | 17 March 2026    | 50.00                 |                     | 5250.00         |               | 0.00                       |                          | 0.00                       |
      | 76        | 18 March 2026    | 50.00                 |                     | 5200.00         |               | 0.00                       |                          | 0.00                       |
      | 77        | 19 March 2026    | 50.00                 |                     | 5150.00         |               | 0.00                       |                          | 0.00                       |
      | 78        | 20 March 2026    | 50.00                 |                     | 5100.00         |               | 0.00                       |                          | 0.00                       |
      | 79        | 21 March 2026    | 50.00                 |                     | 5050.00         |               | 0.00                       |                          | 0.00                       |
      | 80        | 22 March 2026    | 50.00                 |                     | 5000.00         |               | 0.00                       |                          | 0.00                       |
      | 81        | 23 March 2026    | 50.00                 |                     | 4950.00         |               | 0.00                       |                          | 0.00                       |
      | 82        | 24 March 2026    | 50.00                 |                     | 4900.00         |               | 0.00                       |                          | 0.00                       |
      | 83        | 25 March 2026    | 50.00                 |                     | 4850.00         |               | 0.00                       |                          | 0.00                       |
      | 84        | 26 March 2026    | 50.00                 |                     | 4800.00         |               | 0.00                       |                          | 0.00                       |
      | 85        | 27 March 2026    | 50.00                 |                     | 4750.00         |               | 0.00                       |                          | 0.00                       |
      | 86        | 28 March 2026    | 50.00                 |                     | 4700.00         |               | 0.00                       |                          | 0.00                       |
      | 87        | 29 March 2026    | 50.00                 |                     | 4650.00         |               | 0.00                       |                          | 0.00                       |
      | 88        | 30 March 2026    | 50.00                 |                     | 4600.00         |               | 0.00                       |                          | 0.00                       |
      | 89        | 31 March 2026    | 50.00                 |                     | 4550.00         |               | 0.00                       |                          | 0.00                       |
      | 90        | 01 April 2026    | 50.00                 |                     | 4500.00         |               | 0.00                       |                          | 0.00                       |
      | 91        | 02 April 2026    | 50.00                 |                     | 4450.00         |               | 0.00                       |                          | 0.00                       |
      | 92        | 03 April 2026    | 50.00                 |                     | 4400.00         |               | 0.00                       |                          | 0.00                       |
      | 93        | 04 April 2026    | 50.00                 |                     | 4350.00         |               | 0.00                       |                          | 0.00                       |
      | 94        | 05 April 2026    | 50.00                 |                     | 4300.00         |               | 0.00                       |                          | 0.00                       |
      | 95        | 06 April 2026    | 50.00                 |                     | 4250.00         |               | 0.00                       |                          | 0.00                       |
      | 96        | 07 April 2026    | 50.00                 |                     | 4200.00         |               | 0.00                       |                          | 0.00                       |
      | 97        | 08 April 2026    | 50.00                 |                     | 4150.00         |               | 0.00                       |                          | 0.00                       |
      | 98        | 09 April 2026    | 50.00                 |                     | 4100.00         |               | 0.00                       |                          | 0.00                       |
      | 99        | 10 April 2026    | 50.00                 |                     | 4050.00         |               | 0.00                       |                          | 0.00                       |
      | 100       | 11 April 2026    | 50.00                 |                     | 4000.00         |               | 0.00                       |                          | 0.00                       |
      | 101       | 12 April 2026    | 50.00                 |                     | 3950.00         |               | 0.00                       |                          | 0.00                       |
      | 102       | 13 April 2026    | 50.00                 |                     | 3900.00         |               | 0.00                       |                          | 0.00                       |
      | 103       | 14 April 2026    | 50.00                 |                     | 3850.00         |               | 0.00                       |                          | 0.00                       |
      | 104       | 15 April 2026    | 50.00                 |                     | 3800.00         |               | 0.00                       |                          | 0.00                       |
      | 105       | 16 April 2026    | 50.00                 |                     | 3750.00         |               | 0.00                       |                          | 0.00                       |
      | 106       | 17 April 2026    | 50.00                 |                     | 3700.00         |               | 0.00                       |                          | 0.00                       |
      | 107       | 18 April 2026    | 50.00                 |                     | 3650.00         |               | 0.00                       |                          | 0.00                       |
      | 108       | 19 April 2026    | 50.00                 |                     | 3600.00         |               | 0.00                       |                          | 0.00                       |
      | 109       | 20 April 2026    | 50.00                 |                     | 3550.00         |               | 0.00                       |                          | 0.00                       |
      | 110       | 21 April 2026    | 50.00                 |                     | 3500.00         |               | 0.00                       |                          | 0.00                       |
      | 111       | 22 April 2026    | 50.00                 |                     | 3450.00         |               | 0.00                       |                          | 0.00                       |
      | 112       | 23 April 2026    | 50.00                 |                     | 3400.00         |               | 0.00                       |                          | 0.00                       |
      | 113       | 24 April 2026    | 50.00                 |                     | 3350.00         |               | 0.00                       |                          | 0.00                       |
      | 114       | 25 April 2026    | 50.00                 |                     | 3300.00         |               | 0.00                       |                          | 0.00                       |
      | 115       | 26 April 2026    | 50.00                 |                     | 3250.00         |               | 0.00                       |                          | 0.00                       |
      | 116       | 27 April 2026    | 50.00                 |                     | 3200.00         |               | 0.00                       |                          | 0.00                       |
      | 117       | 28 April 2026    | 50.00                 |                     | 3150.00         |               | 0.00                       |                          | 0.00                       |
      | 118       | 29 April 2026    | 50.00                 |                     | 3100.00         |               | 0.00                       |                          | 0.00                       |
      | 119       | 30 April 2026    | 50.00                 |                     | 3050.00         |               | 0.00                       |                          | 0.00                       |
      | 120       | 01 May 2026      | 50.00                 |                     | 3000.00         |               | 0.00                       |                          | 0.00                       |
      | 121       | 02 May 2026      | 50.00                 |                     | 2950.00         |               | 0.00                       |                          | 0.00                       |
      | 122       | 03 May 2026      | 50.00                 |                     | 2900.00         |               | 0.00                       |                          | 0.00                       |
      | 123       | 04 May 2026      | 50.00                 |                     | 2850.00         |               | 0.00                       |                          | 0.00                       |
      | 124       | 05 May 2026      | 50.00                 |                     | 2800.00         |               | 0.00                       |                          | 0.00                       |
      | 125       | 06 May 2026      | 50.00                 |                     | 2750.00         |               | 0.00                       |                          | 0.00                       |
      | 126       | 07 May 2026      | 50.00                 |                     | 2700.00         |               | 0.00                       |                          | 0.00                       |
      | 127       | 08 May 2026      | 50.00                 |                     | 2650.00         |               | 0.00                       |                          | 0.00                       |
      | 128       | 09 May 2026      | 50.00                 |                     | 2600.00         |               | 0.00                       |                          | 0.00                       |
      | 129       | 10 May 2026      | 50.00                 |                     | 2550.00         |               | 0.00                       |                          | 0.00                       |
      | 130       | 11 May 2026      | 50.00                 |                     | 2500.00         |               | 0.00                       |                          | 0.00                       |
      | 131       | 12 May 2026      | 50.00                 |                     | 2450.00         |               | 0.00                       |                          | 0.00                       |
      | 132       | 13 May 2026      | 50.00                 |                     | 2400.00         |               | 0.00                       |                          | 0.00                       |
      | 133       | 14 May 2026      | 50.00                 |                     | 2350.00         |               | 0.00                       |                          | 0.00                       |
      | 134       | 15 May 2026      | 50.00                 |                     | 2300.00         |               | 0.00                       |                          | 0.00                       |
      | 135       | 16 May 2026      | 50.00                 |                     | 2250.00         |               | 0.00                       |                          | 0.00                       |
      | 136       | 17 May 2026      | 50.00                 |                     | 2200.00         |               | 0.00                       |                          | 0.00                       |
      | 137       | 18 May 2026      | 50.00                 |                     | 2150.00         |               | 0.00                       |                          | 0.00                       |
      | 138       | 19 May 2026      | 50.00                 |                     | 2100.00         |               | 0.00                       |                          | 0.00                       |
      | 139       | 20 May 2026      | 50.00                 |                     | 2050.00         |               | 0.00                       |                          | 0.00                       |
      | 140       | 21 May 2026      | 50.00                 |                     | 2000.00         |               | 0.00                       |                          | 0.00                       |
      | 141       | 22 May 2026      | 50.00                 |                     | 1950.00         |               | 0.00                       |                          | 0.00                       |
      | 142       | 23 May 2026      | 50.00                 |                     | 1900.00         |               | 0.00                       |                          | 0.00                       |
      | 143       | 24 May 2026      | 50.00                 |                     | 1850.00         |               | 0.00                       |                          | 0.00                       |
      | 144       | 25 May 2026      | 50.00                 |                     | 1800.00         |               | 0.00                       |                          | 0.00                       |
      | 145       | 26 May 2026      | 50.00                 |                     | 1750.00         |               | 0.00                       |                          | 0.00                       |
      | 146       | 27 May 2026      | 50.00                 |                     | 1700.00         |               | 0.00                       |                          | 0.00                       |
      | 147       | 28 May 2026      | 50.00                 |                     | 1650.00         |               | 0.00                       |                          | 0.00                       |
      | 148       | 29 May 2026      | 50.00                 |                     | 1600.00         |               | 0.00                       |                          | 0.00                       |
      | 149       | 30 May 2026      | 50.00                 |                     | 1550.00         |               | 0.00                       |                          | 0.00                       |
      | 150       | 31 May 2026      | 50.00                 |                     | 1500.00         |               | 0.00                       |                          | 0.00                       |
      | 151       | 01 June 2026     | 50.00                 |                     | 1450.00         |               | 0.00                       |                          | 0.00                       |
      | 152       | 02 June 2026     | 50.00                 |                     | 1400.00         |               | 0.00                       |                          | 0.00                       |
      | 153       | 03 June 2026     | 50.00                 |                     | 1350.00         |               | 0.00                       |                          | 0.00                       |
      | 154       | 04 June 2026     | 50.00                 |                     | 1300.00         |               | 0.00                       |                          | 0.00                       |
      | 155       | 05 June 2026     | 50.00                 |                     | 1250.00         |               | 0.00                       |                          | 0.00                       |
      | 156       | 06 June 2026     | 50.00                 |                     | 1200.00         |               | 0.00                       |                          | 0.00                       |
      | 157       | 07 June 2026     | 50.00                 |                     | 1150.00         |               | 0.00                       |                          | 0.00                       |
      | 158       | 08 June 2026     | 50.00                 |                     | 1100.00         |               | 0.00                       |                          | 0.00                       |
      | 159       | 09 June 2026     | 50.00                 |                     | 1050.00         |               | 0.00                       |                          | 0.00                       |
      | 160       | 10 June 2026     | 50.00                 |                     | 1000.00         |               | 0.00                       |                          | 0.00                       |
      | 161       | 11 June 2026     | 50.00                 |                     | 950.00          |               | 0.00                       |                          | 0.00                       |
      | 162       | 12 June 2026     | 50.00                 |                     | 900.00          |               | 0.00                       |                          | 0.00                       |
      | 163       | 13 June 2026     | 50.00                 |                     | 850.00          |               | 0.00                       |                          | 0.00                       |
      | 164       | 14 June 2026     | 50.00                 |                     | 800.00          |               | 0.00                       |                          | 0.00                       |
      | 165       | 15 June 2026     | 50.00                 |                     | 750.00          |               | 0.00                       |                          | 0.00                       |
      | 166       | 16 June 2026     | 50.00                 |                     | 700.00          |               | 0.00                       |                          | 0.00                       |
      | 167       | 17 June 2026     | 50.00                 |                     | 650.00          |               | 0.00                       |                          | 0.00                       |
      | 168       | 18 June 2026     | 50.00                 |                     | 600.00          |               | 0.00                       |                          | 0.00                       |
      | 169       | 19 June 2026     | 50.00                 |                     | 550.00          |               | 0.00                       |                          | 0.00                       |
      | 170       | 20 June 2026     | 50.00                 |                     | 500.00          |               | 0.00                       |                          | 0.00                       |
      | 171       | 21 June 2026     | 50.00                 |                     | 450.00          |               | 0.00                       |                          | 0.00                       |
      | 172       | 22 June 2026     | 50.00                 |                     | 400.00          |               | 0.00                       |                          | 0.00                       |
      | 173       | 23 June 2026     | 50.00                 |                     | 350.00          |               | 0.00                       |                          | 0.00                       |
      | 174       | 24 June 2026     | 50.00                 |                     | 300.00          |               | 0.00                       |                          | 0.00                       |
      | 175       | 25 June 2026     | 50.00                 |                     | 250.00          |               | 0.00                       |                          | 0.00                       |
      | 176       | 26 June 2026     | 50.00                 |                     | 200.00          |               | 0.00                       |                          | 0.00                       |
      | 177       | 27 June 2026     | 50.00                 |                     | 150.00          |               | 0.00                       |                          | 0.00                       |
      | 178       | 28 June 2026     | 50.00                 |                     | 100.00          |               | 0.00                       |                          | 0.00                       |
      | 179       | 29 June 2026     | 50.00                 |                     | 50.00           |               | 0.00                       |                          | 0.00                       |
      | 180       | 30 June 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 181       | 01 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 182       | 02 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 183       | 03 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 184       | 04 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 185       | 05 July 2026     | 15.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |

  @TestRailId:C85422
  Scenario: Verify Working Capital Repayment with fee and penalty charges within amortization schedule with DUE_FEE_PENALTY_PRINCIPAL allocation - UC11
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct                    | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_DUE_FEE_PENALTY_PRINCIPAL | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "12 January 2026" due date and 35.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "12 January 2026" due date and 43.0 transaction amount
    Then Working Capital Loan has charges with the following data:
      | Charge Name                  | Due Date        | Amount | Currency | isPenalty | Charge Time Type   | Charge Calculation Type | Charge Payment mode |
      | Working Capital Loan Fee     | 12 January 2026 | 35.0   | EUR      | false     | Specified due date | Flat                    | Regular             |
      | Working Capital Loan Penalty | 12 January 2026 | 43.0   | EUR      | true      | Specified due date | Flat                    | Regular             |
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "12 January 2026" with 270.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | totalPaymentVolume | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 192.0              | 100000.0           | 0.0            | 0.0              | 0.0             |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 35.0       | 0.0             | 35.0     | 43.0           | 0.0                 | 43.0         |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Repayment    | 270.0             | 192.0            | 35.0              | 43.0                  | false    |
    When Admin sets the business date to "13 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan amortization schedule has 189 periods, with the following data for periods:
      | paymentNo | paymentDate      | expectedPaymentAmount | actualPaymentAmount | expectedBalance | actualBalance | expectedAmortizationAmount | actualAmortizationAmount | expectedDiscountFeeBalance |
      | 0         | 01 January 2026  | -9000.00              |                     | 9000.00         | 9000.00       |                            |                          | 0.00                       |
      | 1         | 02 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 2         | 03 January 2026  | 50.00                 | 0.00                | 8900.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 3         | 04 January 2026  | 50.00                 | 0.00                | 8850.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 4         | 05 January 2026  | 50.00                 | 0.00                | 8800.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 5         | 06 January 2026  | 50.00                 | 0.00                | 8750.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 6         | 07 January 2026  | 50.00                 | 0.00                | 8700.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 7         | 08 January 2026  | 50.00                 | 0.00                | 8650.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 8         | 09 January 2026  | 50.00                 | 0.00                | 8600.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 9         | 10 January 2026  | 50.00                 | 0.00                | 8550.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 10        | 11 January 2026  | 50.00                 | 0.00                | 8500.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 11        | 12 January 2026  | 50.00                 | 192.00              | 8450.00         | 8808.00       | 0.00                       | 0.00                     | 0.00                       |
      | 12        | 13 January 2026  | 50.00                 |                     | 8400.00         |               | 0.00                       |                          | 0.00                       |
      | 13        | 14 January 2026  | 50.00                 |                     | 8350.00         |               | 0.00                       |                          | 0.00                       |
      | 14        | 15 January 2026  | 50.00                 |                     | 8300.00         |               | 0.00                       |                          | 0.00                       |
      | 15        | 16 January 2026  | 50.00                 |                     | 8250.00         |               | 0.00                       |                          | 0.00                       |
      | 16        | 17 January 2026  | 50.00                 |                     | 8200.00         |               | 0.00                       |                          | 0.00                       |
      | 17        | 18 January 2026  | 50.00                 |                     | 8150.00         |               | 0.00                       |                          | 0.00                       |
      | 18        | 19 January 2026  | 50.00                 |                     | 8100.00         |               | 0.00                       |                          | 0.00                       |
      | 19        | 20 January 2026  | 50.00                 |                     | 8050.00         |               | 0.00                       |                          | 0.00                       |
      | 20        | 21 January 2026  | 50.00                 |                     | 8000.00         |               | 0.00                       |                          | 0.00                       |
      | 21        | 22 January 2026  | 50.00                 |                     | 7950.00         |               | 0.00                       |                          | 0.00                       |
      | 22        | 23 January 2026  | 50.00                 |                     | 7900.00         |               | 0.00                       |                          | 0.00                       |
      | 23        | 24 January 2026  | 50.00                 |                     | 7850.00         |               | 0.00                       |                          | 0.00                       |
      | 24        | 25 January 2026  | 50.00                 |                     | 7800.00         |               | 0.00                       |                          | 0.00                       |
      | 25        | 26 January 2026  | 50.00                 |                     | 7750.00         |               | 0.00                       |                          | 0.00                       |
      | 26        | 27 January 2026  | 50.00                 |                     | 7700.00         |               | 0.00                       |                          | 0.00                       |
      | 27        | 28 January 2026  | 50.00                 |                     | 7650.00         |               | 0.00                       |                          | 0.00                       |
      | 28        | 29 January 2026  | 50.00                 |                     | 7600.00         |               | 0.00                       |                          | 0.00                       |
      | 29        | 30 January 2026  | 50.00                 |                     | 7550.00         |               | 0.00                       |                          | 0.00                       |
      | 30        | 31 January 2026  | 50.00                 |                     | 7500.00         |               | 0.00                       |                          | 0.00                       |
      | 31        | 01 February 2026 | 50.00                 |                     | 7450.00         |               | 0.00                       |                          | 0.00                       |
      | 32        | 02 February 2026 | 50.00                 |                     | 7400.00         |               | 0.00                       |                          | 0.00                       |
      | 33        | 03 February 2026 | 50.00                 |                     | 7350.00         |               | 0.00                       |                          | 0.00                       |
      | 34        | 04 February 2026 | 50.00                 |                     | 7300.00         |               | 0.00                       |                          | 0.00                       |
      | 35        | 05 February 2026 | 50.00                 |                     | 7250.00         |               | 0.00                       |                          | 0.00                       |
      | 36        | 06 February 2026 | 50.00                 |                     | 7200.00         |               | 0.00                       |                          | 0.00                       |
      | 37        | 07 February 2026 | 50.00                 |                     | 7150.00         |               | 0.00                       |                          | 0.00                       |
      | 38        | 08 February 2026 | 50.00                 |                     | 7100.00         |               | 0.00                       |                          | 0.00                       |
      | 39        | 09 February 2026 | 50.00                 |                     | 7050.00         |               | 0.00                       |                          | 0.00                       |
      | 40        | 10 February 2026 | 50.00                 |                     | 7000.00         |               | 0.00                       |                          | 0.00                       |
      | 41        | 11 February 2026 | 50.00                 |                     | 6950.00         |               | 0.00                       |                          | 0.00                       |
      | 42        | 12 February 2026 | 50.00                 |                     | 6900.00         |               | 0.00                       |                          | 0.00                       |
      | 43        | 13 February 2026 | 50.00                 |                     | 6850.00         |               | 0.00                       |                          | 0.00                       |
      | 44        | 14 February 2026 | 50.00                 |                     | 6800.00         |               | 0.00                       |                          | 0.00                       |
      | 45        | 15 February 2026 | 50.00                 |                     | 6750.00         |               | 0.00                       |                          | 0.00                       |
      | 46        | 16 February 2026 | 50.00                 |                     | 6700.00         |               | 0.00                       |                          | 0.00                       |
      | 47        | 17 February 2026 | 50.00                 |                     | 6650.00         |               | 0.00                       |                          | 0.00                       |
      | 48        | 18 February 2026 | 50.00                 |                     | 6600.00         |               | 0.00                       |                          | 0.00                       |
      | 49        | 19 February 2026 | 50.00                 |                     | 6550.00         |               | 0.00                       |                          | 0.00                       |
      | 50        | 20 February 2026 | 50.00                 |                     | 6500.00         |               | 0.00                       |                          | 0.00                       |
      | 51        | 21 February 2026 | 50.00                 |                     | 6450.00         |               | 0.00                       |                          | 0.00                       |
      | 52        | 22 February 2026 | 50.00                 |                     | 6400.00         |               | 0.00                       |                          | 0.00                       |
      | 53        | 23 February 2026 | 50.00                 |                     | 6350.00         |               | 0.00                       |                          | 0.00                       |
      | 54        | 24 February 2026 | 50.00                 |                     | 6300.00         |               | 0.00                       |                          | 0.00                       |
      | 55        | 25 February 2026 | 50.00                 |                     | 6250.00         |               | 0.00                       |                          | 0.00                       |
      | 56        | 26 February 2026 | 50.00                 |                     | 6200.00         |               | 0.00                       |                          | 0.00                       |
      | 57        | 27 February 2026 | 50.00                 |                     | 6150.00         |               | 0.00                       |                          | 0.00                       |
      | 58        | 28 February 2026 | 50.00                 |                     | 6100.00         |               | 0.00                       |                          | 0.00                       |
      | 59        | 01 March 2026    | 50.00                 |                     | 6050.00         |               | 0.00                       |                          | 0.00                       |
      | 60        | 02 March 2026    | 50.00                 |                     | 6000.00         |               | 0.00                       |                          | 0.00                       |
      | 61        | 03 March 2026    | 50.00                 |                     | 5950.00         |               | 0.00                       |                          | 0.00                       |
      | 62        | 04 March 2026    | 50.00                 |                     | 5900.00         |               | 0.00                       |                          | 0.00                       |
      | 63        | 05 March 2026    | 50.00                 |                     | 5850.00         |               | 0.00                       |                          | 0.00                       |
      | 64        | 06 March 2026    | 50.00                 |                     | 5800.00         |               | 0.00                       |                          | 0.00                       |
      | 65        | 07 March 2026    | 50.00                 |                     | 5750.00         |               | 0.00                       |                          | 0.00                       |
      | 66        | 08 March 2026    | 50.00                 |                     | 5700.00         |               | 0.00                       |                          | 0.00                       |
      | 67        | 09 March 2026    | 50.00                 |                     | 5650.00         |               | 0.00                       |                          | 0.00                       |
      | 68        | 10 March 2026    | 50.00                 |                     | 5600.00         |               | 0.00                       |                          | 0.00                       |
      | 69        | 11 March 2026    | 50.00                 |                     | 5550.00         |               | 0.00                       |                          | 0.00                       |
      | 70        | 12 March 2026    | 50.00                 |                     | 5500.00         |               | 0.00                       |                          | 0.00                       |
      | 71        | 13 March 2026    | 50.00                 |                     | 5450.00         |               | 0.00                       |                          | 0.00                       |
      | 72        | 14 March 2026    | 50.00                 |                     | 5400.00         |               | 0.00                       |                          | 0.00                       |
      | 73        | 15 March 2026    | 50.00                 |                     | 5350.00         |               | 0.00                       |                          | 0.00                       |
      | 74        | 16 March 2026    | 50.00                 |                     | 5300.00         |               | 0.00                       |                          | 0.00                       |
      | 75        | 17 March 2026    | 50.00                 |                     | 5250.00         |               | 0.00                       |                          | 0.00                       |
      | 76        | 18 March 2026    | 50.00                 |                     | 5200.00         |               | 0.00                       |                          | 0.00                       |
      | 77        | 19 March 2026    | 50.00                 |                     | 5150.00         |               | 0.00                       |                          | 0.00                       |
      | 78        | 20 March 2026    | 50.00                 |                     | 5100.00         |               | 0.00                       |                          | 0.00                       |
      | 79        | 21 March 2026    | 50.00                 |                     | 5050.00         |               | 0.00                       |                          | 0.00                       |
      | 80        | 22 March 2026    | 50.00                 |                     | 5000.00         |               | 0.00                       |                          | 0.00                       |
      | 81        | 23 March 2026    | 50.00                 |                     | 4950.00         |               | 0.00                       |                          | 0.00                       |
      | 82        | 24 March 2026    | 50.00                 |                     | 4900.00         |               | 0.00                       |                          | 0.00                       |
      | 83        | 25 March 2026    | 50.00                 |                     | 4850.00         |               | 0.00                       |                          | 0.00                       |
      | 84        | 26 March 2026    | 50.00                 |                     | 4800.00         |               | 0.00                       |                          | 0.00                       |
      | 85        | 27 March 2026    | 50.00                 |                     | 4750.00         |               | 0.00                       |                          | 0.00                       |
      | 86        | 28 March 2026    | 50.00                 |                     | 4700.00         |               | 0.00                       |                          | 0.00                       |
      | 87        | 29 March 2026    | 50.00                 |                     | 4650.00         |               | 0.00                       |                          | 0.00                       |
      | 88        | 30 March 2026    | 50.00                 |                     | 4600.00         |               | 0.00                       |                          | 0.00                       |
      | 89        | 31 March 2026    | 50.00                 |                     | 4550.00         |               | 0.00                       |                          | 0.00                       |
      | 90        | 01 April 2026    | 50.00                 |                     | 4500.00         |               | 0.00                       |                          | 0.00                       |
      | 91        | 02 April 2026    | 50.00                 |                     | 4450.00         |               | 0.00                       |                          | 0.00                       |
      | 92        | 03 April 2026    | 50.00                 |                     | 4400.00         |               | 0.00                       |                          | 0.00                       |
      | 93        | 04 April 2026    | 50.00                 |                     | 4350.00         |               | 0.00                       |                          | 0.00                       |
      | 94        | 05 April 2026    | 50.00                 |                     | 4300.00         |               | 0.00                       |                          | 0.00                       |
      | 95        | 06 April 2026    | 50.00                 |                     | 4250.00         |               | 0.00                       |                          | 0.00                       |
      | 96        | 07 April 2026    | 50.00                 |                     | 4200.00         |               | 0.00                       |                          | 0.00                       |
      | 97        | 08 April 2026    | 50.00                 |                     | 4150.00         |               | 0.00                       |                          | 0.00                       |
      | 98        | 09 April 2026    | 50.00                 |                     | 4100.00         |               | 0.00                       |                          | 0.00                       |
      | 99        | 10 April 2026    | 50.00                 |                     | 4050.00         |               | 0.00                       |                          | 0.00                       |
      | 100       | 11 April 2026    | 50.00                 |                     | 4000.00         |               | 0.00                       |                          | 0.00                       |
      | 101       | 12 April 2026    | 50.00                 |                     | 3950.00         |               | 0.00                       |                          | 0.00                       |
      | 102       | 13 April 2026    | 50.00                 |                     | 3900.00         |               | 0.00                       |                          | 0.00                       |
      | 103       | 14 April 2026    | 50.00                 |                     | 3850.00         |               | 0.00                       |                          | 0.00                       |
      | 104       | 15 April 2026    | 50.00                 |                     | 3800.00         |               | 0.00                       |                          | 0.00                       |
      | 105       | 16 April 2026    | 50.00                 |                     | 3750.00         |               | 0.00                       |                          | 0.00                       |
      | 106       | 17 April 2026    | 50.00                 |                     | 3700.00         |               | 0.00                       |                          | 0.00                       |
      | 107       | 18 April 2026    | 50.00                 |                     | 3650.00         |               | 0.00                       |                          | 0.00                       |
      | 108       | 19 April 2026    | 50.00                 |                     | 3600.00         |               | 0.00                       |                          | 0.00                       |
      | 109       | 20 April 2026    | 50.00                 |                     | 3550.00         |               | 0.00                       |                          | 0.00                       |
      | 110       | 21 April 2026    | 50.00                 |                     | 3500.00         |               | 0.00                       |                          | 0.00                       |
      | 111       | 22 April 2026    | 50.00                 |                     | 3450.00         |               | 0.00                       |                          | 0.00                       |
      | 112       | 23 April 2026    | 50.00                 |                     | 3400.00         |               | 0.00                       |                          | 0.00                       |
      | 113       | 24 April 2026    | 50.00                 |                     | 3350.00         |               | 0.00                       |                          | 0.00                       |
      | 114       | 25 April 2026    | 50.00                 |                     | 3300.00         |               | 0.00                       |                          | 0.00                       |
      | 115       | 26 April 2026    | 50.00                 |                     | 3250.00         |               | 0.00                       |                          | 0.00                       |
      | 116       | 27 April 2026    | 50.00                 |                     | 3200.00         |               | 0.00                       |                          | 0.00                       |
      | 117       | 28 April 2026    | 50.00                 |                     | 3150.00         |               | 0.00                       |                          | 0.00                       |
      | 118       | 29 April 2026    | 50.00                 |                     | 3100.00         |               | 0.00                       |                          | 0.00                       |
      | 119       | 30 April 2026    | 50.00                 |                     | 3050.00         |               | 0.00                       |                          | 0.00                       |
      | 120       | 01 May 2026      | 50.00                 |                     | 3000.00         |               | 0.00                       |                          | 0.00                       |
      | 121       | 02 May 2026      | 50.00                 |                     | 2950.00         |               | 0.00                       |                          | 0.00                       |
      | 122       | 03 May 2026      | 50.00                 |                     | 2900.00         |               | 0.00                       |                          | 0.00                       |
      | 123       | 04 May 2026      | 50.00                 |                     | 2850.00         |               | 0.00                       |                          | 0.00                       |
      | 124       | 05 May 2026      | 50.00                 |                     | 2800.00         |               | 0.00                       |                          | 0.00                       |
      | 125       | 06 May 2026      | 50.00                 |                     | 2750.00         |               | 0.00                       |                          | 0.00                       |
      | 126       | 07 May 2026      | 50.00                 |                     | 2700.00         |               | 0.00                       |                          | 0.00                       |
      | 127       | 08 May 2026      | 50.00                 |                     | 2650.00         |               | 0.00                       |                          | 0.00                       |
      | 128       | 09 May 2026      | 50.00                 |                     | 2600.00         |               | 0.00                       |                          | 0.00                       |
      | 129       | 10 May 2026      | 50.00                 |                     | 2550.00         |               | 0.00                       |                          | 0.00                       |
      | 130       | 11 May 2026      | 50.00                 |                     | 2500.00         |               | 0.00                       |                          | 0.00                       |
      | 131       | 12 May 2026      | 50.00                 |                     | 2450.00         |               | 0.00                       |                          | 0.00                       |
      | 132       | 13 May 2026      | 50.00                 |                     | 2400.00         |               | 0.00                       |                          | 0.00                       |
      | 133       | 14 May 2026      | 50.00                 |                     | 2350.00         |               | 0.00                       |                          | 0.00                       |
      | 134       | 15 May 2026      | 50.00                 |                     | 2300.00         |               | 0.00                       |                          | 0.00                       |
      | 135       | 16 May 2026      | 50.00                 |                     | 2250.00         |               | 0.00                       |                          | 0.00                       |
      | 136       | 17 May 2026      | 50.00                 |                     | 2200.00         |               | 0.00                       |                          | 0.00                       |
      | 137       | 18 May 2026      | 50.00                 |                     | 2150.00         |               | 0.00                       |                          | 0.00                       |
      | 138       | 19 May 2026      | 50.00                 |                     | 2100.00         |               | 0.00                       |                          | 0.00                       |
      | 139       | 20 May 2026      | 50.00                 |                     | 2050.00         |               | 0.00                       |                          | 0.00                       |
      | 140       | 21 May 2026      | 50.00                 |                     | 2000.00         |               | 0.00                       |                          | 0.00                       |
      | 141       | 22 May 2026      | 50.00                 |                     | 1950.00         |               | 0.00                       |                          | 0.00                       |
      | 142       | 23 May 2026      | 50.00                 |                     | 1900.00         |               | 0.00                       |                          | 0.00                       |
      | 143       | 24 May 2026      | 50.00                 |                     | 1850.00         |               | 0.00                       |                          | 0.00                       |
      | 144       | 25 May 2026      | 50.00                 |                     | 1800.00         |               | 0.00                       |                          | 0.00                       |
      | 145       | 26 May 2026      | 50.00                 |                     | 1750.00         |               | 0.00                       |                          | 0.00                       |
      | 146       | 27 May 2026      | 50.00                 |                     | 1700.00         |               | 0.00                       |                          | 0.00                       |
      | 147       | 28 May 2026      | 50.00                 |                     | 1650.00         |               | 0.00                       |                          | 0.00                       |
      | 148       | 29 May 2026      | 50.00                 |                     | 1600.00         |               | 0.00                       |                          | 0.00                       |
      | 149       | 30 May 2026      | 50.00                 |                     | 1550.00         |               | 0.00                       |                          | 0.00                       |
      | 150       | 31 May 2026      | 50.00                 |                     | 1500.00         |               | 0.00                       |                          | 0.00                       |
      | 151       | 01 June 2026     | 50.00                 |                     | 1450.00         |               | 0.00                       |                          | 0.00                       |
      | 152       | 02 June 2026     | 50.00                 |                     | 1400.00         |               | 0.00                       |                          | 0.00                       |
      | 153       | 03 June 2026     | 50.00                 |                     | 1350.00         |               | 0.00                       |                          | 0.00                       |
      | 154       | 04 June 2026     | 50.00                 |                     | 1300.00         |               | 0.00                       |                          | 0.00                       |
      | 155       | 05 June 2026     | 50.00                 |                     | 1250.00         |               | 0.00                       |                          | 0.00                       |
      | 156       | 06 June 2026     | 50.00                 |                     | 1200.00         |               | 0.00                       |                          | 0.00                       |
      | 157       | 07 June 2026     | 50.00                 |                     | 1150.00         |               | 0.00                       |                          | 0.00                       |
      | 158       | 08 June 2026     | 50.00                 |                     | 1100.00         |               | 0.00                       |                          | 0.00                       |
      | 159       | 09 June 2026     | 50.00                 |                     | 1050.00         |               | 0.00                       |                          | 0.00                       |
      | 160       | 10 June 2026     | 50.00                 |                     | 1000.00         |               | 0.00                       |                          | 0.00                       |
      | 161       | 11 June 2026     | 50.00                 |                     | 950.00          |               | 0.00                       |                          | 0.00                       |
      | 162       | 12 June 2026     | 50.00                 |                     | 900.00          |               | 0.00                       |                          | 0.00                       |
      | 163       | 13 June 2026     | 50.00                 |                     | 850.00          |               | 0.00                       |                          | 0.00                       |
      | 164       | 14 June 2026     | 50.00                 |                     | 800.00          |               | 0.00                       |                          | 0.00                       |
      | 165       | 15 June 2026     | 50.00                 |                     | 750.00          |               | 0.00                       |                          | 0.00                       |
      | 166       | 16 June 2026     | 50.00                 |                     | 700.00          |               | 0.00                       |                          | 0.00                       |
      | 167       | 17 June 2026     | 50.00                 |                     | 650.00          |               | 0.00                       |                          | 0.00                       |
      | 168       | 18 June 2026     | 50.00                 |                     | 600.00          |               | 0.00                       |                          | 0.00                       |
      | 169       | 19 June 2026     | 50.00                 |                     | 550.00          |               | 0.00                       |                          | 0.00                       |
      | 170       | 20 June 2026     | 50.00                 |                     | 500.00          |               | 0.00                       |                          | 0.00                       |
      | 171       | 21 June 2026     | 50.00                 |                     | 450.00          |               | 0.00                       |                          | 0.00                       |
      | 172       | 22 June 2026     | 50.00                 |                     | 400.00          |               | 0.00                       |                          | 0.00                       |
      | 173       | 23 June 2026     | 50.00                 |                     | 350.00          |               | 0.00                       |                          | 0.00                       |
      | 174       | 24 June 2026     | 50.00                 |                     | 300.00          |               | 0.00                       |                          | 0.00                       |
      | 175       | 25 June 2026     | 50.00                 |                     | 250.00          |               | 0.00                       |                          | 0.00                       |
      | 176       | 26 June 2026     | 50.00                 |                     | 200.00          |               | 0.00                       |                          | 0.00                       |
      | 177       | 27 June 2026     | 50.00                 |                     | 150.00          |               | 0.00                       |                          | 0.00                       |
      | 178       | 28 June 2026     | 50.00                 |                     | 100.00          |               | 0.00                       |                          | 0.00                       |
      | 179       | 29 June 2026     | 50.00                 |                     | 50.00           |               | 0.00                       |                          | 0.00                       |
      | 180       | 30 June 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 181       | 01 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 182       | 02 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 183       | 03 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 184       | 04 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 185       | 05 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 186       | 06 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 187       | 07 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 188       | 08 July 2026     |  8.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |