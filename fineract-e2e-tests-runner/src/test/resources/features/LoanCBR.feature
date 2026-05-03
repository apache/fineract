@LoanCBR
Feature: Credit Balance Refund

  @TestRailId:C2505
  Scenario: Verify that Loan status goes from overpaid to active in case of CBR transaction (with replaying when CBR>new balance → clears overpaid, remaining increasing loan balance)
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "03 January 2023"
    And Customer makes "AUTOPAY" repayment on "03 January 2023" with 100 EUR transaction amount
    When Admin sets the business date to "05 January 2023"
    And Customer makes "AUTOPAY" repayment on "05 January 2023" with 500 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "05 January 2023" with 600 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 200 overpaid amount
    When Admin sets the business date to "07 January 2023"
    When Admin makes Credit Balance Refund transaction on "07 January 2023" with 200 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Customer undo "1"th repayment on "3 January 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 100 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "03 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 100.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | l1           | Overpayment account       |       | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 600.0 |        |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "07 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | l1           | Overpayment account       | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 200.0  |

  @TestRailId:C2511
  Scenario: Verify that Loan status goes from overpaid to closed in case of CBR transaction when transaction amount equals overpaid amount
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "3 January 2023"
    And Customer makes "AUTOPAY" repayment on "3 January 2023" with 450 EUR transaction amount
    When Admin sets the business date to "5 January 2023"
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 450 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 300 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 200 overpaid amount
    When Admin makes Credit Balance Refund transaction on "5 January 2023" with 200 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "03 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 450.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 450.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 450.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 450.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |
      | LIABILITY | l1           | Overpayment account       |       | 200.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 300.0 |        |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 200.0  |

  @TestRailId:C2515
  Scenario: Single repayment reversal
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 1200 EUR transaction amount
    When Admin sets the business date to "11 January 2023"
    And Admin makes Credit Balance Refund transaction on "11 January 2023" with 200 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Customer undo "1"th repayment on "10 January 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 1200 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | l1           | Overpayment account       |        | 200.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 1200.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | l1           | Overpayment account       | 200.0  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1200.0 |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "11 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 200.0  |


  @TestRailId:C2516
  Scenario: Multi repayment reversal
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 500 EUR transaction amount
    When Admin sets the business date to "11 January 2023"
    And Customer makes "AUTOPAY" repayment on "11 January 2023" with 700 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan has 200 overpaid amount
    When Admin sets the business date to "13 January 2023"
    And Admin makes Credit Balance Refund transaction on "13 January 2023" with 200 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Customer undo "1"th repayment on "10 January 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 500 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 500.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "11 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 700.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 700.0 |        |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "13 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 200.0  |

  @TestRailId:C2517
  Scenario: Overpaid paid portion
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 100 EUR transaction amount
    When Admin sets the business date to "11 January 2023"
    And Customer makes "AUTOPAY" repayment on "11 January 2023" with 1100 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan has 200 overpaid amount
    When Admin sets the business date to "13 January 2023"
    And Admin makes Credit Balance Refund transaction on "13 January 2023" with 200 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Customer undo "1"th repayment on "10 January 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 100 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 100.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "11 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | l1           | Overpayment account       |        | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 1100.0 |        |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "13 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | l1           | Overpayment account       | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 200.0  |

  @TestRailId:C2518
  Scenario: Repayment reversal
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 1000 EUR transaction amount
    When Admin sets the business date to "11 January 2023"
    And Admin makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "11 January 2023" with 1000 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan has 1000 overpaid amount
    When Admin sets the business date to "13 January 2023"
    And Admin makes Credit Balance Refund transaction on "13 January 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Customer undo "1"th repayment on "10 January 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 1000 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "11 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "13 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |

  @TestRailId:C2519
  Scenario: Refund reversal
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 1000 EUR transaction amount
    When Admin sets the business date to "11 January 2023"
    And Refund happens on "11 January 2023" with 1000 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan has 1000 overpaid amount
    When Admin sets the business date to "13 January 2023"
    And Admin makes Credit Balance Refund transaction on "13 January 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Refund undo happens on "13 January 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 1000 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "11 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | l1           | Overpayment account       |        | 1000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
      | LIABILITY | l1           | Overpayment account       | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "13 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |

  @TestRailId:C2520
  Scenario: Partial refund reversal
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 1000 EUR transaction amount
    When Admin sets the business date to "11 January 2023"
    And Refund happens on "11 January 2023" with 500 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan has 500 overpaid amount
    When Admin sets the business date to "13 January 2023"
    And Admin makes Credit Balance Refund transaction on "13 January 2023" with 500 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Refund undo happens on "13 January 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 500 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "11 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
      | LIABILITY | l1           | Overpayment account       | 500.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 500.0  |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "13 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 500.0  |

  @TestRailId:C2521
  Scenario: Chargeback after CBR
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 500 EUR transaction amount
    When Admin sets the business date to "11 January 2023"
    And Customer makes "AUTOPAY" repayment on "11 January 2023" with 700 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan has 200 overpaid amount
    When Admin sets the business date to "13 January 2023"
    And Admin makes Credit Balance Refund transaction on "13 January 2023" with 200 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "15 January 2023"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 500 EUR transaction amount for Payment nr. 1
    Then Loan status will be "ACTIVE"
    Then Loan has 500 outstanding amount
    When Customer undo "2"th repayment on "11 January 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 1200 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "11 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | l1           | Overpayment account       |       | 200.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 700.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
      | LIABILITY | l1           | Overpayment account       | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 700.0  |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "13 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 200.0  |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "15 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 500.0  |

  @TestRailId:C2522
  Scenario: Refund after CBR scenario
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 500 EUR transaction amount
    When Admin sets the business date to "11 January 2023"
    And Customer makes "AUTOPAY" repayment on "11 January 2023" with 700 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan has 200 overpaid amount
    When Admin sets the business date to "13 January 2023"
    And Admin makes Credit Balance Refund transaction on "13 January 2023" with 200 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "15 January 2023"
    And Refund happens on "15 January 2023" with 500 EUR transaction amount
    When Admin sets the business date to "17 January 2023"
    And Admin makes Credit Balance Refund transaction on "17 January 2023" with 500 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Customer undo "2"th repayment on "11 January 2023"
    Then Loan has 700 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "11 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | l1           | Overpayment account       |       | 200.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 700.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
      | LIABILITY | l1           | Overpayment account       | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 700.0  |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "13 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 200.0  |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "15 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "17 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 500.0  |

  @TestRailId:C2523
  Scenario: Single repayment reversal (after maturity)
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 1200 EUR transaction amount
    When Admin sets the business date to "11 February 2023"
    And Admin makes Credit Balance Refund transaction on "11 February 2023" with 200 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Customer undo "1"th repayment on "1 February 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 1200 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | l1           | Overpayment account       |        | 200.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 1200.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | l1           | Overpayment account       | 200.0  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1200.0 |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "11 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 200.0  |

  @TestRailId:C2524
  Scenario: Multi repayment reversal (after maturity)
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 500 EUR transaction amount
    When Admin sets the business date to "2 February 2023"
    And Customer makes "AUTOPAY" repayment on "2 February 2023" with 700 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan has 200 overpaid amount
    When Admin sets the business date to "11 February 2023"
    And Admin makes Credit Balance Refund transaction on "11 February 2023" with 200 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Customer undo "1"th repayment on "1 February 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 500 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 500.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "02 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 700.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 700.0 |        |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "11 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 200.0  |

  @TestRailId:C2525
  Scenario: Overpaid paid portion (after maturity)
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 100 EUR transaction amount
    When Admin sets the business date to "2 February 2023"
    And Customer makes "AUTOPAY" repayment on "2 February 2023" with 1100 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan has 200 overpaid amount
    When Admin sets the business date to "11 February 2023"
    And Admin makes Credit Balance Refund transaction on "11 February 2023" with 200 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Customer undo "1"th repayment on "1 February 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 100 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 100.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "02 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | l1           | Overpayment account       |        | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 1100.0 |        |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "11 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | l1           | Overpayment account       | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 200.0  |

  @TestRailId:C2526
  Scenario: Repayment reversal (after maturity)
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 1000 EUR transaction amount
    When Admin sets the business date to "10 February 2023"
    And Admin makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "10 February 2023" with 1000 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan has 1000 overpaid amount
    When Admin sets the business date to "11 February 2023"
    And Admin makes Credit Balance Refund transaction on "11 February 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Customer undo "1"th repayment on "1 February 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 1000 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "10 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "11 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |

  @TestRailId:C2527
  Scenario: Refund reversal (after maturity)
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 1000 EUR transaction amount
    When Admin sets the business date to "10 February 2023"
    And Refund happens on "10 February 2023" with 1000 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan has 1000 overpaid amount
    When Admin sets the business date to "11 February 2023"
    And Admin makes Credit Balance Refund transaction on "11 February 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Refund undo happens on "10 February 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 1000 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "10 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | l1           | Overpayment account       |        | 1000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
      | LIABILITY | l1           | Overpayment account       | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "11 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |

  @TestRailId:C2528
  Scenario: Partial refund reversal (after maturity)
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 1000 EUR transaction amount
    When Admin sets the business date to "10 February 2023"
    And Refund happens on "10 February 2023" with 500 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan has 500 overpaid amount
    When Admin sets the business date to "11 February 2023"
    And Admin makes Credit Balance Refund transaction on "11 February 2023" with 500 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Refund undo happens on "10 February 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 500 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "10 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
      | LIABILITY | l1           | Overpayment account       | 500.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 500.0  |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "11 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 500.0  |

  @TestRailId:C2529
  Scenario: Chargeback after CBR (after maturity)
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 500 EUR transaction amount
    When Admin sets the business date to "2 February 2023"
    And Customer makes "AUTOPAY" repayment on "2 February 2023" with 700 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan has 200 overpaid amount
    When Admin sets the business date to "11 February 2023"
    And Admin makes Credit Balance Refund transaction on "11 February 2023" with 200 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "15 February 2023"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 500 EUR transaction amount for Payment nr. 1
    Then Loan status will be "ACTIVE"
    Then Loan has 500 outstanding amount
    When Customer undo "2"th repayment on "2 February 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 1200 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "02 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | l1           | Overpayment account       |       | 200.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 700.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
      | LIABILITY | l1           | Overpayment account       | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 700.0  |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "11 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 200.0  |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "15 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 500.0  |

  @TestRailId:C2530
  Scenario: Refund after CBR scenario (after maturity)
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 500 EUR transaction amount
    When Admin sets the business date to "2 February 2023"
    And Customer makes "AUTOPAY" repayment on "2 February 2023" with 700 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan has 200 overpaid amount
    When Admin sets the business date to "11 February 2023"
    And Admin makes Credit Balance Refund transaction on "11 February 2023" with 200 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "15 February 2023"
    And Refund happens on "15 February 2023" with 500 EUR transaction amount
    When Admin sets the business date to "17 February 2023"
    And Admin makes Credit Balance Refund transaction on "17 February 2023" with 500 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Customer undo "2"th repayment on "2 February 2023"
    Then Loan has 700 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "02 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | l1           | Overpayment account       |       | 200.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 700.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
      | LIABILITY | l1           | Overpayment account       | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 700.0  |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "11 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 200.0  |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "15 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "17 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 500.0  |

  @TestRailId:C2841
  Scenario: Verify that accruals created for charges after CBR post-maturity
    When Admin sets the business date to "01 July 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 July 2023"
    And Admin successfully approves the loan on "01 July 2023" with "1000" amount and expected disbursement date on "01 July 2023"
    And Admin successfully disburse the loan on "01 July 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "31 July 2023"
    And Customer makes "AUTOPAY" repayment on "31 July 2023" with 1000 EUR transaction amount
    When Admin sets the business date to "01 August 2023"
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "01 August 2023" with 200 EUR transaction amount and system-generated Idempotency key
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "02 August 2023"
    And Admin makes Credit Balance Refund transaction on "02 August 2023" with 200 EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 July 2023 |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 31 July 2023 | 31 July 2023 | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 July 2023     | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 31 July 2023     | Repayment              | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
      | 01 August 2023   | Merchant Issued Refund | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |
      | 02 August 2023   | Credit Balance Refund  | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "03 August 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "03 August 2023" due date and 10 EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "04 August 2023"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 July 2023   |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 31 July 2023   | 31 July 2023 | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 3    | 03 August 2023 |              | 0.0             | 200.0         | 0.0      | 10.0 | 0.0       | 210.0  | 200.0  | 200.0      | 0.0  | 10.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1200.0        | 0.0      | 10.0 | 0.0       | 1210.0 | 1200.0 | 200.0      | 0.0  | 10.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 July 2023     | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 31 July 2023     | Repayment              | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
      | 01 August 2023   | Merchant Issued Refund | 200.0  | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 02 August 2023   | Credit Balance Refund  | 200.0  | 10.0      | 0.0      | 0.0  | 0.0       | 10.0         |
      | 03 August 2023   | Accrual                | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |

  @TestRailId:C2885
  Scenario: Verify that Loan status goes from overpaid to active in case of CBR transaction (with replaying when CBR>new balance → clears overpaid, remaining increasing loan balance) - LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION loan product
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 September 2023 | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "1000" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 September 2023"
    And Customer makes "AUTOPAY" repayment on "16 September 2023" with 250 EUR transaction amount
    When Admin sets the business date to "01 October 2023"
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 350 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 100 overpaid amount
    When Admin makes Credit Balance Refund transaction on "16 October 2023" with 100 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Customer undo "1"th repayment on "16 September 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 250 outstanding amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 September 2023 |                   | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 15   | 16 September 2023 | 01 October 2023   | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 3  | 15   | 01 October 2023   | 16 October 2023   | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 4  | 15   | 16 October 2023   |                   | 0.0             | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 100.0 | 0.0        | 0.0   | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1100.0        | 0        | 0    | 0         | 1100.0 | 850.0 | 0          | 500  | 250         |
    Then On Loan Transactions tab the "Repayment" Transaction with date "16 September 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type      | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement          | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 September 2023 | Down Payment          | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 September 2023 | Repayment             | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 October 2023   | Repayment             | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 16 October 2023   | Repayment             | 350.0  | 350.0     | 0.0      | 0.0  | 0.0       | 150.0        |
      | 16 October 2023   | Credit Balance Refund | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 250.0        |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 September 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 September 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "16 September 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 October 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "16 October 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 350.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 350.0 |        |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "16 October 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |

  @TestRailId:C2886
  Scenario: Verify that Loan status goes from overpaid to closed in case of CBR transaction when transaction amount equals overpaid amount - LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION loan product
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 September 2023 | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "1000" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 September 2023"
    And Customer makes "AUTOPAY" repayment on "16 September 2023" with 250 EUR transaction amount
    When Admin sets the business date to "01 October 2023"
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 350 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 100 overpaid amount
    When Admin makes Credit Balance Refund transaction on "16 October 2023" with 100 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |                   | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 September 2023 | 16 September 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 01 October 2023   | 01 October 2023   | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 16 October 2023   | 16 October 2023   | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 1000.0 | 0          | 0    | 0           |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type      | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement          | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 September 2023 | Down Payment          | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 September 2023 | Repayment             | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 October 2023   | Repayment             | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 16 October 2023   | Repayment             | 350.0  | 250.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 16 October 2023   | Credit Balance Refund | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 September 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 September 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "16 September 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 October 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "16 October 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | l1           | Overpayment account       |       | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 350.0 |        |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "16 October 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |

  @TestRailId:C2887
  Scenario: Verify that Loan status goes from overpaid to active in case of Refund transaction was reverted - LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION loan product
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 September 2023 | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "1000" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 September 2023"
    And Customer makes "AUTOPAY" repayment on "16 September 2023" with 250 EUR transaction amount
    When Admin sets the business date to "01 October 2023"
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 250 EUR transaction amount
    And Refund happens on "16 October 2023" with 100 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 100 overpaid amount
    When Admin makes Credit Balance Refund transaction on "16 October 2023" with 100 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Refund undo happens on "16 October 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 100 outstanding amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |                   | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 September 2023 | 16 September 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 01 October 2023   | 01 October 2023   | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 16 October 2023   |                   | 0.0             | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 250.0 | 0.0        | 0.0  | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1100.0        | 0        | 0    | 0         | 1100.0 | 1000.0 | 0          | 0    | 100         |
    Then On Loan Transactions tab the "Payout Refund" Transaction with date "16 October 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type      | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement          | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 September 2023 | Down Payment          | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 September 2023 | Repayment             | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 October 2023   | Repayment             | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 16 October 2023   | Repayment             | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 16 October 2023   | Payout Refund         | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |
      | 16 October 2023   | Credit Balance Refund | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 September 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 September 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "16 September 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 October 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "16 October 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "16 October 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       |       | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 100.0 |        |
      | LIABILITY | l1           | Overpayment account       | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "16 October 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |

  @TestRailId:C2888
  Scenario: Multi repayment reversal (after maturity) - LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION loan product
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 September 2023 | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "1000" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 September 2023"
    And Customer makes "AUTOPAY" repayment on "16 September 2023" with 250 EUR transaction amount
    When Admin sets the business date to "01 October 2023"
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "20 October 2023"
    And Customer makes "AUTOPAY" repayment on "20 October 2023" with 350 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 100 overpaid amount
    When Admin sets the business date to "25 October 2023"
    When Admin makes Credit Balance Refund transaction on "25 October 2023" with 100 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Customer undo "1"th repayment on "20 October 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 250 outstanding amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 September 2023 |                   | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 15   | 16 September 2023 | 01 October 2023   | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 3  | 15   | 01 October 2023   | 20 October 2023   | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 4  | 15   | 16 October 2023   |                   | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 100.0 | 0.0        | 100.0 | 150.0       |
      | 5  | 9    | 25 October 2023   |                   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0   | 0.0        | 0.0   | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1100.0        | 0        | 0    | 0         | 1100.0 | 850.0 | 0          | 600  | 250         |
    Then On Loan Transactions tab the "Repayment" Transaction with date "16 September 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type      | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement          | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 September 2023 | Down Payment          | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 September 2023 | Repayment             | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 October 2023   | Repayment             | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 20 October 2023   | Repayment             | 350.0  | 350.0     | 0.0      | 0.0  | 0.0       | 150.0        |
      | 25 October 2023   | Credit Balance Refund | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 250.0        |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 September 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 September 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "16 September 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 October 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "20 October 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 350.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 350.0 |        |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "25 October 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |

  @TestRailId:C2889
  Scenario: Verify that Loan status goes from overpaid to active in case of CBR transaction (after maturity) - LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION loan product
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 September 2023 | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "1000" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 September 2023"
    And Customer makes "AUTOPAY" repayment on "16 September 2023" with 250 EUR transaction amount
    When Admin sets the business date to "01 October 2023"
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "20 October 2023"
    And Customer makes "AUTOPAY" repayment on "20 October 2023" with 200 EUR transaction amount
    When Admin sets the business date to "21 October 2023"
    And Customer makes "AUTOPAY" repayment on "21 October 2023" with 150 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 100 overpaid amount
    When Admin makes Credit Balance Refund transaction on "21 October 2023" with 100 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Customer undo "1"th repayment on "20 September 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 250 outstanding amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 September 2023 |                   | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 15   | 16 September 2023 | 01 October 2023   | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 3  | 15   | 01 October 2023   | 21 October 2023   | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 4  | 15   | 16 October 2023   |                   | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 100.0 | 0.0        | 100.0 | 150.0       |
      | 5  | 5    | 21 October 2023   |                   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0   | 0.0        | 0.0   | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1100.0        | 0        | 0    | 0         | 1100.0 | 850.0 | 0          | 600  | 250         |
    Then On Loan Transactions tab the "Repayment" Transaction with date "16 September 2023" is reverted
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type      | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement          | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 September 2023 | Down Payment          | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 September 2023 | Repayment             | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 October 2023   | Repayment             | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 20 October 2023   | Repayment             | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 300.0        |
      | 21 October 2023   | Repayment             | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 150.0        |
      | 21 October 2023   | Credit Balance Refund | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 250.0        |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 September 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 September 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "16 September 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 October 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "20 October 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 200.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 200.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "21 October 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 150.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 150.0 |        |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "21 October 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |

  @TestRailId:C2890
  Scenario: Verify that accruals created for charges after CBR post-maturity - LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION loan product
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 September 2023 | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "1000" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 September 2023"
    And Customer makes "AUTOPAY" repayment on "16 September 2023" with 250 EUR transaction amount
    When Admin sets the business date to "01 October 2023"
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 250 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "17 October 2023"
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "17 October 2023" with 100 EUR transaction amount and system-generated Idempotency key
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "18 October 2023"
    And Admin makes Credit Balance Refund transaction on "18 October 2023" with 100 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |                   | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 September 2023 | 16 September 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 01 October 2023   | 01 October 2023   | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 16 October 2023   | 16 October 2023   | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 1000.0 | 0          | 0    | 0           |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 September 2023 | Down Payment           | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 September 2023 | Repayment              | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 October 2023   | Repayment              | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 16 October 2023   | Repayment              | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 17 October 2023   | Merchant Issued Refund | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |
      | 18 October 2023   | Credit Balance Refund  | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "19 October 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "19 October 2023" due date and 10 EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "20 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |                   | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 September 2023 | 16 September 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 01 October 2023   | 01 October 2023   | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 16 October 2023   | 16 October 2023   | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 5  | 3    | 19 October 2023   |                   | 0.0             | 0.0           | 0.0      | 10.0 | 0.0       | 10.0  | 0.0   | 0.0        | 0.0  | 10.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0        | 10   | 0         | 1010.0 | 1000.0 | 0          | 0    | 10          |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 September 2023 | Down Payment           | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 September 2023 | Repayment              | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 October 2023   | Repayment              | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 16 October 2023   | Repayment              | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 17 October 2023   | Merchant Issued Refund | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |
      | 18 October 2023   | Credit Balance Refund  | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |
      | 19 October 2023   | Accrual                | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |

  @TestRailId:C2989
  Scenario: Verify that CBR transaction date cannot be in the future
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 September 2023 | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "1000" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 September 2023"
    And Customer makes "AUTOPAY" repayment on "10 September 2023" with 1000 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 250 overpaid amount
    When Admin sets the business date to "15 September 2023"
    Then Credit Balance Refund transaction on future date "20 September 2023" with 250 EUR transaction amount will result an error

  @TestRailId:C3020
  Scenario: Verify that Charge-off and CBR transaction GL entries are correct in case of repayment reversal after CBR and Fraud flagged loan
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    Then Admin can successfully set Fraud flag to the loan
    And Admin does charge-off the loan on "01 January 2024"
    Then Loan marked as charged-off on "01 January 2024"
    When Admin sets the business date to "10 January 2024"
    And Customer makes "AUTOPAY" repayment on "10 January 2024" with 1000 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 250 overpaid amount
    When Admin sets the business date to "11 January 2024"
    And Admin makes Credit Balance Refund transaction on "11 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "12 January 2024"
    When Customer undo "1"th "Repayment" transaction made on "10 January 2024"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "01 January 2024" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 750.0  |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 750.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | INCOME    | 744008       | Recoveries                |        | 750.0  |
      | LIABILITY | l1           | Overpayment account       |        | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
      | INCOME    | 744008       | Recoveries                | 750.0  |        |
      | LIABILITY | l1           | Overpayment account       | 250.0  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "11 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name               | Debit | Credit |
      | EXPENSE   | 744037       | Credit Loss/Bad Debt-Fraud | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account  |       | 250.0  |

  @TestRailId:C3021
  Scenario: Verify that Charge-off and CBR transaction GL entries are correct in case of repayment reversal after CBR and Non-Fraud loan
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    And Admin does charge-off the loan on "01 January 2024"
    Then Loan marked as charged-off on "01 January 2024"
    When Admin sets the business date to "10 January 2024"
    And Customer makes "AUTOPAY" repayment on "10 January 2024" with 1000 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 250 overpaid amount
    When Admin sets the business date to "11 January 2024"
    And Admin makes Credit Balance Refund transaction on "11 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "12 January 2024"
    When Customer undo "1"th "Repayment" transaction made on "10 January 2024"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "01 January 2024" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable     |       | 750.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 750.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | INCOME    | 744008       | Recoveries                |        | 750.0  |
      | LIABILITY | l1           | Overpayment account       |        | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
      | INCOME    | 744008       | Recoveries                | 750.0  |        |
      | LIABILITY | l1           | Overpayment account       | 250.0  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "11 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |

  @TestRailId:C3040
  Scenario: Verify that Charge-off and CBR transaction GL entries are correct before and after a repayment reversal taken place after CBR - UC1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    And Customer makes "AUTOPAY" repayment on "02 January 2024" with 100 EUR transaction amount
    When Admin sets the business date to "03 January 2024"
    And Admin does charge-off the loan on "03 January 2024"
    When Admin sets the business date to "04 January 2024"
    When Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "04 January 2024" with 1000 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "05 January 2024"
    And Admin makes Credit Balance Refund transaction on "05 January 2024" with 350 EUR transaction amount
