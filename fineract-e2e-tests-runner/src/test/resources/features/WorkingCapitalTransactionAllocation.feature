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
      | 12 January 2026 | Credit Balance Refund | 160.0             | 0.0              | 0.0               | 0.0                   | false    |

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
      | 2         | 03 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 3         | 04 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 4         | 05 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 5         | 06 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 6         | 07 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 7         | 08 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 8         | 09 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 9         | 10 January 2026  | 50.00                 | 235.00              | 8950.00         | 8765.00       | 0.00                       | 0.00                     | 0.00                       |
      | 10        | 11 January 2026  | 50.00                 |                     | 8715.00         |               | 0.00                       |                          | 0.00                       |
      | 11        | 12 January 2026  | 50.00                 |                     | 8665.00         |               | 0.00                       |                          | 0.00                       |
      | 12        | 13 January 2026  | 50.00                 |                     | 8615.00         |               | 0.00                       |                          | 0.00                       |
      | 13        | 14 January 2026  | 50.00                 |                     | 8565.00         |               | 0.00                       |                          | 0.00                       |
      | 14        | 15 January 2026  | 50.00                 |                     | 8515.00         |               | 0.00                       |                          | 0.00                       |
      | 15        | 16 January 2026  | 50.00                 |                     | 8465.00         |               | 0.00                       |                          | 0.00                       |
      | 16        | 17 January 2026  | 50.00                 |                     | 8415.00         |               | 0.00                       |                          | 0.00                       |
      | 17        | 18 January 2026  | 50.00                 |                     | 8365.00         |               | 0.00                       |                          | 0.00                       |
      | 18        | 19 January 2026  | 50.00                 |                     | 8315.00         |               | 0.00                       |                          | 0.00                       |
      | 19        | 20 January 2026  | 50.00                 |                     | 8265.00         |               | 0.00                       |                          | 0.00                       |
      | 20        | 21 January 2026  | 50.00                 |                     | 8215.00         |               | 0.00                       |                          | 0.00                       |
      | 21        | 22 January 2026  | 50.00                 |                     | 8165.00         |               | 0.00                       |                          | 0.00                       |
      | 22        | 23 January 2026  | 50.00                 |                     | 8115.00         |               | 0.00                       |                          | 0.00                       |
      | 23        | 24 January 2026  | 50.00                 |                     | 8065.00         |               | 0.00                       |                          | 0.00                       |
      | 24        | 25 January 2026  | 50.00                 |                     | 8015.00         |               | 0.00                       |                          | 0.00                       |
      | 25        | 26 January 2026  | 50.00                 |                     | 7965.00         |               | 0.00                       |                          | 0.00                       |
      | 26        | 27 January 2026  | 50.00                 |                     | 7915.00         |               | 0.00                       |                          | 0.00                       |
      | 27        | 28 January 2026  | 50.00                 |                     | 7865.00         |               | 0.00                       |                          | 0.00                       |
      | 28        | 29 January 2026  | 50.00                 |                     | 7815.00         |               | 0.00                       |                          | 0.00                       |
      | 29        | 30 January 2026  | 50.00                 |                     | 7765.00         |               | 0.00                       |                          | 0.00                       |
      | 30        | 31 January 2026  | 50.00                 |                     | 7715.00         |               | 0.00                       |                          | 0.00                       |
      | 31        | 01 February 2026 | 50.00                 |                     | 7665.00         |               | 0.00                       |                          | 0.00                       |
      | 32        | 02 February 2026 | 50.00                 |                     | 7615.00         |               | 0.00                       |                          | 0.00                       |
      | 33        | 03 February 2026 | 50.00                 |                     | 7565.00         |               | 0.00                       |                          | 0.00                       |
      | 34        | 04 February 2026 | 50.00                 |                     | 7515.00         |               | 0.00                       |                          | 0.00                       |
      | 35        | 05 February 2026 | 50.00                 |                     | 7465.00         |               | 0.00                       |                          | 0.00                       |
      | 36        | 06 February 2026 | 50.00                 |                     | 7415.00         |               | 0.00                       |                          | 0.00                       |
      | 37        | 07 February 2026 | 50.00                 |                     | 7365.00         |               | 0.00                       |                          | 0.00                       |
      | 38        | 08 February 2026 | 50.00                 |                     | 7315.00         |               | 0.00                       |                          | 0.00                       |
      | 39        | 09 February 2026 | 50.00                 |                     | 7265.00         |               | 0.00                       |                          | 0.00                       |
      | 40        | 10 February 2026 | 50.00                 |                     | 7215.00         |               | 0.00                       |                          | 0.00                       |
      | 41        | 11 February 2026 | 50.00                 |                     | 7165.00         |               | 0.00                       |                          | 0.00                       |
      | 42        | 12 February 2026 | 50.00                 |                     | 7115.00         |               | 0.00                       |                          | 0.00                       |
      | 43        | 13 February 2026 | 50.00                 |                     | 7065.00         |               | 0.00                       |                          | 0.00                       |
      | 44        | 14 February 2026 | 50.00                 |                     | 7015.00         |               | 0.00                       |                          | 0.00                       |
      | 45        | 15 February 2026 | 50.00                 |                     | 6965.00         |               | 0.00                       |                          | 0.00                       |
      | 46        | 16 February 2026 | 50.00                 |                     | 6915.00         |               | 0.00                       |                          | 0.00                       |
      | 47        | 17 February 2026 | 50.00                 |                     | 6865.00         |               | 0.00                       |                          | 0.00                       |
      | 48        | 18 February 2026 | 50.00                 |                     | 6815.00         |               | 0.00                       |                          | 0.00                       |
      | 49        | 19 February 2026 | 50.00                 |                     | 6765.00         |               | 0.00                       |                          | 0.00                       |
      | 50        | 20 February 2026 | 50.00                 |                     | 6715.00         |               | 0.00                       |                          | 0.00                       |
      | 51        | 21 February 2026 | 50.00                 |                     | 6665.00         |               | 0.00                       |                          | 0.00                       |
      | 52        | 22 February 2026 | 50.00                 |                     | 6615.00         |               | 0.00                       |                          | 0.00                       |
      | 53        | 23 February 2026 | 50.00                 |                     | 6565.00         |               | 0.00                       |                          | 0.00                       |
      | 54        | 24 February 2026 | 50.00                 |                     | 6515.00         |               | 0.00                       |                          | 0.00                       |
      | 55        | 25 February 2026 | 50.00                 |                     | 6465.00         |               | 0.00                       |                          | 0.00                       |
      | 56        | 26 February 2026 | 50.00                 |                     | 6415.00         |               | 0.00                       |                          | 0.00                       |
      | 57        | 27 February 2026 | 50.00                 |                     | 6365.00         |               | 0.00                       |                          | 0.00                       |
      | 58        | 28 February 2026 | 50.00                 |                     | 6315.00         |               | 0.00                       |                          | 0.00                       |
      | 59        | 01 March 2026    | 50.00                 |                     | 6265.00         |               | 0.00                       |                          | 0.00                       |
      | 60        | 02 March 2026    | 50.00                 |                     | 6215.00         |               | 0.00                       |                          | 0.00                       |
      | 61        | 03 March 2026    | 50.00                 |                     | 6165.00         |               | 0.00                       |                          | 0.00                       |
      | 62        | 04 March 2026    | 50.00                 |                     | 6115.00         |               | 0.00                       |                          | 0.00                       |
      | 63        | 05 March 2026    | 50.00                 |                     | 6065.00         |               | 0.00                       |                          | 0.00                       |
      | 64        | 06 March 2026    | 50.00                 |                     | 6015.00         |               | 0.00                       |                          | 0.00                       |
      | 65        | 07 March 2026    | 50.00                 |                     | 5965.00         |               | 0.00                       |                          | 0.00                       |
      | 66        | 08 March 2026    | 50.00                 |                     | 5915.00         |               | 0.00                       |                          | 0.00                       |
      | 67        | 09 March 2026    | 50.00                 |                     | 5865.00         |               | 0.00                       |                          | 0.00                       |
      | 68        | 10 March 2026    | 50.00                 |                     | 5815.00         |               | 0.00                       |                          | 0.00                       |
      | 69        | 11 March 2026    | 50.00                 |                     | 5765.00         |               | 0.00                       |                          | 0.00                       |
      | 70        | 12 March 2026    | 50.00                 |                     | 5715.00         |               | 0.00                       |                          | 0.00                       |
      | 71        | 13 March 2026    | 50.00                 |                     | 5665.00         |               | 0.00                       |                          | 0.00                       |
      | 72        | 14 March 2026    | 50.00                 |                     | 5615.00         |               | 0.00                       |                          | 0.00                       |
      | 73        | 15 March 2026    | 50.00                 |                     | 5565.00         |               | 0.00                       |                          | 0.00                       |
      | 74        | 16 March 2026    | 50.00                 |                     | 5515.00         |               | 0.00                       |                          | 0.00                       |
      | 75        | 17 March 2026    | 50.00                 |                     | 5465.00         |               | 0.00                       |                          | 0.00                       |
      | 76        | 18 March 2026    | 50.00                 |                     | 5415.00         |               | 0.00                       |                          | 0.00                       |
      | 77        | 19 March 2026    | 50.00                 |                     | 5365.00         |               | 0.00                       |                          | 0.00                       |
      | 78        | 20 March 2026    | 50.00                 |                     | 5315.00         |               | 0.00                       |                          | 0.00                       |
      | 79        | 21 March 2026    | 50.00                 |                     | 5265.00         |               | 0.00                       |                          | 0.00                       |
      | 80        | 22 March 2026    | 50.00                 |                     | 5215.00         |               | 0.00                       |                          | 0.00                       |
      | 81        | 23 March 2026    | 50.00                 |                     | 5165.00         |               | 0.00                       |                          | 0.00                       |
      | 82        | 24 March 2026    | 50.00                 |                     | 5115.00         |               | 0.00                       |                          | 0.00                       |
      | 83        | 25 March 2026    | 50.00                 |                     | 5065.00         |               | 0.00                       |                          | 0.00                       |
      | 84        | 26 March 2026    | 50.00                 |                     | 5015.00         |               | 0.00                       |                          | 0.00                       |
      | 85        | 27 March 2026    | 50.00                 |                     | 4965.00         |               | 0.00                       |                          | 0.00                       |
      | 86        | 28 March 2026    | 50.00                 |                     | 4915.00         |               | 0.00                       |                          | 0.00                       |
      | 87        | 29 March 2026    | 50.00                 |                     | 4865.00         |               | 0.00                       |                          | 0.00                       |
      | 88        | 30 March 2026    | 50.00                 |                     | 4815.00         |               | 0.00                       |                          | 0.00                       |
      | 89        | 31 March 2026    | 50.00                 |                     | 4765.00         |               | 0.00                       |                          | 0.00                       |
      | 90        | 01 April 2026    | 50.00                 |                     | 4715.00         |               | 0.00                       |                          | 0.00                       |
      | 91        | 02 April 2026    | 50.00                 |                     | 4665.00         |               | 0.00                       |                          | 0.00                       |
      | 92        | 03 April 2026    | 50.00                 |                     | 4615.00         |               | 0.00                       |                          | 0.00                       |
      | 93        | 04 April 2026    | 50.00                 |                     | 4565.00         |               | 0.00                       |                          | 0.00                       |
      | 94        | 05 April 2026    | 50.00                 |                     | 4515.00         |               | 0.00                       |                          | 0.00                       |
      | 95        | 06 April 2026    | 50.00                 |                     | 4465.00         |               | 0.00                       |                          | 0.00                       |
      | 96        | 07 April 2026    | 50.00                 |                     | 4415.00         |               | 0.00                       |                          | 0.00                       |
      | 97        | 08 April 2026    | 50.00                 |                     | 4365.00         |               | 0.00                       |                          | 0.00                       |
      | 98        | 09 April 2026    | 50.00                 |                     | 4315.00         |               | 0.00                       |                          | 0.00                       |
      | 99        | 10 April 2026    | 50.00                 |                     | 4265.00         |               | 0.00                       |                          | 0.00                       |
      | 100       | 11 April 2026    | 50.00                 |                     | 4215.00         |               | 0.00                       |                          | 0.00                       |
      | 101       | 12 April 2026    | 50.00                 |                     | 4165.00         |               | 0.00                       |                          | 0.00                       |
      | 102       | 13 April 2026    | 50.00                 |                     | 4115.00         |               | 0.00                       |                          | 0.00                       |
      | 103       | 14 April 2026    | 50.00                 |                     | 4065.00         |               | 0.00                       |                          | 0.00                       |
      | 104       | 15 April 2026    | 50.00                 |                     | 4015.00         |               | 0.00                       |                          | 0.00                       |
      | 105       | 16 April 2026    | 50.00                 |                     | 3965.00         |               | 0.00                       |                          | 0.00                       |
      | 106       | 17 April 2026    | 50.00                 |                     | 3915.00         |               | 0.00                       |                          | 0.00                       |
      | 107       | 18 April 2026    | 50.00                 |                     | 3865.00         |               | 0.00                       |                          | 0.00                       |
      | 108       | 19 April 2026    | 50.00                 |                     | 3815.00         |               | 0.00                       |                          | 0.00                       |
      | 109       | 20 April 2026    | 50.00                 |                     | 3765.00         |               | 0.00                       |                          | 0.00                       |
      | 110       | 21 April 2026    | 50.00                 |                     | 3715.00         |               | 0.00                       |                          | 0.00                       |
      | 111       | 22 April 2026    | 50.00                 |                     | 3665.00         |               | 0.00                       |                          | 0.00                       |
      | 112       | 23 April 2026    | 50.00                 |                     | 3615.00         |               | 0.00                       |                          | 0.00                       |
      | 113       | 24 April 2026    | 50.00                 |                     | 3565.00         |               | 0.00                       |                          | 0.00                       |
      | 114       | 25 April 2026    | 50.00                 |                     | 3515.00         |               | 0.00                       |                          | 0.00                       |
      | 115       | 26 April 2026    | 50.00                 |                     | 3465.00         |               | 0.00                       |                          | 0.00                       |
      | 116       | 27 April 2026    | 50.00                 |                     | 3415.00         |               | 0.00                       |                          | 0.00                       |
      | 117       | 28 April 2026    | 50.00                 |                     | 3365.00         |               | 0.00                       |                          | 0.00                       |
      | 118       | 29 April 2026    | 50.00                 |                     | 3315.00         |               | 0.00                       |                          | 0.00                       |
      | 119       | 30 April 2026    | 50.00                 |                     | 3265.00         |               | 0.00                       |                          | 0.00                       |
      | 120       | 01 May 2026      | 50.00                 |                     | 3215.00         |               | 0.00                       |                          | 0.00                       |
      | 121       | 02 May 2026      | 50.00                 |                     | 3165.00         |               | 0.00                       |                          | 0.00                       |
      | 122       | 03 May 2026      | 50.00                 |                     | 3115.00         |               | 0.00                       |                          | 0.00                       |
      | 123       | 04 May 2026      | 50.00                 |                     | 3065.00         |               | 0.00                       |                          | 0.00                       |
      | 124       | 05 May 2026      | 50.00                 |                     | 3015.00         |               | 0.00                       |                          | 0.00                       |
      | 125       | 06 May 2026      | 50.00                 |                     | 2965.00         |               | 0.00                       |                          | 0.00                       |
      | 126       | 07 May 2026      | 50.00                 |                     | 2915.00         |               | 0.00                       |                          | 0.00                       |
      | 127       | 08 May 2026      | 50.00                 |                     | 2865.00         |               | 0.00                       |                          | 0.00                       |
      | 128       | 09 May 2026      | 50.00                 |                     | 2815.00         |               | 0.00                       |                          | 0.00                       |
      | 129       | 10 May 2026      | 50.00                 |                     | 2765.00         |               | 0.00                       |                          | 0.00                       |
      | 130       | 11 May 2026      | 50.00                 |                     | 2715.00         |               | 0.00                       |                          | 0.00                       |
      | 131       | 12 May 2026      | 50.00                 |                     | 2665.00         |               | 0.00                       |                          | 0.00                       |
      | 132       | 13 May 2026      | 50.00                 |                     | 2615.00         |               | 0.00                       |                          | 0.00                       |
      | 133       | 14 May 2026      | 50.00                 |                     | 2565.00         |               | 0.00                       |                          | 0.00                       |
      | 134       | 15 May 2026      | 50.00                 |                     | 2515.00         |               | 0.00                       |                          | 0.00                       |
      | 135       | 16 May 2026      | 50.00                 |                     | 2465.00         |               | 0.00                       |                          | 0.00                       |
      | 136       | 17 May 2026      | 50.00                 |                     | 2415.00         |               | 0.00                       |                          | 0.00                       |
      | 137       | 18 May 2026      | 50.00                 |                     | 2365.00         |               | 0.00                       |                          | 0.00                       |
      | 138       | 19 May 2026      | 50.00                 |                     | 2315.00         |               | 0.00                       |                          | 0.00                       |
      | 139       | 20 May 2026      | 50.00                 |                     | 2265.00         |               | 0.00                       |                          | 0.00                       |
      | 140       | 21 May 2026      | 50.00                 |                     | 2215.00         |               | 0.00                       |                          | 0.00                       |
      | 141       | 22 May 2026      | 50.00                 |                     | 2165.00         |               | 0.00                       |                          | 0.00                       |
      | 142       | 23 May 2026      | 50.00                 |                     | 2115.00         |               | 0.00                       |                          | 0.00                       |
      | 143       | 24 May 2026      | 50.00                 |                     | 2065.00         |               | 0.00                       |                          | 0.00                       |
      | 144       | 25 May 2026      | 50.00                 |                     | 2015.00         |               | 0.00                       |                          | 0.00                       |
      | 145       | 26 May 2026      | 50.00                 |                     | 1965.00         |               | 0.00                       |                          | 0.00                       |
      | 146       | 27 May 2026      | 50.00                 |                     | 1915.00         |               | 0.00                       |                          | 0.00                       |
      | 147       | 28 May 2026      | 50.00                 |                     | 1865.00         |               | 0.00                       |                          | 0.00                       |
      | 148       | 29 May 2026      | 50.00                 |                     | 1815.00         |               | 0.00                       |                          | 0.00                       |
      | 149       | 30 May 2026      | 50.00                 |                     | 1765.00         |               | 0.00                       |                          | 0.00                       |
      | 150       | 31 May 2026      | 50.00                 |                     | 1715.00         |               | 0.00                       |                          | 0.00                       |
      | 151       | 01 June 2026     | 50.00                 |                     | 1665.00         |               | 0.00                       |                          | 0.00                       |
      | 152       | 02 June 2026     | 50.00                 |                     | 1615.00         |               | 0.00                       |                          | 0.00                       |
      | 153       | 03 June 2026     | 50.00                 |                     | 1565.00         |               | 0.00                       |                          | 0.00                       |
      | 154       | 04 June 2026     | 50.00                 |                     | 1515.00         |               | 0.00                       |                          | 0.00                       |
      | 155       | 05 June 2026     | 50.00                 |                     | 1465.00         |               | 0.00                       |                          | 0.00                       |
      | 156       | 06 June 2026     | 50.00                 |                     | 1415.00         |               | 0.00                       |                          | 0.00                       |
      | 157       | 07 June 2026     | 50.00                 |                     | 1365.00         |               | 0.00                       |                          | 0.00                       |
      | 158       | 08 June 2026     | 50.00                 |                     | 1315.00         |               | 0.00                       |                          | 0.00                       |
      | 159       | 09 June 2026     | 50.00                 |                     | 1265.00         |               | 0.00                       |                          | 0.00                       |
      | 160       | 10 June 2026     | 50.00                 |                     | 1215.00         |               | 0.00                       |                          | 0.00                       |
      | 161       | 11 June 2026     | 50.00                 |                     | 1165.00         |               | 0.00                       |                          | 0.00                       |
      | 162       | 12 June 2026     | 50.00                 |                     | 1115.00         |               | 0.00                       |                          | 0.00                       |
      | 163       | 13 June 2026     | 50.00                 |                     | 1065.00         |               | 0.00                       |                          | 0.00                       |
      | 164       | 14 June 2026     | 50.00                 |                     | 1015.00         |               | 0.00                       |                          | 0.00                       |
      | 165       | 15 June 2026     | 50.00                 |                     | 965.00          |               | 0.00                       |                          | 0.00                       |
      | 166       | 16 June 2026     | 50.00                 |                     | 915.00          |               | 0.00                       |                          | 0.00                       |
      | 167       | 17 June 2026     | 50.00                 |                     | 865.00          |               | 0.00                       |                          | 0.00                       |
      | 168       | 18 June 2026     | 50.00                 |                     | 815.00          |               | 0.00                       |                          | 0.00                       |
      | 169       | 19 June 2026     | 50.00                 |                     | 765.00          |               | 0.00                       |                          | 0.00                       |
      | 170       | 20 June 2026     | 50.00                 |                     | 715.00          |               | 0.00                       |                          | 0.00                       |
      | 171       | 21 June 2026     | 50.00                 |                     | 665.00          |               | 0.00                       |                          | 0.00                       |
      | 172       | 22 June 2026     | 50.00                 |                     | 615.00          |               | 0.00                       |                          | 0.00                       |
      | 173       | 23 June 2026     | 50.00                 |                     | 565.00          |               | 0.00                       |                          | 0.00                       |
      | 174       | 24 June 2026     | 50.00                 |                     | 515.00          |               | 0.00                       |                          | 0.00                       |
      | 175       | 25 June 2026     | 50.00                 |                     | 465.00          |               | 0.00                       |                          | 0.00                       |
      | 176       | 26 June 2026     | 50.00                 |                     | 415.00          |               | 0.00                       |                          | 0.00                       |
      | 177       | 27 June 2026     | 50.00                 |                     | 365.00          |               | 0.00                       |                          | 0.00                       |
      | 178       | 28 June 2026     | 50.00                 |                     | 315.00          |               | 0.00                       |                          | 0.00                       |
      | 179       | 29 June 2026     | 50.00                 |                     | 265.00          |               | 0.00                       |                          | 0.00                       |
      | 180       | 30 June 2026     | 50.00                 |                     | 215.00          |               | 0.00                       |                          | 0.00                       |
      | 181       | 01 July 2026     | 50.00                 |                     | 165.00          |               | 0.00                       |                          | 0.00                       |
      | 182       | 02 July 2026     | 50.00                 |                     | 115.00          |               | 0.00                       |                          | 0.00                       |
      | 183       | 03 July 2026     | 50.00                 |                     | 65.00           |               | 0.00                       |                          | 0.00                       |
      | 184       | 04 July 2026     | 50.00                 |                     | 15.00           |               | 0.00                       |                          | 0.00                       |
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
      | 2         | 03 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 3         | 04 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 4         | 05 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 5         | 06 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 6         | 07 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 7         | 08 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 8         | 09 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 9         | 10 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 10        | 11 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 11        | 12 January 2026  | 50.00                 | 192.00              | 8950.00         | 8808.00       | 0.00                       | 0.00                     | 0.00                       |
      | 12        | 13 January 2026  | 50.00                 |                     | 8758.00         |               | 0.00                       |                          | 0.00                       |
      | 13        | 14 January 2026  | 50.00                 |                     | 8708.00         |               | 0.00                       |                          | 0.00                       |
      | 14        | 15 January 2026  | 50.00                 |                     | 8658.00         |               | 0.00                       |                          | 0.00                       |
      | 15        | 16 January 2026  | 50.00                 |                     | 8608.00         |               | 0.00                       |                          | 0.00                       |
      | 16        | 17 January 2026  | 50.00                 |                     | 8558.00         |               | 0.00                       |                          | 0.00                       |
      | 17        | 18 January 2026  | 50.00                 |                     | 8508.00         |               | 0.00                       |                          | 0.00                       |
      | 18        | 19 January 2026  | 50.00                 |                     | 8458.00         |               | 0.00                       |                          | 0.00                       |
      | 19        | 20 January 2026  | 50.00                 |                     | 8408.00         |               | 0.00                       |                          | 0.00                       |
      | 20        | 21 January 2026  | 50.00                 |                     | 8358.00         |               | 0.00                       |                          | 0.00                       |
      | 21        | 22 January 2026  | 50.00                 |                     | 8308.00         |               | 0.00                       |                          | 0.00                       |
      | 22        | 23 January 2026  | 50.00                 |                     | 8258.00         |               | 0.00                       |                          | 0.00                       |
      | 23        | 24 January 2026  | 50.00                 |                     | 8208.00         |               | 0.00                       |                          | 0.00                       |
      | 24        | 25 January 2026  | 50.00                 |                     | 8158.00         |               | 0.00                       |                          | 0.00                       |
      | 25        | 26 January 2026  | 50.00                 |                     | 8108.00         |               | 0.00                       |                          | 0.00                       |
      | 26        | 27 January 2026  | 50.00                 |                     | 8058.00         |               | 0.00                       |                          | 0.00                       |
      | 27        | 28 January 2026  | 50.00                 |                     | 8008.00         |               | 0.00                       |                          | 0.00                       |
      | 28        | 29 January 2026  | 50.00                 |                     | 7958.00         |               | 0.00                       |                          | 0.00                       |
      | 29        | 30 January 2026  | 50.00                 |                     | 7908.00         |               | 0.00                       |                          | 0.00                       |
      | 30        | 31 January 2026  | 50.00                 |                     | 7858.00         |               | 0.00                       |                          | 0.00                       |
      | 31        | 01 February 2026 | 50.00                 |                     | 7808.00         |               | 0.00                       |                          | 0.00                       |
      | 32        | 02 February 2026 | 50.00                 |                     | 7758.00         |               | 0.00                       |                          | 0.00                       |
      | 33        | 03 February 2026 | 50.00                 |                     | 7708.00         |               | 0.00                       |                          | 0.00                       |
      | 34        | 04 February 2026 | 50.00                 |                     | 7658.00         |               | 0.00                       |                          | 0.00                       |
      | 35        | 05 February 2026 | 50.00                 |                     | 7608.00         |               | 0.00                       |                          | 0.00                       |
      | 36        | 06 February 2026 | 50.00                 |                     | 7558.00         |               | 0.00                       |                          | 0.00                       |
      | 37        | 07 February 2026 | 50.00                 |                     | 7508.00         |               | 0.00                       |                          | 0.00                       |
      | 38        | 08 February 2026 | 50.00                 |                     | 7458.00         |               | 0.00                       |                          | 0.00                       |
      | 39        | 09 February 2026 | 50.00                 |                     | 7408.00         |               | 0.00                       |                          | 0.00                       |
      | 40        | 10 February 2026 | 50.00                 |                     | 7358.00         |               | 0.00                       |                          | 0.00                       |
      | 41        | 11 February 2026 | 50.00                 |                     | 7308.00         |               | 0.00                       |                          | 0.00                       |
      | 42        | 12 February 2026 | 50.00                 |                     | 7258.00         |               | 0.00                       |                          | 0.00                       |
      | 43        | 13 February 2026 | 50.00                 |                     | 7208.00         |               | 0.00                       |                          | 0.00                       |
      | 44        | 14 February 2026 | 50.00                 |                     | 7158.00         |               | 0.00                       |                          | 0.00                       |
      | 45        | 15 February 2026 | 50.00                 |                     | 7108.00         |               | 0.00                       |                          | 0.00                       |
      | 46        | 16 February 2026 | 50.00                 |                     | 7058.00         |               | 0.00                       |                          | 0.00                       |
      | 47        | 17 February 2026 | 50.00                 |                     | 7008.00         |               | 0.00                       |                          | 0.00                       |
      | 48        | 18 February 2026 | 50.00                 |                     | 6958.00         |               | 0.00                       |                          | 0.00                       |
      | 49        | 19 February 2026 | 50.00                 |                     | 6908.00         |               | 0.00                       |                          | 0.00                       |
      | 50        | 20 February 2026 | 50.00                 |                     | 6858.00         |               | 0.00                       |                          | 0.00                       |
      | 51        | 21 February 2026 | 50.00                 |                     | 6808.00         |               | 0.00                       |                          | 0.00                       |
      | 52        | 22 February 2026 | 50.00                 |                     | 6758.00         |               | 0.00                       |                          | 0.00                       |
      | 53        | 23 February 2026 | 50.00                 |                     | 6708.00         |               | 0.00                       |                          | 0.00                       |
      | 54        | 24 February 2026 | 50.00                 |                     | 6658.00         |               | 0.00                       |                          | 0.00                       |
      | 55        | 25 February 2026 | 50.00                 |                     | 6608.00         |               | 0.00                       |                          | 0.00                       |
      | 56        | 26 February 2026 | 50.00                 |                     | 6558.00         |               | 0.00                       |                          | 0.00                       |
      | 57        | 27 February 2026 | 50.00                 |                     | 6508.00         |               | 0.00                       |                          | 0.00                       |
      | 58        | 28 February 2026 | 50.00                 |                     | 6458.00         |               | 0.00                       |                          | 0.00                       |
      | 59        | 01 March 2026    | 50.00                 |                     | 6408.00         |               | 0.00                       |                          | 0.00                       |
      | 60        | 02 March 2026    | 50.00                 |                     | 6358.00         |               | 0.00                       |                          | 0.00                       |
      | 61        | 03 March 2026    | 50.00                 |                     | 6308.00         |               | 0.00                       |                          | 0.00                       |
      | 62        | 04 March 2026    | 50.00                 |                     | 6258.00         |               | 0.00                       |                          | 0.00                       |
      | 63        | 05 March 2026    | 50.00                 |                     | 6208.00         |               | 0.00                       |                          | 0.00                       |
      | 64        | 06 March 2026    | 50.00                 |                     | 6158.00         |               | 0.00                       |                          | 0.00                       |
      | 65        | 07 March 2026    | 50.00                 |                     | 6108.00         |               | 0.00                       |                          | 0.00                       |
      | 66        | 08 March 2026    | 50.00                 |                     | 6058.00         |               | 0.00                       |                          | 0.00                       |
      | 67        | 09 March 2026    | 50.00                 |                     | 6008.00         |               | 0.00                       |                          | 0.00                       |
      | 68        | 10 March 2026    | 50.00                 |                     | 5958.00         |               | 0.00                       |                          | 0.00                       |
      | 69        | 11 March 2026    | 50.00                 |                     | 5908.00         |               | 0.00                       |                          | 0.00                       |
      | 70        | 12 March 2026    | 50.00                 |                     | 5858.00         |               | 0.00                       |                          | 0.00                       |
      | 71        | 13 March 2026    | 50.00                 |                     | 5808.00         |               | 0.00                       |                          | 0.00                       |
      | 72        | 14 March 2026    | 50.00                 |                     | 5758.00         |               | 0.00                       |                          | 0.00                       |
      | 73        | 15 March 2026    | 50.00                 |                     | 5708.00         |               | 0.00                       |                          | 0.00                       |
      | 74        | 16 March 2026    | 50.00                 |                     | 5658.00         |               | 0.00                       |                          | 0.00                       |
      | 75        | 17 March 2026    | 50.00                 |                     | 5608.00         |               | 0.00                       |                          | 0.00                       |
      | 76        | 18 March 2026    | 50.00                 |                     | 5558.00         |               | 0.00                       |                          | 0.00                       |
      | 77        | 19 March 2026    | 50.00                 |                     | 5508.00         |               | 0.00                       |                          | 0.00                       |
      | 78        | 20 March 2026    | 50.00                 |                     | 5458.00         |               | 0.00                       |                          | 0.00                       |
      | 79        | 21 March 2026    | 50.00                 |                     | 5408.00         |               | 0.00                       |                          | 0.00                       |
      | 80        | 22 March 2026    | 50.00                 |                     | 5358.00         |               | 0.00                       |                          | 0.00                       |
      | 81        | 23 March 2026    | 50.00                 |                     | 5308.00         |               | 0.00                       |                          | 0.00                       |
      | 82        | 24 March 2026    | 50.00                 |                     | 5258.00         |               | 0.00                       |                          | 0.00                       |
      | 83        | 25 March 2026    | 50.00                 |                     | 5208.00         |               | 0.00                       |                          | 0.00                       |
      | 84        | 26 March 2026    | 50.00                 |                     | 5158.00         |               | 0.00                       |                          | 0.00                       |
      | 85        | 27 March 2026    | 50.00                 |                     | 5108.00         |               | 0.00                       |                          | 0.00                       |
      | 86        | 28 March 2026    | 50.00                 |                     | 5058.00         |               | 0.00                       |                          | 0.00                       |
      | 87        | 29 March 2026    | 50.00                 |                     | 5008.00         |               | 0.00                       |                          | 0.00                       |
      | 88        | 30 March 2026    | 50.00                 |                     | 4958.00         |               | 0.00                       |                          | 0.00                       |
      | 89        | 31 March 2026    | 50.00                 |                     | 4908.00         |               | 0.00                       |                          | 0.00                       |
      | 90        | 01 April 2026    | 50.00                 |                     | 4858.00         |               | 0.00                       |                          | 0.00                       |
      | 91        | 02 April 2026    | 50.00                 |                     | 4808.00         |               | 0.00                       |                          | 0.00                       |
      | 92        | 03 April 2026    | 50.00                 |                     | 4758.00         |               | 0.00                       |                          | 0.00                       |
      | 93        | 04 April 2026    | 50.00                 |                     | 4708.00         |               | 0.00                       |                          | 0.00                       |
      | 94        | 05 April 2026    | 50.00                 |                     | 4658.00         |               | 0.00                       |                          | 0.00                       |
      | 95        | 06 April 2026    | 50.00                 |                     | 4608.00         |               | 0.00                       |                          | 0.00                       |
      | 96        | 07 April 2026    | 50.00                 |                     | 4558.00         |               | 0.00                       |                          | 0.00                       |
      | 97        | 08 April 2026    | 50.00                 |                     | 4508.00         |               | 0.00                       |                          | 0.00                       |
      | 98        | 09 April 2026    | 50.00                 |                     | 4458.00         |               | 0.00                       |                          | 0.00                       |
      | 99        | 10 April 2026    | 50.00                 |                     | 4408.00         |               | 0.00                       |                          | 0.00                       |
      | 100       | 11 April 2026    | 50.00                 |                     | 4358.00         |               | 0.00                       |                          | 0.00                       |
      | 101       | 12 April 2026    | 50.00                 |                     | 4308.00         |               | 0.00                       |                          | 0.00                       |
      | 102       | 13 April 2026    | 50.00                 |                     | 4258.00         |               | 0.00                       |                          | 0.00                       |
      | 103       | 14 April 2026    | 50.00                 |                     | 4208.00         |               | 0.00                       |                          | 0.00                       |
      | 104       | 15 April 2026    | 50.00                 |                     | 4158.00         |               | 0.00                       |                          | 0.00                       |
      | 105       | 16 April 2026    | 50.00                 |                     | 4108.00         |               | 0.00                       |                          | 0.00                       |
      | 106       | 17 April 2026    | 50.00                 |                     | 4058.00         |               | 0.00                       |                          | 0.00                       |
      | 107       | 18 April 2026    | 50.00                 |                     | 4008.00         |               | 0.00                       |                          | 0.00                       |
      | 108       | 19 April 2026    | 50.00                 |                     | 3958.00         |               | 0.00                       |                          | 0.00                       |
      | 109       | 20 April 2026    | 50.00                 |                     | 3908.00         |               | 0.00                       |                          | 0.00                       |
      | 110       | 21 April 2026    | 50.00                 |                     | 3858.00         |               | 0.00                       |                          | 0.00                       |
      | 111       | 22 April 2026    | 50.00                 |                     | 3808.00         |               | 0.00                       |                          | 0.00                       |
      | 112       | 23 April 2026    | 50.00                 |                     | 3758.00         |               | 0.00                       |                          | 0.00                       |
      | 113       | 24 April 2026    | 50.00                 |                     | 3708.00         |               | 0.00                       |                          | 0.00                       |
      | 114       | 25 April 2026    | 50.00                 |                     | 3658.00         |               | 0.00                       |                          | 0.00                       |
      | 115       | 26 April 2026    | 50.00                 |                     | 3608.00         |               | 0.00                       |                          | 0.00                       |
      | 116       | 27 April 2026    | 50.00                 |                     | 3558.00         |               | 0.00                       |                          | 0.00                       |
      | 117       | 28 April 2026    | 50.00                 |                     | 3508.00         |               | 0.00                       |                          | 0.00                       |
      | 118       | 29 April 2026    | 50.00                 |                     | 3458.00         |               | 0.00                       |                          | 0.00                       |
      | 119       | 30 April 2026    | 50.00                 |                     | 3408.00         |               | 0.00                       |                          | 0.00                       |
      | 120       | 01 May 2026      | 50.00                 |                     | 3358.00         |               | 0.00                       |                          | 0.00                       |
      | 121       | 02 May 2026      | 50.00                 |                     | 3308.00         |               | 0.00                       |                          | 0.00                       |
      | 122       | 03 May 2026      | 50.00                 |                     | 3258.00         |               | 0.00                       |                          | 0.00                       |
      | 123       | 04 May 2026      | 50.00                 |                     | 3208.00         |               | 0.00                       |                          | 0.00                       |
      | 124       | 05 May 2026      | 50.00                 |                     | 3158.00         |               | 0.00                       |                          | 0.00                       |
      | 125       | 06 May 2026      | 50.00                 |                     | 3108.00         |               | 0.00                       |                          | 0.00                       |
      | 126       | 07 May 2026      | 50.00                 |                     | 3058.00         |               | 0.00                       |                          | 0.00                       |
      | 127       | 08 May 2026      | 50.00                 |                     | 3008.00         |               | 0.00                       |                          | 0.00                       |
      | 128       | 09 May 2026      | 50.00                 |                     | 2958.00         |               | 0.00                       |                          | 0.00                       |
      | 129       | 10 May 2026      | 50.00                 |                     | 2908.00         |               | 0.00                       |                          | 0.00                       |
      | 130       | 11 May 2026      | 50.00                 |                     | 2858.00         |               | 0.00                       |                          | 0.00                       |
      | 131       | 12 May 2026      | 50.00                 |                     | 2808.00         |               | 0.00                       |                          | 0.00                       |
      | 132       | 13 May 2026      | 50.00                 |                     | 2758.00         |               | 0.00                       |                          | 0.00                       |
      | 133       | 14 May 2026      | 50.00                 |                     | 2708.00         |               | 0.00                       |                          | 0.00                       |
      | 134       | 15 May 2026      | 50.00                 |                     | 2658.00         |               | 0.00                       |                          | 0.00                       |
      | 135       | 16 May 2026      | 50.00                 |                     | 2608.00         |               | 0.00                       |                          | 0.00                       |
      | 136       | 17 May 2026      | 50.00                 |                     | 2558.00         |               | 0.00                       |                          | 0.00                       |
      | 137       | 18 May 2026      | 50.00                 |                     | 2508.00         |               | 0.00                       |                          | 0.00                       |
      | 138       | 19 May 2026      | 50.00                 |                     | 2458.00         |               | 0.00                       |                          | 0.00                       |
      | 139       | 20 May 2026      | 50.00                 |                     | 2408.00         |               | 0.00                       |                          | 0.00                       |
      | 140       | 21 May 2026      | 50.00                 |                     | 2358.00         |               | 0.00                       |                          | 0.00                       |
      | 141       | 22 May 2026      | 50.00                 |                     | 2308.00         |               | 0.00                       |                          | 0.00                       |
      | 142       | 23 May 2026      | 50.00                 |                     | 2258.00         |               | 0.00                       |                          | 0.00                       |
      | 143       | 24 May 2026      | 50.00                 |                     | 2208.00         |               | 0.00                       |                          | 0.00                       |
      | 144       | 25 May 2026      | 50.00                 |                     | 2158.00         |               | 0.00                       |                          | 0.00                       |
      | 145       | 26 May 2026      | 50.00                 |                     | 2108.00         |               | 0.00                       |                          | 0.00                       |
      | 146       | 27 May 2026      | 50.00                 |                     | 2058.00         |               | 0.00                       |                          | 0.00                       |
      | 147       | 28 May 2026      | 50.00                 |                     | 2008.00         |               | 0.00                       |                          | 0.00                       |
      | 148       | 29 May 2026      | 50.00                 |                     | 1958.00         |               | 0.00                       |                          | 0.00                       |
      | 149       | 30 May 2026      | 50.00                 |                     | 1908.00         |               | 0.00                       |                          | 0.00                       |
      | 150       | 31 May 2026      | 50.00                 |                     | 1858.00         |               | 0.00                       |                          | 0.00                       |
      | 151       | 01 June 2026     | 50.00                 |                     | 1808.00         |               | 0.00                       |                          | 0.00                       |
      | 152       | 02 June 2026     | 50.00                 |                     | 1758.00         |               | 0.00                       |                          | 0.00                       |
      | 153       | 03 June 2026     | 50.00                 |                     | 1708.00         |               | 0.00                       |                          | 0.00                       |
      | 154       | 04 June 2026     | 50.00                 |                     | 1658.00         |               | 0.00                       |                          | 0.00                       |
      | 155       | 05 June 2026     | 50.00                 |                     | 1608.00         |               | 0.00                       |                          | 0.00                       |
      | 156       | 06 June 2026     | 50.00                 |                     | 1558.00         |               | 0.00                       |                          | 0.00                       |
      | 157       | 07 June 2026     | 50.00                 |                     | 1508.00         |               | 0.00                       |                          | 0.00                       |
      | 158       | 08 June 2026     | 50.00                 |                     | 1458.00         |               | 0.00                       |                          | 0.00                       |
      | 159       | 09 June 2026     | 50.00                 |                     | 1408.00         |               | 0.00                       |                          | 0.00                       |
      | 160       | 10 June 2026     | 50.00                 |                     | 1358.00         |               | 0.00                       |                          | 0.00                       |
      | 161       | 11 June 2026     | 50.00                 |                     | 1308.00         |               | 0.00                       |                          | 0.00                       |
      | 162       | 12 June 2026     | 50.00                 |                     | 1258.00         |               | 0.00                       |                          | 0.00                       |
      | 163       | 13 June 2026     | 50.00                 |                     | 1208.00         |               | 0.00                       |                          | 0.00                       |
      | 164       | 14 June 2026     | 50.00                 |                     | 1158.00         |               | 0.00                       |                          | 0.00                       |
      | 165       | 15 June 2026     | 50.00                 |                     | 1108.00         |               | 0.00                       |                          | 0.00                       |
      | 166       | 16 June 2026     | 50.00                 |                     | 1058.00         |               | 0.00                       |                          | 0.00                       |
      | 167       | 17 June 2026     | 50.00                 |                     | 1008.00         |               | 0.00                       |                          | 0.00                       |
      | 168       | 18 June 2026     | 50.00                 |                     | 958.00          |               | 0.00                       |                          | 0.00                       |
      | 169       | 19 June 2026     | 50.00                 |                     | 908.00          |               | 0.00                       |                          | 0.00                       |
      | 170       | 20 June 2026     | 50.00                 |                     | 858.00          |               | 0.00                       |                          | 0.00                       |
      | 171       | 21 June 2026     | 50.00                 |                     | 808.00          |               | 0.00                       |                          | 0.00                       |
      | 172       | 22 June 2026     | 50.00                 |                     | 758.00          |               | 0.00                       |                          | 0.00                       |
      | 173       | 23 June 2026     | 50.00                 |                     | 708.00          |               | 0.00                       |                          | 0.00                       |
      | 174       | 24 June 2026     | 50.00                 |                     | 658.00          |               | 0.00                       |                          | 0.00                       |
      | 175       | 25 June 2026     | 50.00                 |                     | 608.00          |               | 0.00                       |                          | 0.00                       |
      | 176       | 26 June 2026     | 50.00                 |                     | 558.00          |               | 0.00                       |                          | 0.00                       |
      | 177       | 27 June 2026     | 50.00                 |                     | 508.00          |               | 0.00                       |                          | 0.00                       |
      | 178       | 28 June 2026     | 50.00                 |                     | 458.00          |               | 0.00                       |                          | 0.00                       |
      | 179       | 29 June 2026     | 50.00                 |                     | 408.00          |               | 0.00                       |                          | 0.00                       |
      | 180       | 30 June 2026     | 50.00                 |                     | 358.00          |               | 0.00                       |                          | 0.00                       |
      | 181       | 01 July 2026     | 50.00                 |                     | 308.00          |               | 0.00                       |                          | 0.00                       |
      | 182       | 02 July 2026     | 50.00                 |                     | 258.00          |               | 0.00                       |                          | 0.00                       |
      | 183       | 03 July 2026     | 50.00                 |                     | 208.00          |               | 0.00                       |                          | 0.00                       |
      | 184       | 04 July 2026     | 50.00                 |                     | 158.00          |               | 0.00                       |                          | 0.00                       |
      | 185       | 05 July 2026     | 50.00                 |                     | 108.00          |               | 0.00                       |                          | 0.00                       |
      | 186       | 06 July 2026     | 50.00                 |                     | 58.00           |               | 0.00                       |                          | 0.00                       |
      | 187       | 07 July 2026     | 50.00                 |                     | 8.00            |               | 0.00                       |                          | 0.00                       |
      | 188       | 08 July 2026     | 8.00                  |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
