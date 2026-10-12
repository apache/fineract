@WorkingCapital
@WorkingCapitalLoanAccountBusinessEventsFeature
@SerialChargeAccrualConfig
Feature: Working Capital Loan Account Business Events

  @TestRailId:C89791
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
    And no Working Capital Loan Status Changed business event is raised
    When Admin successfully approves the working capital loan on "01 January 2026" with "80" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And a Working Capital Loan Approved business event is raised
    And a Working Capital Loan Balance Changed business event is raised on approval
    And a Working Capital Loan Status Changed business event is raised
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    And a Working Capital Loan Undo Approval business event is raised
    And a Working Capital Loan Balance Changed business event is raised on undo approval
    And a Working Capital Loan Status Changed business event is raised
    When Admin successfully approves the working capital loan on "01 January 2026" with "80" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And a Working Capital Loan Approved business event is raised
    And a Working Capital Loan Status Changed business event is raised
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "80" EUR transaction amount
    Then a Working Capital Loan Disbursal business event is raised with loan details matching the API
    And a Working Capital Loan Status Changed business event is raised
    When Admin successfully undo Working Capital disbursal
    Then a Working Capital Loan Undo Disbursal business event is raised
    And a Working Capital Loan Status Changed business event is raised

  @TestRailId:C89792
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

  @TestRailId:C89793
  Scenario: Working Capital loan raises Balance Changed on partial repayment and Status Changed on full repayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
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
    And no Working Capital Loan Status Changed business event is raised
    When Admin sets the business date to "20 January 2026"
    And Customer makes repayment on "20 January 2026" with 5000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And a Working Capital Loan Status Changed business event is raised with timeline matching the API
    And a Working Capital Loan Balance Changed business event is raised with transaction type totals:
      | totalRepaymentTransaction | totalRepaymentTransactionReversed |
      | 9000.0                    | 0.0                               |

  @TestRailId:C89794
  Scenario: Overpaid Working Capital loan raises Balance Changed and Status Changed business events through credit balance refund
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
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
    And a Working Capital Loan Balance Changed business event is raised with transaction type totals:
      | totalRepaymentTransaction | totalCreditBalanceRefund | totalCreditBalanceRefundReversed | totalPayment | totalPaymentReversed |
      | 9200.0                    | 200.0                    | 0.0                              | 9200.0       | 0.0                  |

  @TestRailId:C89795
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
    Then a Working Capital Loan Delinquency Range Change business event is raised naming the delinquency range
    When Admin closes the Working Capital loan with a full repayment on "15 February 2026"
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And a Working Capital Loan Status Changed business event is raised
    When Admin sets the business date to "16 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then a Working Capital Loan Delinquency Range Change business event is raised

  @TestRailId:C89796
  Scenario: Working Capital loan raises Balance Changed on discount fee, discount fee adjustment and undo of the adjustment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
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

  @TestRailId:C89797
  Scenario: Working Capital loan Balance Changed event exposes charge amountAccrued and amountUnrecognized before and after accrual
    Given Admin sets the business date to "01 January 2028"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2028 | 01 January 2028          | 9000            | 100000             | 18                | 0        |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2028" with "9000" amount and expected disbursement date on "01 January 2028"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2028" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Balance Changed business event is raised
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "05 January 2028"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "15 January 2028" due date and 50.0 transaction amount
    And a Working Capital Loan Balance Changed business event is raised
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

  @TestRailId:C89798
  Scenario: Working Capital loan Balance Changed events carry payout refund, goodwill credit and reversal totals in the summary
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Balance Changed business event is raised
    When Admin sets the business date to "05 January 2026"
    And Customer makes "PAYOUT_REFUND" transaction on "05 January 2026" with 1000.0 transaction amount on Working Capital loan
    Then a Working Capital Loan Balance Changed business event is raised with transaction type totals:
      | totalPayoutRefund | totalPayoutRefundReversed | totalGoodwillCredit | totalRepaymentTransaction | totalPayment | totalPaymentReversed |
      | 1000.0            | 0.0                       | 0.0                 | 0.0                       | 1000.0       | 0.0                  |
    When Customer makes "GOODWILL_CREDIT" transaction on "05 January 2026" with 500.0 transaction amount on Working Capital loan
    Then a Working Capital Loan Balance Changed business event is raised with transaction type totals:
      | totalPayoutRefund | totalGoodwillCredit | totalGoodwillCreditReversed | totalPayment | totalPaymentReversed |
      | 1000.0            | 500.0               | 0.0                         | 1500.0       | 0.0                  |
    When Customer undo "1"th "PAYOUT_REFUND" transaction made on "05 January 2026" on Working Capital loan
    Then a Working Capital Loan Balance Changed business event is raised with transaction type totals:
      | totalPayoutRefund | totalPayoutRefundReversed | totalGoodwillCredit | totalPayment | totalPaymentReversed |
      | 0.0               | 1000.0                    | 500.0               | 500.0        | 1000.0               |
    When Admin closes the Working Capital loan with a full repayment on "05 January 2026"

  @TestRailId:C89799
  Scenario: Undoing a closing repayment reopens the Working Capital loan and raises Status Changed and Balance Changed business events
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    Then a Working Capital Loan Status Changed business event is raised
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Status Changed business event is raised
    And a Working Capital Loan Balance Changed business event is raised
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And a Working Capital Loan Status Changed business event is raised with timeline matching the API
    And a Working Capital Loan Balance Changed business event is raised with transaction type totals:
      | totalRepaymentTransaction | totalRepaymentTransactionReversed |
      | 100.0                     | 0.0                               |
    When Customer undo "1"th "REPAYMENT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Status Changed business event is raised
    And a Working Capital Loan Balance Changed business event is raised with transaction type totals:
      | totalRepaymentTransaction | totalRepaymentTransactionReversed |
      | 0.0                       | 100.0                             |
    When Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C89800
  Scenario: Working Capital loan Balance Changed event exposes the delinquency pause periods
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Balance Changed business event is raised
    When Admin runs inline COB job for Working Capital Loan by loanId
    And Admin initiate a Working Capital loan delinquency pause with startDate "01 January 2026" and endDate "15 January 2026"
    And Admin sets the business date to "06 January 2026"
    And Customer makes repayment on "06 January 2026" with 100.0 transaction amount on Working Capital loan
    Then a Working Capital Loan Balance Changed business event is raised with the delinquency pause periods
    When Admin closes the Working Capital loan with a full repayment on "06 January 2026"

  @TestRailId:C89801
  Scenario: Working Capital loan Balance Changed event leaves charge accrual fields empty for products without deferred revenue accrual accounting
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Balance Changed business event is raised
    When Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "15 January 2026" due date and 50.0 transaction amount
    And Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 100.0 transaction amount on Working Capital loan
    Then a Working Capital Loan Balance Changed business event is raised where charge accrual fields are not populated
    When Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C89802
  Scenario: Adding a charge to a closed Working Capital loan reopens it and raises Status Changed and Balance Changed business events
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    Then a Working Capital Loan Status Changed business event is raised
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Status Changed business event is raised
    And a Working Capital Loan Balance Changed business event is raised
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And a Working Capital Loan Status Changed business event is raised
    And a Working Capital Loan Balance Changed business event is raised
    When Admin sets the business date to "10 January 2026"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 January 2026" due date and 50.0 transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Status Changed business event is raised
    And a Working Capital Loan Balance Changed business event is raised
    When Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C89803
  Scenario: A charge adjustment that settles the outstanding charge closes the Working Capital loan and raises Status Changed and Balance Changed business events
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    Then a Working Capital Loan Status Changed business event is raised
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Status Changed business event is raised
    And a Working Capital Loan Balance Changed business event is raised
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And a Working Capital Loan Status Changed business event is raised
    And a Working Capital Loan Balance Changed business event is raised
    When Admin sets the business date to "10 January 2026"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 January 2026" due date and 50.0 transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Status Changed business event is raised
    And a Working Capital Loan Balance Changed business event is raised
    When Admin makes a charge adjustment for the last added charge with 50.0 amount on working capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And a Working Capital Loan Status Changed business event is raised
    And a Working Capital Loan Balance Changed business event is raised

  @TestRailId:C89804
  Scenario: A partial charge adjustment on an active Working Capital loan raises Balance Changed without Status Changed
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    Then a Working Capital Loan Status Changed business event is raised
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Status Changed business event is raised
    And a Working Capital Loan Balance Changed business event is raised
    When Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "15 January 2026" due date and 50.0 transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Balance Changed business event is raised
    And no Working Capital Loan Status Changed business event is raised
    When Admin makes a charge adjustment for the last added charge with 20.0 amount on working capital loan
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Balance Changed business event is raised
    And no Working Capital Loan Status Changed business event is raised
    When Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C89805
  Scenario: Undoing a discount fee adjustment that closed the Working Capital loan reopens it and raises Status Changed and Balance Changed business events
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    Then a Working Capital Loan Status Changed business event is raised
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Status Changed business event is raised
    And a Working Capital Loan Balance Changed business event is raised
    When Admin adds Discount fee with "12" amount on Working Capital loan account for last disbursement
    Then a Working Capital Loan Balance Changed business event is raised
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Balance Changed business event is raised
    When Admin adds Discount fee adjustment with "12" amount on Working Capital loan account for last discount
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And a Working Capital Loan Status Changed business event is raised
    And a Working Capital Loan Balance Changed business event is raised
    When Admin undo the last Discount fee adjustment on Working Capital loan account
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Status Changed business event is raised
    And a Working Capital Loan Balance Changed business event is raised

  @TestRailId:C94063
  Scenario: Working Capital loan raises Balance Changed business event on approval
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    And a Working Capital Loan Created business event is raised
    When Admin successfully approves the working capital loan on "01 January 2026" with "80" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And a Working Capital Loan Approved business event is raised
    And a Working Capital Loan Balance Changed business event is raised on approval
    And a Working Capital Loan Status Changed business event is raised
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "80" EUR transaction amount
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"
