@WorkingCapital
@WorkingCapitalLoanRepaymentAccountingEntriesFeature
Feature: Working Capital Loan Repayment Accounting Entries

  @TestRailId:C78870
  Scenario: Verify Working Capital loan repayment GL entries - UC1: simple principal repayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 270.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 270.0  |

  @TestRailId:C78871
  Scenario: Verify Working Capital loan repayment GL entries - UC2: multiple payments same day
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 170.0 transaction amount on Working Capital loan
    And Customer makes repayment on "02 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has 2 "REPAYMENT" transactions with date "02 January 2026" which have the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 170.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 170.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 100.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |

  @TestRailId:C78872
  Scenario: Verify Working Capital loan repayment GL entries - UC3: multiple payments different days
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 170.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 170.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 170.0  |
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 100.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |

  @TestRailId:C78873
  Scenario: Verify Working Capital loan repayment GL entries - UC4: repayment with overpayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 10000.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit   | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 10000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |         | 9000.0 |
      | LIABILITY | 245000       | Other Credit Liability    |         | 1000.0 |

  @Skip @RepaymentGLEntriesFee
  Scenario: Verify Working Capital loan repayment GL entries - UC5: repayment allocates to fees
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
#    TODO Add fee here to Working Capital loan
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 320.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 320.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 270.0  |
      | ASSET     | 112603       | Fee Receivable            |       | 50.0   |

  @Skip @RepaymentGLEntriesPenalty
  Scenario: Verify Working Capital loan repayment GL entries - UC6: repayment allocates to penalties
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
#    TODO Add penalty here to Working Capital loan
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 300.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 300.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 270.0  |
      | ASSET     | 112603       | Fee Receivable            |       | 50.0   |

  @Skip @RepaymentGLEntriesFeePenaltyOverpayment
  Scenario: Verify Working Capital loan repayment GL entries - UC7: complex allocation with fees, penalties, and overpayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
#    TODO Add fee + penalty here to Working Capital loan
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 10500.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit   | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 10500.0 |        |
      | ASSET     | 112601       | Loans Receivable          |         | 9000.0 |
      | ASSET     | 112603       | Fee Receivable            |         | 80.0   |
      | LIABILITY | 245000       | Other Credit liability    |         | 1420.0 |

  @TestRailId:C78874
  Scenario: Verify Working Capital loan repayment GL entries - UC8: partial repayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 100.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |

  @TestRailId:C85338
  Scenario: Verify Working Capital loan UNDO repayment GL entries - UC1: simple reversal
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 270.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 270.0  |
    When Customer undo "1"th "Repayment" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 270.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 270.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 270.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 270.0  |

  @UndoRepaymentGLEntries2
  Scenario: Verify Working Capital loan UNDO repayment GL entries - UC2: reversal with fees
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 50.0 transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 320.0 transaction amount on Working Capital loan
    When Customer undo "1"th "Repayment" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 270.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 50.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 320.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 270.0 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 50.0  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 320.0  |

  @UndoRepaymentGLEntries3
  Scenario: Verify Working Capital loan UNDO repayment GL entries - UC3: reversal with penalties
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "05 January 2026" due date and 50.0 transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 300.0 transaction amount on Working Capital loan
    When Customer undo "1"th "Repayment" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 50.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 300.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 50.0  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 300.0  |

  @TestRailId:C85339
  Scenario: Verify Working Capital loan UNDO repayment GL entries - UC4: reversal with overpayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 10000.0 transaction amount on Working Capital loan
    When Customer undo "1"th "Repayment" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit   | Credit  |
      | ASSET     | 112601       | Loans Receivable          |         | 9000.0  |
      | LIABILITY | 245000       | Other Credit Liability    |         | 1000.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 10000.0 |         |
      | ASSET     | 112601       | Loans Receivable          | 9000.0  |         |
      | LIABILITY | 245000       | Other Credit Liability    | 1000.0  |         |
      | LIABILITY | 145023       | Suspense/Clearing account |         | 10000.0 |

  @TestRailId:C78875
  Scenario: Verify Working Capital loan repayment GL entries - UC13: Advanced Accounting, payment channel fund source mapping
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 270.0 transaction amount on Working Capital loan with the following payment details:
      | paymentType    | accountNumber | checkNumber | routingCode | receiptNumber | bankNumber |
      | MONEY_TRANSFER |               |             |             |               |            |
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type  | Account code | Account name     | Debit | Credit |
      | ASSET | 987654       | Fund Receivables | 270.0 |        |
      | ASSET | 112601       | Loans Receivable |       | 270.0  |

  @TestRailId:C85340
  Scenario: Verify Working Capital loan UNDO repayment GL entries - UC5: Advanced Accounting, payment channel fund source mapping, reversal
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 270.0 transaction amount on Working Capital loan with the following payment details:
      | paymentType    | accountNumber | checkNumber | routingCode | receiptNumber | bankNumber |
      | MONEY_TRANSFER |               |             |             |               |            |
    When Customer undo "1"th "Repayment" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type  | Account code | Account name     | Debit | Credit |
      | ASSET | 112601       | Loans Receivable |       | 270.0  |
      | ASSET | 987654       | Fund Receivables | 270.0 |        |
      | ASSET | 112601       | Loans Receivable | 270.0 |        |
      | ASSET | 987654       | Fund Receivables |       | 270.0  |

  @TestRailId:C85604
  Scenario: Verify Working Capital loan repayment GL entries - UC9: repayment on already closed loan posts only overpayment credit
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 9000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "11 January 2026"
    And Customer makes repayment on "11 January 2026" with 300.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance overpaymentAmount is "300.00"
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "11 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 300.0 |        |
      | LIABILITY | 245000       | Other Credit Liability    |       | 300.0  |

  @TestRailId:C85605
  Scenario: Verify Working Capital loan repayment GL entries - UC10: repayment on already overpaid loan posts only overpayment credit
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 9200.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance overpaymentAmount is "200.00"
    When Admin sets the business date to "11 January 2026"
    And Customer makes repayment on "11 January 2026" with 199.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance overpaymentAmount is "399.00"
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "11 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 199.0 |        |
      | LIABILITY | 245000       | Other Credit Liability    |       | 199.0  |

  @TestRailId:C85606
  Scenario: Verify Working Capital loan repayment GL entries - UC11: repayment on already overpaid loan with advanced accounting payment channel fund source mapping
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 9200.0 transaction amount on Working Capital loan with the following payment details:
      | paymentType    | accountNumber | checkNumber | routingCode | receiptNumber | bankNumber |
      | MONEY_TRANSFER |               |             |             |               |            |
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance overpaymentAmount is "200.00"
    When Admin sets the business date to "11 January 2026"
    And Customer makes repayment on "11 January 2026" with 199.0 transaction amount on Working Capital loan with the following payment details:
      | paymentType    | accountNumber | checkNumber | routingCode | receiptNumber | bankNumber |
      | MONEY_TRANSFER |               |             |             |               |            |
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance overpaymentAmount is "399.00"
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "11 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 987654       | Fund Receivables           | 199.0 |        |
      | LIABILITY | 245000       | Other Credit Liability    |       | 199.0  |
