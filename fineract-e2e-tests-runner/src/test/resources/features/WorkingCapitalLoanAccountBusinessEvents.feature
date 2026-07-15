@WorkingCapital
@WorkingCapitalLoanAccountBusinessEventsFeature
Feature: Working Capital Loan Account Business Events

  Scenario: Working Capital loan raises Created, Application Modified, Approved, Undo Approval, Disbursal and Undo Disbursal business events across its lifecycle
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And a Working Capital Loan Created business event is raised
    When Admin modifies the working capital loan with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      |                 |                          | 80.0            |                    |                   |          |
    Then a Working Capital Loan Application Modified business event is raised
    When Admin successfully approves the working capital loan on "01 January 2026" with "80" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And a Working Capital Loan Approved business event is raised
    And a Working Capital Loan Status Changed business event is raised
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And a Working Capital Loan Undo Approval business event is raised
    And a Working Capital Loan Status Changed business event is raised
    When Admin successfully approves the working capital loan on "01 January 2026" with "80" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And a Working Capital Loan Approved business event is raised
    And a Working Capital Loan Status Changed business event is raised
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "80" EUR transaction amount
    Then a Working Capital Loan Disbursal business event is raised
    And a Working Capital Loan Status Changed business event is raised
    When Admin successfully undo Working Capital disbursal
    Then a Working Capital Loan Undo Disbursal business event is raised
    And a Working Capital Loan Status Changed business event is raised

  Scenario: Rejected Working Capital loan raises Rejected and Status Changed business events
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin rejects the working capital loan on "01 January 2026"
    Then Working capital loan rejection was successful
    And a Working Capital Loan Rejected business event is raised
    And a Working Capital Loan Status Changed business event is raised

  Scenario: Working Capital loan raises Balance Changed on partial repayment and Status Changed on full repayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then a Working Capital Loan Status Changed business event is raised
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Status Changed business event is raised
    And a Working Capital Loan Balance Changed business event is raised
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 4000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Balance Changed business event is raised
    When Admin sets the business date to "20 January 2026"
    And Customer makes repayment on "20 January 2026" with 5000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And a Working Capital Loan Status Changed business event is raised

  Scenario: Overpaid Working Capital loan raises Balance Changed and Status Changed business events through credit balance refund
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then a Working Capital Loan Status Changed business event is raised
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Status Changed business event is raised
    And a Working Capital Loan Balance Changed business event is raised
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "20 January 2026" with 9200.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And a Working Capital Loan Status Changed business event is raised
    And a Working Capital Loan Balance Changed business event is raised
    When Customer makes credit balance refund on "20 January 2026" with 200.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And a Working Capital Loan Status Changed business event is raised
    And a Working Capital Loan Balance Changed business event is raised

  Scenario: Delinquent Working Capital loan raises a Delinquency Range Change business event
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then a Working Capital Loan Status Changed business event is raised
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Status Changed business event is raised
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "15 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then a Working Capital Loan Delinquency Range Change business event is raised
    When Admin closes the Working Capital loan with a full repayment on "15 February 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And a Working Capital Loan Status Changed business event is raised

  Scenario: Working Capital loan raises Balance Changed on discount fee, discount fee adjustment and undo of the adjustment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then a Working Capital Loan Status Changed business event is raised
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Status Changed business event is raised
    And a Working Capital Loan Balance Changed business event is raised
    When Admin adds Discount fee with "12" amount on Working Capital loan account for last disbursement
    Then a Working Capital Loan Balance Changed business event is raised
    When Admin adds Discount fee adjustment with "5" amount on Working Capital loan account for last discount
    Then a Working Capital Loan Balance Changed business event is raised
    When Admin undo the last Discount fee adjustment on Working Capital loan account
    Then a Working Capital Loan Balance Changed business event is raised
    When Admin closes the Working Capital loan with a full repayment on "01 January 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And a Working Capital Loan Status Changed business event is raised

  @SerialChargeAccrualConfig
  Scenario: Working Capital loan Balance Changed event exposes charge amountAccrued and amountUnrecognized before and after accrual
    Given Admin sets the business date to "01 January 2028"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2028 | 01 January 2028          | 9000            | 100000       | 18                | 0        |
    And a Working Capital Loan Balance Changed business event is raised
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "05 January 2028"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "15 January 2028" due date and 50.0 transaction amount
    And Admin sets the business date to "10 January 2028"
    And Customer makes repayment on "10 January 2028" with 100.0 transaction amount on Working Capital loan
    Then a Working Capital Loan Balance Changed business event is raised with charges:
      | amount | amountAccrued | amountUnrecognized |
      | 50.0   | 0.0           | 50.0               |
    When Admin sets the business date to "16 January 2028"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Admin closes the Working Capital loan with a full repayment on "16 January 2028"
    And a Working Capital Loan Balance Changed business event is raised with charges:
      | amount | amountAccrued | amountUnrecognized |
      | 50.0   | 50.0          | 0.0                |