#    --- Before reverse/replay ---
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 100.0 |        |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "03 January 2024" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable     |       | 650.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 650.0 |        |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "04 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 650.0  |
      | LIABILITY | l1           | Overpayment account       |        | 350.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "05 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       | 350.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 350.0  |
#    --- After reverse/replay ---
    When Admin sets the business date to "06 January 2024"
    When Customer undo "1"th "Repayment" transaction made on "02 January 2024"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 100.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "03 January 2024" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable     |       | 750.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 750.0 |        |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "04 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |        | 750.0  |
      | LIABILITY | l1           | Overpayment account       |        | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "05 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 100.0 |        |
      | LIABILITY | l1           | Overpayment account       | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 350.0  |

  @TestRailId:C3041
  Scenario: Verify that Charge-off and CBR transaction GL entries are correct before and after a repayment reversal taken place after CBR - UC2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    And Admin does charge-off the loan on "01 January 2024"
    Then Loan marked as charged-off on "01 January 2024"
    When Admin sets the business date to "10 January 2024"
    And Customer makes "AUTOPAY" repayment on "10 January 2024" with 1000 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 250 overpaid amount
    When Admin sets the business date to "11 January 2024"
    And Admin makes Credit Balance Refund transaction on "11 January 2024" with 250 EUR transaction amount
