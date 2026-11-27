@LoanChargeAmortizationFeature
Feature: Charge amortization configuration on the loan charge

  @TestRailId:C127968
  Scenario: Verify loan charge amortization configuration - UC1: configuration is copied onto the loan charge and frozen
    When Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product and adds the amortized charge
    Then The loan charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |
    When Admin updates the amortization configuration of the charge to:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | EFFECTIVE_RATE              | FEE                    |
    Then The charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | EFFECTIVE_RATE              | FEE                    |
    And The loan charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |

  @TestRailId:C127969
  Scenario: Verify loan charge amortization configuration - UC2: amortized charge cannot be added to a loan whose product has the flag disabled
    When Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  |                             |                        |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | false                    | false                         | false        |
    And Admin creates a client with random data
    Then Adding the amortized charge to a new Loan with the charge amortization product results an error:
      | httpCode | errorMessage                                                              |
      | 403      | can be added only to a loan whose product has charge amortization enabled |

  @TestRailId:C127970
  Scenario: Verify loan charge amortization configuration - UC3: non-amortized charge can still be added when the flag is disabled
    When Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      |                        |                  |                             |                        |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | false                    | false                         | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product and adds the amortized charge
    Then The loan charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NONE                   |                  |                             |                        |

  @TestRailId:C127971
  Scenario: Verify loan charge amortization configuration - UC4: modify loan application does not revalidate existing loan charges against an updated charge definition
    When Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      |                        |                  |                             |                        |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | false                    | false                         | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product and adds the amortized charge
    Then The loan charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NONE                   |                  |                             |                        |
    When Admin updates the amortization configuration of the charge to:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  |                             |                        |
    And Admin modifies the loan resending its existing charges
    Then The loan charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NONE                   |                  |                             |                        |

  @TestRailId:C127972
  Scenario: Verify loan charge amortization configuration - UC5: modify loan application keeps the frozen configuration when the charge definition is switched back to NONE
    When Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product and adds the amortized charge
    Then The loan charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |
    When Admin updates the amortization configuration of the charge to:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NONE                   |                  |                             |                        |
    Then The charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NONE                   |                  |                             |                        |
    When Admin modifies the loan resending its existing charges
    Then The loan charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |

  @TestRailId:C127973
  Scenario: Verify loan charge amortization configuration - UC6: default calculation and income types are copied onto the loan charge
    When Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  |                             |                        |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product and adds the amortized charge
    Then The loan charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | FLAT                        | FEE                    |

  @TestRailId:C127974
  Scenario: Verify loan charge amortization configuration - UC7: amortized charge in the loan application is copied and validated against the flag
    When Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product and the amortized charge in the loan application
    Then The loan charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |
    When Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | false                    | false                         | false        |
    Then Creating a new Loan with charge amortization product and the amortized charge in the loan application results an error:
      | httpCode | errorMessage                                                              |
      | 400      | can be added only to a loan whose product has charge amortization enabled |

  @TestRailId:C127975
  Scenario: Verify loan charge amortization configuration - UC8: amortized charge can be added to a disbursed loan and stays frozen
    When Admin sets the business date to "01 January 2024"
    And Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product on "01 January 2024"
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin disburses the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin adds the amortized charge to the loan on "15 January 2024"
    Then The loan charge has the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | NUMBER_OF_DAYS         | 30               | FLAT                        | INTEREST               |

  Scenario: Verify Deferred Charge is created for an amortized disbursement fee on disbursement
    When Admin sets the business date to "01 January 2026"
    And Admin creates a loan fee with "DISBURSEMENT" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | FLAT                        | FEE                    |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product and the amortized charge on "01 January 2026" with principal "1000"
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    And Loan Charges tab has the amortized charge with the following data:
      | Name | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      |      | false     | Disbursement   |           | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    And Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |           | 1000.0          |               |          | 10.0 |           | 10.0   | 10.0 |            |      |             |
      | 1  | 30   | 31 January 2026  |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 10.0 | 0.0        | 0.0  | 1000.0      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                        | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 January 2026  | Deferred Charge                     | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          | false    |
      | 01 January 2026  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 1000.0       | false    |
    And LoanDeferredChargeTransactionCreatedBusinessEvent is created on "01 January 2026"
    When Customer makes "AUTOPAY" repayment on "01 January 2026" with 1000 EUR transaction amount
    Then Loan has 0.0 outstanding amount
    And Loan has 0.0 overpaid amount
    And Loan status will be "CLOSED_OBLIGATIONS_MET"

  Scenario: Verify Deferred Charge is created for an amortized specified due date charge when due is on or before business date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | FLAT                        | FEE                    |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product on "01 January 2026" with principal "1000"
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    And Admin adds the amortized charge to the loan with due date "01 January 2026"
    Then Loan status will be "ACTIVE"
    And Loan Charges tab has the amortized charge with the following data:
      | Name | isPenalty | Payment due at      | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      |      | false     | Specified due date  | 01 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    And Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2026  |           | 0.0             | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 January 2026  | Deferred Charge  | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          | false    |
    And LoanDeferredChargeTransactionCreatedBusinessEvent is created on "01 January 2026"
    When Customer makes "AUTOPAY" repayment on "01 January 2026" with 1010 EUR transaction amount
    Then Loan has 0.0 outstanding amount
    And Loan has 0.0 overpaid amount
    And Loan status will be "CLOSED_OBLIGATIONS_MET"

  Scenario: Verify Deferred Charge is created per tranche for an amortized tranche disbursement fee
    When Admin sets the business date to "01 January 2026"
    And Admin creates a loan fee with "TRANCHE_DISBURSEMENT" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | FLAT                        | FEE                    |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge | multiDisburse |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        | true          |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product, the amortized charge and two expected tranches on "01 January 2026" and "15 January 2026" with principal "1000"
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "500" EUR transaction amount
    Then Loan status will be "ACTIVE"
    And Loan Charges tab has the amortized charge with the following data:
      | Name | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      |      | false     | Tranche Disbursement | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                        | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    |
      | 01 January 2026  | Deferred Charge                     | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          | false    |
      | 01 January 2026  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 500.0        | false    |
    And LoanDeferredChargeTransactionCreatedBusinessEvent is created on "01 January 2026"
    When Admin sets the business date to "15 January 2026"
    And Admin successfully disburse the loan on "15 January 2026" with "500" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |           | 500.0           |               |          | 10.0 |           | 10.0   | 10.0 |            |      |             |
      |    |      | 15 January 2026  |           | 500.0           |               |          | 10.0 |           | 10.0   | 10.0 |            |      |             |
      | 1  | 30   | 31 January 2026  |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 20.0 | 0.0        | 0.0  | 1000.0      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                        | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    |
      | 01 January 2026  | Deferred Charge                     | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          | false    |
      | 01 January 2026  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 500.0        | false    |
      | 15 January 2026  | Disbursement                        | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 15 January 2026  | Deferred Charge                     | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          | false    |
      | 15 January 2026  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 1000.0       | false    |
    And LoanDeferredChargeTransactionCreatedBusinessEvent is created on "15 January 2026"
    When Admin successfully undo last disbursal
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                        | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    |
      | 01 January 2026  | Deferred Charge                     | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          | false    |
      | 01 January 2026  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 500.0        | false    |

  Scenario: Verify Deferred Charge is created for an application-time specified due date charge on disbursement
    When Admin sets the business date to "01 January 2026"
    And Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | FLAT                        | FEE                    |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product and the amortized specified due date charge due on "01 January 2026" submitted on "01 January 2026" with principal "1000"
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 January 2026  | Deferred Charge  | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          | false    |
    And LoanDeferredChargeTransactionCreatedBusinessEvent is created on "01 January 2026"
    When Admin successfully undo disbursal
    Then Loan status will be "APPROVED"
    And Loan Transactions tab has none transaction
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 January 2026  | Deferred Charge  | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          | false    |
    When Customer makes "AUTOPAY" repayment on "01 January 2026" with 1010 EUR transaction amount
    Then Loan has 0.0 outstanding amount
    And Loan has 0.0 overpaid amount
    And Loan status will be "CLOSED_OBLIGATIONS_MET"

  Scenario: Verify Deferred Charge is not created for a future specified due date amortized charge
    When Admin sets the business date to "01 January 2026"
    And Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | FLAT                        | FEE                    |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product on "01 January 2026" with principal "1000"
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    And Admin adds the amortized charge to the loan with due date "15 January 2026"
    Then Loan status will be "ACTIVE"
    And Loan Charges tab has the amortized charge with the following data:
      | Name | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      |      | false     | Specified due date | 15 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    And Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2026  |           | 0.0             | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    When Customer makes "AUTOPAY" repayment on "01 January 2026" with 1010 EUR transaction amount
    Then Loan has 0.0 outstanding amount
    And Loan has 0.0 overpaid amount
    And Loan status will be "CLOSED_OBLIGATIONS_MET"

  Scenario: Verify undo disbursement clears Deferred Charge transactions
    When Admin sets the business date to "01 January 2026"
    And Admin creates a loan fee with "DISBURSEMENT" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | FLAT                        | FEE                    |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product and the amortized charge on "01 January 2026" with principal "1000"
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                        | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 January 2026  | Deferred Charge                     | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          | false    |
      | 01 January 2026  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 1000.0       | false    |
    When Admin successfully undo disbursal
    Then Loan status will be "APPROVED"
    And Loan Transactions tab has none transaction

  Scenario: Verify a non-amortized charge does not create a Deferred Charge transaction
    When Admin sets the business date to "01 January 2026"
    And Admin creates a loan fee with "DISBURSEMENT" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      |                        |                  |                             |                        |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | false                    | false                         | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product and the amortized charge on "01 January 2026" with principal "1000"
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    And Loan Charges tab has the amortized charge with the following data:
      | Name | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      |      | false     | Disbursement   |           | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    And Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |           | 1000.0          |               |          | 10.0 |           | 10.0   | 10.0 |            |      |             |
      | 1  | 30   | 31 January 2026  |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 10.0 | 0.0        | 0.0  | 1000.0      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                        | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 January 2026  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 1000.0       | false    |
    When Customer makes "AUTOPAY" repayment on "01 January 2026" with 1000 EUR transaction amount
    Then Loan has 0.0 outstanding amount
    And Loan has 0.0 overpaid amount
    And Loan status will be "CLOSED_OBLIGATIONS_MET"

  Scenario: Verify a non-amortized specified due date charge of a cumulative upfront accrual loan is still accrued on disbursement
    When Admin sets the business date to "01 January 2026"
    And Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      |                        |                  |                             |                        |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule  | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | CUMULATIVE       | ACCRUAL_UPFRONT | false                    | false                         | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product and the amortized specified due date charge due on "15 January 2026" submitted on "01 January 2026" with principal "1000"
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                        | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 January 2026  | Accrual                             | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "ACCRUAL" transaction with date "01 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
    When Customer makes "AUTOPAY" repayment on "01 January 2026" with 1010 EUR transaction amount
    Then Loan has 0.0 outstanding amount
    And Loan has 0.0 overpaid amount
    And Loan status will be "CLOSED_OBLIGATIONS_MET"

  Scenario: Verify undo last disbursal of a same-day tranche reverses only the Deferred Charge of that tranche
    When Admin sets the business date to "01 January 2026"
    And Admin creates a loan fee with "TRANCHE_DISBURSEMENT" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | FLAT                        | FEE                    |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge | multiDisburse |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        | true          |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product, the amortized charge and two expected tranches on "01 January 2026" and "01 January 2026" with principal "1000"
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "500" EUR transaction amount
    And Admin successfully disburse the loan on "01 January 2026" with "500" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                        | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    |
      | 01 January 2026  | Deferred Charge                     | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          | false    |
      | 01 January 2026  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 500.0        | false    |
      | 01 January 2026  | Disbursement                        | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 January 2026  | Deferred Charge                     | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          | false    |
      | 01 January 2026  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 1000.0       | false    |
    When Admin successfully undo last disbursal
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                        | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    |
      | 01 January 2026  | Deferred Charge                     | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          | false    |
      | 01 January 2026  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 500.0        | false    |

  Scenario: Verify undo last disbursal keeps the Deferred Charge of a specified due date charge added after the last tranche
    When Admin sets the business date to "01 January 2026"
    And Admin creates a loan fee with "TRANCHE_DISBURSEMENT" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | FLAT                        | FEE                    |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge | multiDisburse |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        | true          |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product, the amortized charge and two expected tranches on "01 January 2026" and "10 January 2026" with principal "2000"
    And Admin successfully approves the loan on "01 January 2026" with "2000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Admin successfully disburse the loan on "10 January 2026" with "1000" EUR transaction amount
    And Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | FLAT                        | FEE                    |
    And Admin adds the amortized charge to the loan with due date "10 January 2026"
    When Admin successfully undo last disbursal
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                        | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 January 2026  | Deferred Charge                     | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          | false    |
      | 01 January 2026  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 1000.0       | false    |
      | 10 January 2026  | Deferred Charge                     | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          | false    |

  Scenario: Verify Deferred Charge of a specified due date charge added before disbursement is created on disbursement
    When Admin sets the business date to "10 January 2026"
    And Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | FLAT                        | FEE                    |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product on "01 January 2026" with principal "1000"
    And Admin adds the amortized charge to the loan with due date "05 January 2026"
    Then Loan Transactions tab has none transaction
    When Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                        | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 05 January 2026  | Deferred Charge                     | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          | false    |
    And LoanDeferredChargeTransactionCreatedBusinessEvent is created on "05 January 2026"
    When Customer makes "AUTOPAY" repayment on "10 January 2026" with 1010 EUR transaction amount
    Then Loan has 0.0 outstanding amount
    And Loan has 0.0 overpaid amount
    And Loan status will be "CLOSED_OBLIGATIONS_MET"

  Scenario: Verify Deferred Charge is created on the due date for a backdated specified due date charge
    When Admin sets the business date to "10 January 2026"
    And Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | FLAT                        | FEE                    |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product on "01 January 2026" with principal "1000"
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    And Admin adds the amortized charge to the loan with due date "05 January 2026"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                        | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 05 January 2026  | Deferred Charge                     | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          | false    |
    And LoanDeferredChargeTransactionCreatedBusinessEvent is created on "05 January 2026"
    When Customer makes "AUTOPAY" repayment on "10 January 2026" with 1010 EUR transaction amount
    Then Loan has 0.0 outstanding amount
    And Loan has 0.0 overpaid amount
    And Loan status will be "CLOSED_OBLIGATIONS_MET"

  Scenario: Verify Deferred Charge is not created for a specified due date charge waived before its due date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a loan fee with "TRANCHE_DISBURSEMENT" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | FLAT                        | FEE                    |
    And Admin creates a loan product with the following charge amortization settings:
      | loanScheduleType | accountingRule   | enableChargeAmortization | deferredIncomeLiabilityMapped | attachCharge | multiDisburse |
      | PROGRESSIVE      | ACCRUAL_PERIODIC | true                     | true                          | false        | true          |
    And Admin creates a client with random data
    And Admin creates a new Loan with charge amortization product, the amortized charge and two expected tranches on "01 January 2026" and "20 January 2026" with principal "2000"
    And Admin successfully approves the loan on "01 January 2026" with "2000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    And Admin creates a loan fee with "SPECIFIED_DUE_DATE" charge time type and the following amortization configuration:
      | amortizationPeriodType | amortizationDays | amortizationCalculationType | amortizationIncomeType |
      | FULL_LOAN_TERM         |                  | FLAT                        | FEE                    |
    And Admin adds the amortized charge to the loan with due date "15 January 2026"
    And Admin waives the added amortized charge
    When Admin sets the business date to "20 January 2026"
    And Admin successfully disburse the loan on "20 January 2026" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                        | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 January 2026  | Deferred Charge                     | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          | false    |
      | 01 January 2026  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 1000.0       | false    |
      | 01 January 2026  | Waive loan charges                  | 10.0   | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 20 January 2026  | Disbursement                        | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2000.0       | false    |
      | 20 January 2026  | Deferred Charge                     | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          | false    |
      | 20 January 2026  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 2000.0       | false    |