#    --- Before reverse/replay ---
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "01 January 2024" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable     |       | 750.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 750.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | INCOME    | 744008       | Recoveries                |        | 750.0  |
      | LIABILITY | l1           | Overpayment account       |        | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "11 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
#    --- After reverse/replay ---
    When Admin sets the business date to "12 January 2024"
    When Customer undo "1"th "Repayment" transaction made on "10 January 2024"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "01 January 2024" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable     |       | 750.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 750.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | INCOME    | 744008       | Recoveries                |        | 750.0  |
      | LIABILITY | l1           | Overpayment account       |        | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
      | INCOME    | 744008       | Recoveries                | 750.0  |        |
      | LIABILITY | l1           | Overpayment account       | 250.0  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "11 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |

  @TestRailId:C3092
  Scenario: Verify that overpayment portion calculated properly in case of CBR reverse-replay
    When Admin sets the business date to "25 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION | 25 January 2024   | 212.87         | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "25 January 2024" with "212.87" amount and expected disbursement date on "25 January 2024"
    When Admin successfully disburse the loan on "25 January 2024" with "212.87" EUR transaction amount
    When Admin sets the business date to "24 February 2024"
    And Customer makes "AUTOPAY" repayment on "24 February 2024" with 212.87 EUR transaction amount
    When Admin sets the business date to "29 February 2024"
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "29 February 2024" with 36.99 EUR transaction amount and system-generated Idempotency key
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "29 February 2024" with 18.94 EUR transaction amount and system-generated Idempotency key
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "29 February 2024" with 36.99 EUR transaction amount and system-generated Idempotency key
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "29 February 2024" with 31.91 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "01 March 2024"
    And Admin makes Credit Balance Refund transaction on "01 March 2024" with 124.83 EUR transaction amount
    When Admin sets the business date to "02 March 2024"
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "02 March 2024" with 19.99 EUR transaction amount and system-generated Idempotency key
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "02 March 2024" with 19.99 EUR transaction amount and system-generated Idempotency key
    When Customer undo "1"th "Merchant Issued Refund" transaction made on "29 February 2024"
    Then Loan status will be "OVERPAID"
    Then Loan has 2.99 overpaid amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      |    |      | 25 January 2024  |                  | 212.87          |               |          | 0.0  |           | 0.0    | 0.0    |            |       |             |
      | 1  | 0    | 25 January 2024  | 24 February 2024 | 159.87          | 53.0          | 0.0      | 0.0  | 0.0       | 53.0   | 53.0   | 0.0        | 53.0  | 0.0         |
      | 2  | 30   | 24 February 2024 | 24 February 2024 | 0.0             | 159.87        | 0.0      | 0.0  | 0.0       | 159.87 | 159.87 | 0.0        | 0.0   | 0.0         |
      | 3  | 6    | 01 March 2024    | 02 March 2024    | 0.0             | 124.83        | 0.0      | 0.0  | 0.0       | 124.83 | 124.83 | 0.0        | 36.99 | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 337.7         | 0.0      | 0.0  | 0.0       | 337.7 | 337.7 | 0.0        | 89.99 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 25 January 2024  | Disbursement           | 212.87 | 0.0       | 0.0      | 0.0  | 0.0       | 212.87       | false    |
      | 24 February 2024 | Repayment              | 212.87 | 212.87    | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 29 February 2024 | Merchant Issued Refund | 36.99  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | true     |
      | 29 February 2024 | Merchant Issued Refund | 18.94  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 29 February 2024 | Merchant Issued Refund | 36.99  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 29 February 2024 | Merchant Issued Refund | 31.91  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 01 March 2024    | Credit Balance Refund  | 124.83 | 36.99     | 0.0      | 0.0  | 0.0       | 36.99        | false    |
      | 02 March 2024    | Merchant Issued Refund | 19.99  | 19.99     | 0.0      | 0.0  | 0.0       | 17.0         | false    |
      | 02 March 2024    | Merchant Issued Refund | 19.99  | 17.0      | 0.0      | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C3140
  Scenario: Verify that the journal entries are correct in case of merchant issued refund (chargeoff, backdated transaction, undo repayment, downpayment)
    When Admin sets the business date to "24 May 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 24 May 2024       | 200            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "24 May 2024" with "200" amount and expected disbursement date on "24 May 2024"
    When Admin successfully disburse the loan on "24 May 2024" with "200" EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "25 May 2024"
    When Customer undo "1"th "Down Payment" transaction made on "24 May 2024"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 24 May 2024  |           | 200.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 24 May 2024  |           | 150.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0  | 0.0  | 0.0        | 0.0  | 50.0        |
      | 2  | 30   | 23 June 2024 |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 24 May 2024      | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 24 May 2024      | Down Payment     | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | true     | false    |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 0.0  | 0.0        | 0.0  | 200.0       |
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "26 May 2024"
    And Admin does charge-off the loan on "26 May 2024"
    And Customer makes "AUTOPAY" repayment on "25 May 2024" with 10 EUR transaction amount
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "26 May 2024" with 200 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date   | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 24 May 2024  |             | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 24 May 2024  | 26 May 2024 | 150.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0  | 50.0  | 0.0        | 50.0 | 0.0         |
      | 2  | 30   | 23 June 2024 | 26 May 2024 | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 150.0 | 150.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 200.0 | 150.0      | 50.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 24 May 2024      | Disbursement           | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 24 May 2024      | Down Payment           | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | true     | false    |
      | 25 May 2024      | Repayment              | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 190.0        | false    | false    |
      | 26 May 2024      | Charge-off             | 190.0  | 190.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 26 May 2024      | Merchant Issued Refund | 200.0  | 190.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin runs inline COB job for Loan
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 10 overpaid amount
    When Admin sets the business date to "27 May 2024"
    When Admin makes Credit Balance Refund transaction on "27 May 2024" with 10 EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date   | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 24 May 2024  |             | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 24 May 2024  | 26 May 2024 | 150.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0  | 50.0  | 0.0        | 50.0 | 0.0         |
      | 2  | 30   | 23 June 2024 | 26 May 2024 | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 150.0 | 150.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 200.0 | 150.0      | 50.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 24 May 2024      | Disbursement           | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 24 May 2024      | Down Payment           | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | true     | false    |
      | 25 May 2024      | Repayment              | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 190.0        | false    | false    |
      | 26 May 2024      | Charge-off             | 190.0  | 190.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 26 May 2024      | Merchant Issued Refund | 200.0  | 190.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 May 2024      | Credit Balance Refund  | 10.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Customer undo "1"th "Repayment" transaction made on "25 May 2024"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date   | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 24 May 2024  |             | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 24 May 2024  | 26 May 2024 | 150.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0  | 50.0  | 0.0        | 50.0 | 0.0         |
      | 2  | 30   | 23 June 2024 |             | 0.0             | 160.0         | 0.0      | 0.0  | 0.0       | 160.0 | 150.0 | 150.0      | 0.0  | 10.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 210.0         | 0.0      | 0.0  | 0.0       | 210.0 | 200.0 | 150.0      | 50.0 | 10.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 24 May 2024      | Disbursement           | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 24 May 2024      | Down Payment           | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | true     | false    |
      | 25 May 2024      | Repayment              | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 190.0        | true     | false    |
      | 26 May 2024      | Charge-off             | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 26 May 2024      | Merchant Issued Refund | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 27 May 2024      | Credit Balance Refund  | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 10.0         | false    | true     |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "24 May 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 200.0  |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "24 May 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 50.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 50.0  |        |
      | ASSET     | 112601       | Loans Receivable          | 50.0  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 50.0   |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "25 May 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 10.0  |        |
      | ASSET     | 112601       | Loans Receivable          | 10.0  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 10.0   |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "26 May 2024" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable     |       | 200.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 200.0 |        |
    Then Loan Transactions tab has a "MERCHANT_ISSUED_REFUND" transaction with date "26 May 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |       | 200.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 200.0 |        |
    Then Loan Transactions tab has a "CREDIT_BALANCE_REFUND" transaction with date "27 May 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 10.0  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 10.0   |

  @TestRailId:C3203
  Scenario: Verify that loan status is correct when CBR is reversed on an overpaid loan
    When Admin sets the business date to "01 July 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 July 2024       | 1000            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2024" with "1000" amount and expected disbursement date on "01 July 2024"
    When Admin successfully disburse the loan on "01 July 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "10 July 2024"
    And Customer makes "AUTOPAY" repayment on "10 July 2024" with 1000 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 250 overpaid amount
    When Admin sets the business date to "11 July 2024"
    When Admin makes Credit Balance Refund transaction on "11 July 2024" with 250 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "12 July 2024"
    When Customer undo "1"th transaction made on "11 July 2024"
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 250 overpaid amount

  @TestRailId:C3734
  Scenario: Verify that 2nd disbursement is allowed after MIR, Payout Refund and Credit Balance Refund closes the loan
    When Admin sets the business date to "14 March 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_CUSTOM_PAYMENT_ALLOCATION  | 14 March 2024     | 487.58         | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "14 March 2024" with "487.58" amount and expected disbursement date on "14 March 2024"
  # First disbursement with automatic downpayment
    When Admin successfully disburse the loan on "14 March 2024" with "487.58" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 14 March 2024 |               | 487.58          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 14 March 2024 | 14 March 2024 | 365.58          | 122.0         | 0.0      | 0.0  | 0.0       | 122.0  | 122.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 29 March 2024 |               | 243.58          | 122.0         | 0.0      | 0.0  | 0.0       | 122.0  | 0.0   | 0.0        | 0.0  | 122.0       |
      | 3  | 15   | 13 April 2024 |               | 121.58          | 122.0         | 0.0      | 0.0  | 0.0       | 122.0  | 0.0   | 0.0        | 0.0  | 122.0       |
      | 4  | 15   | 28 April 2024 |               | 0.0             | 121.58        | 0.0      | 0.0  | 0.0       | 121.58 | 0.0   | 0.0        | 0.0  | 121.58      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 487.58        | 0.0      | 0.0  | 0.0       | 487.58 | 122.0 | 0.0        | 0.0  | 365.58      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement     | 487.58 | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment     | 122.0  | 122.0     | 0.0      | 0.0  | 0.0       | 365.58       | false    | false    |
    When Admin runs inline COB job for Loan
  # Merchant Issued Refund
    When Admin sets the business date to "24 March 2024"
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "24 March 2024" with 201.39 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 14 March 2024 |               | 487.58          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 0    | 14 March 2024 | 14 March 2024 | 365.58          | 122.0         | 0.0      | 0.0  | 0.0       | 122.0  | 122.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 29 March 2024 |               | 243.58          | 122.0         | 0.0      | 0.0  | 0.0       | 122.0  | 0.0    | 0.0        | 0.0  | 122.0       |
      | 3  | 15   | 13 April 2024 |               | 121.58          | 122.0         | 0.0      | 0.0  | 0.0       | 122.0  | 79.81  | 79.81      | 0.0  | 42.19       |
      | 4  | 15   | 28 April 2024 | 24 March 2024 | 0.0             | 121.58        | 0.0      | 0.0  | 0.0       | 121.58 | 121.58 | 121.58     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 487.58        | 0.0      | 0.0  | 0.0       | 487.58 | 323.39 | 201.39     | 0.0  | 164.19      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement           | 487.58 | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment           | 122.0  | 122.0     | 0.0      | 0.0  | 0.0       | 365.58       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 201.39 | 201.39    | 0.0      | 0.0  | 0.0       | 164.19       | false    | false    |
    When Admin runs inline COB job for Loan
  # Move forward to next year for Payout Refund
    When Admin sets the business date to "24 March 2025"
    When Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "24 March 2025" with 286.19 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      |    |      | 14 March 2024 |               | 487.58          |               |          | 0.0  |           | 0.0    | 0.0    |            |       |             |
      | 1  | 0    | 14 March 2024 | 14 March 2024 | 365.58          | 122.0         | 0.0      | 0.0  | 0.0       | 122.0  | 122.0  | 0.0        | 0.0   | 0.0         |
      | 2  | 15   | 29 March 2024 | 24 March 2025 | 243.58          | 122.0         | 0.0      | 0.0  | 0.0       | 122.0  | 122.0  | 0.0        | 122.0 | 0.0         |
      | 3  | 15   | 13 April 2024 | 24 March 2025 | 121.58          | 122.0         | 0.0      | 0.0  | 0.0       | 122.0  | 122.0  | 79.81      | 42.19 | 0.0         |
      | 4  | 15   | 28 April 2024 | 24 March 2024 | 0.0             | 121.58        | 0.0      | 0.0  | 0.0       | 121.58 | 121.58 | 121.58     | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      | 487.58        | 0.0      | 0.0  | 0.0       | 487.58 | 487.58 | 201.39     | 164.19 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement           | 487.58 | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment           | 122.0  | 122.0     | 0.0      | 0.0  | 0.0       | 365.58       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 201.39 | 201.39    | 0.0      | 0.0  | 0.0       | 164.19       | false    | false    |
      | 24 March 2025    | Payout Refund          | 286.19 | 164.19    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 122.0 overpaid amount
    When Admin runs inline COB job for Loan
  # Credit Balance Refund to close the loan
    When Admin sets the business date to "25 March 2025"
    When Admin makes Credit Balance Refund transaction on "25 March 2025" with 122.0 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      |    |      | 14 March 2024 |               | 487.58          |               |          | 0.0  |           | 0.0    | 0.0    |            |       |             |
      | 1  | 0    | 14 March 2024 | 14 March 2024 | 365.58          | 122.0         | 0.0      | 0.0  | 0.0       | 122.0  | 122.0  | 0.0        | 0.0   | 0.0         |
      | 2  | 15   | 29 March 2024 | 24 March 2025 | 243.58          | 122.0         | 0.0      | 0.0  | 0.0       | 122.0  | 122.0  | 0.0        | 122.0 | 0.0         |
      | 3  | 15   | 13 April 2024 | 24 March 2025 | 121.58          | 122.0         | 0.0      | 0.0  | 0.0       | 122.0  | 122.0  | 79.81      | 42.19 | 0.0         |
      | 4  | 15   | 28 April 2024 | 24 March 2024 | 0.0             | 121.58        | 0.0      | 0.0  | 0.0       | 121.58 | 121.58 | 121.58     | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      | 487.58        | 0.0      | 0.0  | 0.0       | 487.58 | 487.58 | 201.39     | 164.19 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement           | 487.58 | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment           | 122.0  | 122.0     | 0.0      | 0.0  | 0.0       | 365.58       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 201.39 | 201.39    | 0.0      | 0.0  | 0.0       | 164.19       | false    | false    |
      | 24 March 2025    | Payout Refund          | 286.19 | 164.19    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2025    | Credit Balance Refund  | 122.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin runs inline COB job for Loan
  # Second disbursement
    When Admin sets the business date to "01 April 2025"
    When Admin successfully disburse the loan on "01 April 2025" with "243.79" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      |    |      | 14 March 2024 |               | 487.58          |               |          | 0.0  |           | 0.0    | 0.0    |            |       |             |
      | 1  | 0    | 14 March 2024 | 14 March 2024 | 365.58          | 122.0         | 0.0      | 0.0  | 0.0       | 122.0  | 122.0  | 0.0        | 0.0   | 0.0         |
      | 2  | 15   | 29 March 2024 | 24 March 2025 | 243.58          | 122.0         | 0.0      | 0.0  | 0.0       | 122.0  | 122.0  | 0.0        | 122.0 | 0.0         |
      | 3  | 15   | 13 April 2024 | 24 March 2025 | 121.58          | 122.0         | 0.0      | 0.0  | 0.0       | 122.0  | 122.0  | 79.81      | 42.19 | 0.0         |
      | 4  | 15   | 28 April 2024 | 24 March 2024 | 0.0             | 121.58        | 0.0      | 0.0  | 0.0       | 121.58 | 121.58 | 121.58     | 0.0   | 0.0         |
      |    |      | 01 April 2025 |               | 243.79          |               |          | 0.0  |           | 0.0    | 0.0    |            |       |             |
      | 5  | 0    | 01 April 2025 | 01 April 2025 | 182.79          | 61.0          | 0.0      | 0.0  | 0.0       | 61.0   | 61.0   | 0.0        | 0.0   | 0.0         |
      | 6  | 0    | 01 April 2025 |               | 0.0             | 182.79        | 0.0      | 0.0  | 0.0       | 182.79 | 0.0    | 0.0        | 0.0   | 182.79      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      | 731.37        | 0.0      | 0.0  | 0.0       | 731.37 | 548.58 | 201.39     | 164.19 | 182.79      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement           | 487.58 | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment           | 122.0  | 122.0     | 0.0      | 0.0  | 0.0       | 365.58       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 201.39 | 201.39    | 0.0      | 0.0  | 0.0       | 164.19       | false    | false    |
      | 24 March 2025    | Payout Refund          | 286.19 | 164.19    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2025    | Credit Balance Refund  | 122.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2025    | Disbursement           | 243.79 | 0.0       | 0.0      | 0.0  | 0.0       | 243.79       | false    | false    |
      | 01 April 2025    | Down Payment           | 61.0   | 61.0      | 0.0      | 0.0  | 0.0       | 182.79       | false    | false    |
    Then Loan status will be "ACTIVE"

  Scenario Outline: Verify that Loan ends in correct state after CBR + backdated GoodwillCredit cocktail (<rule> future-installment rule)
    When Admin sets the business date to "02 September 2025"
    And Admin creates a client with random data

    # Migration loan: submitted & disbursed back-dated to 06 April 2025, 6 monthly installments
    And Admin creates a fully customized loan with the following data:
      | LoanProduct   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | <loanProduct> | 06 April 2025     | 1316.49        | 12.2062                | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "06 April 2025" with "1316.49" amount and expected disbursement date on "06 April 2025"
    And Admin successfully disburse the loan on "06 April 2025" with "1316.49" EUR transaction amount

    # 4 backdated AUTOPAY repayments of 227.31 EUR (still on system date 02 September 2025)
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "06 May 2025" with 227.31 EUR transaction amount and system-generated Idempotency key
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "06 June 2025" with 227.31 EUR transaction amount and system-generated Idempotency key
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "06 July 2025" with 227.31 EUR transaction amount and system-generated Idempotency key
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "06 August 2025" with 227.31 EUR transaction amount and system-generated Idempotency key

    # 5th installment via AUTOPAY on 06 September 2025
    When Admin sets the business date to "06 September 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "06 September 2025" with 227.31 EUR transaction amount and system-generated Idempotency key

    # 6th installment via REAL_TIME on 02 October 2025
    When Admin sets the business date to "02 October 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "REPAYMENT" transaction with "REAL_TIME" payment type on "02 October 2025" with 227.00 EUR transaction amount and system-generated Idempotency key

    # 3 MIRs (interestRefundCalculation=false) on 29 October 2025 -> loan flips to OVERPAID
    When Admin sets the business date to "29 October 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "29 October 2025" with 242.00 EUR transaction amount and system-generated Idempotency key and interestRefundCalculation false
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "29 October 2025" with 242.00 EUR transaction amount and system-generated Idempotency key and interestRefundCalculation false
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "29 October 2025" with 30.49 EUR transaction amount and system-generated Idempotency key and interestRefundCalculation false
    Then Loan status will be "OVERPAID"

    # 30 October 2025 -> CBR 514.49
    When Admin sets the business date to "30 October 2025"
    And Admin runs inline COB job for Loan
    And Admin makes Credit Balance Refund transaction on "30 October 2025" with 514.49 EUR transaction amount

    # 11 December 2025 -> INTEREST_REFUND on MIR1 + backdated GOODWILL_CREDIT (txn date 28 October 2025, BEFORE the MIRs)
    When Admin sets the business date to "11 December 2025"
    And Admin runs inline COB job for Loan
    And Admin manually adds Interest Refund for "1"th "MERCHANT_ISSUED_REFUND" transaction made on "29 October 2025" with 0.01 EUR interest refund amount
    And Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "28 October 2025" with 0.01 EUR transaction amount and system-generated Idempotency key

    # 12 December 2025 -> CBR 27.92
    When Admin sets the business date to "12 December 2025"
    And Admin runs inline COB job for Loan
    And Admin makes Credit Balance Refund transaction on "12 December 2025" with 27.92 EUR transaction amount

    # 16 December 2025 -> INTEREST_REFUND on MIR2 & MIR3 + another backdated GOODWILL_CREDIT
    When Admin sets the business date to "16 December 2025"
    And Admin runs inline COB job for Loan
    And Admin manually adds Interest Refund for "2"th "MERCHANT_ISSUED_REFUND" transaction made on "29 October 2025" with 0.01 EUR interest refund amount
    And Admin manually adds Interest Refund for "3"th "MERCHANT_ISSUED_REFUND" transaction made on "29 October 2025" with 0.01 EUR interest refund amount
    And Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "28 October 2025" with 0.01 EUR transaction amount and system-generated Idempotency key

    # 17 December 2025 -> final CBR 0.01 - should fully close the loan
    When Admin sets the business date to "17 December 2025"
    And Admin runs inline COB job for Loan
    And Admin makes Credit Balance Refund transaction on "17 December 2025" with 0.01 EUR transaction amount
    Then Loan has 0.0 outstanding amount
    And Loan has 0.0 overpaid amount
    And Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 06 April 2025     |                   | 1316.49         |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 06 May 2025       | 06 May 2025       | 1102.39         | 214.1         | 13.21    | 0.0  | 0.0       | 227.31 | 227.31 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 06 June 2025      | 06 June 2025      | 886.51          | 215.88        | 11.43    | 0.0  | 0.0       | 227.31 | 227.31 | 0.0        | 0.0  | 0.0         |
      | 3  | 30   | 06 July 2025      | 06 July 2025      | 668.09          | 218.42        | 8.89     | 0.0  | 0.0       | 227.31 | 227.31 | 0.0        | 0.0  | 0.0         |
      | 4  | 31   | 06 August 2025    | 06 August 2025    | 447.71          | 220.38        | 6.93     | 0.0  | 0.0       | 227.31 | 227.31 | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 06 September 2025 | 06 September 2025 | 225.04          | 222.67        | 4.64     | 0.0  | 0.0       | 227.31 | 227.31 | 0.0        | 0.0  | 0.0         |
      | 6  | 30   | 06 October 2025   | 02 October 2025   | 0.0             | 225.04        | 1.96     | 0.0  | 0.0       | 227.0  | 227.0  | 227.0      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1316.49       | 47.06    | 0.0  | 0.0       | 1363.55 | 1363.55 | 227.0      | 0.0  | 0.0         |

    @TestRailId:C78812
    Examples: LAST_INSTALLMENT future-installment rule (the configuration that originally reproduced PS-3087)
      | rule             | loanProduct                                                                                       |
      | LAST_INSTALLMENT | LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_INT_REFUND_FULL_ZERO_INT_CHARGE_OFF_ACC_LAST_INSTALLMENT |

    @TestRailId:C78853
    Examples: NEXT_INSTALLMENT future-installment rule (default; must stay unaffected by the fix)
      | rule             | loanProduct                                                                                    |
      | NEXT_INSTALLMENT | LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_INT_REFUND_FULL_ZERO_INT_CHARGE_OFF_ACCRUAL_ACTIVITY |

  @TestRailId:C78851
  Scenario: Verify that backdated GoodwillCredit on fully paid loan followed by CBR closes the loan
    When Admin sets the business date to "15 December 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_INT_REFUND_FULL_ZERO_INT_CHARGE_OFF_ACC_LAST_INSTALLMENT | 01 January 2025   | 300            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "300" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "300" EUR transaction amount
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 February 2025" with 100.00 EUR transaction amount and system-generated Idempotency key
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 March 2025" with 100.00 EUR transaction amount and system-generated Idempotency key
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 April 2025" with 100.00 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    # Backdated GoodwillCredit dated BEFORE maturity (01 April 2025) on an already-closed loan
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "15 March 2025" with 0.50 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    And Loan has 0.0 outstanding amount
    And Loan has 0.5 overpaid amount
    # CBR equal to overpayment closes the loan; the schedule's last installment must remain intact
    When Admin makes Credit Balance Refund transaction on "15 April 2025" with 0.5 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan has 0.0 outstanding amount
    And Loan has 0.0 overpaid amount
    And Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                  | 300.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025 | 01 February 2025 | 200.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    | 01 March 2025    | 100.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025    | 01 April 2025    | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.5        | 0.0  | 0.0         |

  @TestRailId:C78852
  Scenario: Verify that Reverse-replay reduces overpayment so an earlier CBR re-runs with principalPortion > 0
    When Admin sets the business date to "15 December 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_INT_REFUND_FULL_ZERO_INT_CHARGE_OFF_ACC_LAST_INSTALLMENT | 01 January 2025   | 300            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "300" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "300" EUR transaction amount
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 February 2025" with 100.00 EUR transaction amount and system-generated Idempotency key
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 March 2025" with 100.00 EUR transaction amount and system-generated Idempotency key
    # Final repayment overpays by 50 EUR
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 April 2025" with 150.00 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    And Loan has 50.0 overpaid amount
    # CBR equals overpayment, after maturity → loan closes
    When Admin makes Credit Balance Refund transaction on "15 April 2025" with 50 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan has 0.0 outstanding amount
    # Reverse the SECOND repayment → reverse-replay re-runs the CBR with smaller overpayment
    When Customer undo "1"th repayment on "01 March 2025"
    Then Loan status will be "ACTIVE"
    And Loan has 100.0 outstanding amount
    And Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025  |               | 300.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2025 | 01 March 2025 | 200.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 100.0 | 0.0         |
      | 2  | 28   | 01 March 2025    | 01 April 2025 | 100.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 100.0 | 0.0         |
      | 3  | 31   | 01 April 2025    |               | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 50.0  | 0.0        | 0.0   | 50.0        |
      | 4  | 14   | 15 April 2025    |               | 0.0             | 50.0          | 0.0      | 0.0  | 0.0       | 50.0  | 0.0   | 0.0        | 0.0   | 50.0        |
