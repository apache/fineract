@AuditTrailFeature
Feature: Audit trail - submitted vs returned command identifiers

  @TestRailId:C119623
  Scenario: Verify audit trail keeps submitted command identifiers - UC1: successful loan transaction adjustment
    When Admin sets the business date to "01 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                          |
      | LP1         | 01 June 2024      | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    When Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
# --- Repayment ---
    When Admin makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 June 2024" with 100 EUR transaction amount
    When Save ID of "1"th "REPAYMENT" transaction made on "01 June 2024" as "repayment-txn-id"
# --- Audit check for the repayment itself: the txn id appears as resourceId, nothing was submitted ---
    Then The audit search for action "REPAYMENT" on entity "LOAN" filtered by resourceId of the "1"th "REPAYMENT" transaction made on "01 June 2024" returns 1 entries
    Then The audit search for action "REPAYMENT" on entity "LOAN" filtered by submittedResourceId of the "1"th "REPAYMENT" transaction made on "01 June 2024" returns 0 entries
    And The returned audit entry has the following data:
      | field               | value                  |
      | resourceId          | repayment-txn-id       |
      | submittedResourceId | none                   |
      | submittedLoanId     | created loan           |
      | processingResult    | Processed              |
# --- Repayment adjustment: submitted resourceId stays the original txn, returned resourceId is the new txn ---
    When Customer adjust 1th "REPAYMENT" transaction made on "01 June 2024" with "AUTOPAY" payment type on "01 June 2024" with 50 EUR transaction amount
    When Save ID of the last adjusted transaction as "adjustment-txn-id"
    Then The audit search for action "ADJUST" on entity "LOAN" filtered by resourceId of the last adjusted transaction returns 1 entries
    And The returned audit entry has the following data:
      | field               | value               |
      | resourceId          | adjustment-txn-id   |
      | submittedResourceId | repayment-txn-id    |
      | submittedLoanId     | created loan        |
      | processingResult    | Processed           |
    Then The audit search for action "ADJUST" on entity "LOAN" filtered by resourceId of the "1"th "REPAYMENT" transaction made on "01 June 2024" returns 0 entries
    Then The audit search for action "ADJUST" on entity "LOAN" filtered by submittedResourceId of the "1"th "REPAYMENT" transaction made on "01 June 2024" returns 1 entries
# --- close the loan ---
    When Loan Pay-off is made on "01 June 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119624
  Scenario: Verify audit trail keeps submitted command identifiers - UC2: failed loan transaction adjustment keeps submitted identifiers
    When Admin sets the business date to "01 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                          |
      | LP1         | 01 June 2024      | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    When Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
# --- Repayment ---
    When Admin makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 June 2024" with 100 EUR transaction amount
    When Save ID of "1"th "REPAYMENT" transaction made on "01 June 2024" as "repayment-txn-id"
# --- Audit check for the repayment itself: the txn id appears as resourceId, nothing was submitted ---
    Then The audit search for action "REPAYMENT" on entity "LOAN" filtered by resourceId of the "1"th "REPAYMENT" transaction made on "01 June 2024" returns 1 entries
    Then The audit search for action "REPAYMENT" on entity "LOAN" filtered by submittedResourceId of the "1"th "REPAYMENT" transaction made on "01 June 2024" returns 0 entries
    And The returned audit entry has the following data:
      | field               | value                  |
      | resourceId          | repayment-txn-id       |
      | submittedResourceId | none                   |
      | submittedLoanId     | created loan           |
      | processingResult    | Processed              |
# --- Failed adjustment: transaction date in the future is rejected ---
    Then Customer fails to adjust "1"th "REPAYMENT" transaction made on "01 June 2024" with "AUTOPAY" payment type on "02 June 2024" with 50 EUR transaction amount
# --- Audit checks: the failed command still reports the submitted identifiers ---
# (failed command: no result ids are written, so the submitted id remains in resourceId too)
    Then The audit search for action "ADJUST" on entity "LOAN" filtered by resourceId of the "1"th "REPAYMENT" transaction made on "01 June 2024" returns 1 entries
    Then The audit search for action "ADJUST" on entity "LOAN" filtered by submittedResourceId of the "1"th "REPAYMENT" transaction made on "01 June 2024" returns 1 entries
    And The returned audit entry has the following data:
      | field               | value                  |
      | submittedResourceId | repayment-txn-id       |
      | submittedLoanId     | created loan           |
      | processingResult    | Error                  |
# --- close the loan ---
    When Loan Pay-off is made on "01 June 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119625
  Scenario: Verify audit trail keeps submitted command identifiers - UC3: command submitted by loan external ID keeps submittedLoanExternalId
    When Admin sets the business date to "01 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 June 2024      | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    When Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
# --- Repayment ---
    When Admin makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 June 2024" with 100 EUR transaction amount
    When Save ID of "1"th "REPAYMENT" transaction made on "01 June 2024" as "repayment-txn-id"
# --- Audit check for the repayment itself: the txn id appears as resourceId, nothing was submitted ---
    Then The audit search for action "REPAYMENT" on entity "LOAN" filtered by resourceId of the "1"th "REPAYMENT" transaction made on "01 June 2024" returns 1 entries
    Then The audit search for action "REPAYMENT" on entity "LOAN" filtered by submittedResourceId of the "1"th "REPAYMENT" transaction made on "01 June 2024" returns 0 entries
    And The returned audit entry has the following data:
      | field               | value                  |
      | resourceId          | repayment-txn-id       |
      | submittedResourceId | none                   |
      | submittedLoanId     | created loan           |
      | processingResult    | Processed              |
# --- Re-amortization by loan external ID (needs an overdue installment, so business date is past the first due date) ---
    When Admin sets the business date to "02 July 2024"
    When Admin creates a Loan re-amortization transaction on current business date by loan external ID
# --- Audit checks: submittedLoanExternalId is searchable ---
    Then The audit search for action "REAMORTIZE" on entity "LOAN" filtered by submittedLoanExternalId of the created loan returns 1 entries
    And The returned audit entry has the following data:
      | field                   | value                   |
      | submittedLoanExternalId | created loan externalId |
      | processingResult        | Processed               |
# --- close the loan ---
    When Loan Pay-off is made on "02 July 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119626
  Scenario: Verify audit trail keeps submitted command identifiers - UC4: maker-checker queued command reports submitted identifiers
    When Admin sets the business date to "01 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                          |
      | LP1         | 01 June 2024      | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    When Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
# --- Repayment ---
    When Admin makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 June 2024" with 100 EUR transaction amount
    When Save ID of "1"th "REPAYMENT" transaction made on "01 June 2024" as "repayment-txn-id"
# --- Audit check for the repayment itself: the txn id appears as resourceId, nothing was submitted ---
    Then The audit search for action "REPAYMENT" on entity "LOAN" filtered by resourceId of the "1"th "REPAYMENT" transaction made on "01 June 2024" returns 1 entries
    Then The audit search for action "REPAYMENT" on entity "LOAN" filtered by submittedResourceId of the "1"th "REPAYMENT" transaction made on "01 June 2024" returns 0 entries
    And The returned audit entry has the following data:
      | field               | value                  |
      | resourceId          | repayment-txn-id       |
      | submittedResourceId | none                   |
      | submittedLoanId     | created loan           |
      | processingResult    | Processed              |
# --- Adjustment submitted while maker-checker is enabled stays awaiting approval ---
    Given Global configuration "maker-checker" is enabled
    Given Maker-checker is enabled for permission "ADJUST_LOAN"
    When Admin creates new user with "AUDIT_MAKER" username, "AUDIT_MAKER_ROLE" role name and given permissions:
      | ADJUST_LOAN |
    When Created user adjusts "1"th "REPAYMENT" transaction made on "01 June 2024" with "AUTOPAY" payment type on "01 June 2024" with 50 EUR transaction amount
# --- Audit checks: the pending command reports its submitted identifiers ---
# (pending command: resourceId already holds the submitted transaction id)
    Then The audit search for action "ADJUST" on entity "LOAN" filtered by resourceId of the "1"th "REPAYMENT" transaction made on "01 June 2024" returns 1 entries
    Then The audit search for action "ADJUST" on entity "LOAN" filtered by submittedResourceId of the "1"th "REPAYMENT" transaction made on "01 June 2024" returns 1 entries
    And The returned audit entry has the following data:
      | field               | value                  |
      | submittedResourceId | repayment-txn-id       |
      | submittedLoanId     | created loan           |
      | processingResult    | Awaiting Approval      |
    Given Maker-checker is disabled for permission "ADJUST_LOAN"
    Given Global configuration "maker-checker" is disabled
# --- close the loan ---
    When Loan Pay-off is made on "01 June 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119627
  Scenario: Verify audit trail keeps submitted command identifiers - UC5: successful adjustment on a progressive (LP2) loan
    When Admin sets the business date to "01 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 June 2024      | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    When Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
# --- Repayment ---
    When Admin makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 June 2024" with 100 EUR transaction amount
    When Save ID of "1"th "REPAYMENT" transaction made on "01 June 2024" as "repayment-txn-id"
# --- Audit check for the repayment itself: the txn id appears as resourceId, nothing was submitted ---
    Then The audit search for action "REPAYMENT" on entity "LOAN" filtered by resourceId of the "1"th "REPAYMENT" transaction made on "01 June 2024" returns 1 entries
    Then The audit search for action "REPAYMENT" on entity "LOAN" filtered by submittedResourceId of the "1"th "REPAYMENT" transaction made on "01 June 2024" returns 0 entries
    And The returned audit entry has the following data:
      | field               | value            |
      | resourceId          | repayment-txn-id |
      | submittedResourceId | none             |
      | submittedLoanId     | created loan     |
      | processingResult    | Processed        |
# --- Repayment adjustment: submitted resourceId stays the original txn, returned resourceId is the new txn ---
    When Customer adjust 1th "REPAYMENT" transaction made on "01 June 2024" with "AUTOPAY" payment type on "01 June 2024" with 50 EUR transaction amount
    When Save ID of the last adjusted transaction as "adjustment-txn-id"
    Then The audit search for action "ADJUST" on entity "LOAN" filtered by resourceId of the last adjusted transaction returns 1 entries
    And The returned audit entry has the following data:
      | field               | value             |
      | resourceId          | adjustment-txn-id |
      | submittedResourceId | repayment-txn-id  |
      | submittedLoanId     | created loan      |
      | processingResult    | Processed         |
    Then The audit search for action "ADJUST" on entity "LOAN" filtered by resourceId of the "1"th "REPAYMENT" transaction made on "01 June 2024" returns 0 entries
    Then The audit search for action "ADJUST" on entity "LOAN" filtered by submittedResourceId of the "1"th "REPAYMENT" transaction made on "01 June 2024" returns 1 entries
# --- close the loan ---
    When Loan Pay-off is made on "01 June 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119628
  Scenario: Verify audit trail keeps submitted command identifiers - UC6: successful adjustment on a Working Capital loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 1000            | 1000               | 1                 |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "1000" EUR transaction amount
# --- Repayment ---
    When Customer makes repayment on "01 January 2026" with 100.0 transaction amount on Working Capital loan
    When Save ID of "1"th "REPAYMENT" transaction made on "01 January 2026" as "repayment-txn-id" on Working Capital loan
# --- Audit check for the repayment itself: the txn id appears as resourceId, nothing was submitted ---
    Then The audit search for action "REPAYMENT" on entity "WORKINGCAPITALLOAN" filtered by resourceId of "repayment-txn-id" returns 1 entries
    Then The audit search for action "REPAYMENT" on entity "WORKINGCAPITALLOAN" filtered by submittedResourceId of "repayment-txn-id" returns 0 entries
    And The returned audit entry has the following data:
      | field               | value                        |
      | resourceId          | repayment-txn-id             |
      | submittedResourceId | created working capital loan |
      | processingResult    | Processed                    |
# --- Repayment adjustment: submitted resourceId stays the original txn, returned resourceId is the new txn ---
    When Admin sets the business date to "02 January 2026"
    When Customer adjust "1"th "REPAYMENT" transaction made on "01 January 2026" on Working Capital loan with amount "50" and date "02 January 2026"
    When Save ID of the last adjusted transaction as "adjustment-txn-id" on Working Capital loan
    Then The audit search for action "ADJUST" on entity "WORKINGCAPITALLOANTRANSACTION" filtered by resourceId of "adjustment-txn-id" returns 1 entries
    And The returned audit entry has the following data:
      | field               | value             |
      | resourceId          | adjustment-txn-id |
      | submittedResourceId | repayment-txn-id  |
      | processingResult    | Processed         |
    Then The audit search for action "ADJUST" on entity "WORKINGCAPITALLOANTRANSACTION" filtered by resourceId of "repayment-txn-id" returns 0 entries
    Then The audit search for action "ADJUST" on entity "WORKINGCAPITALLOANTRANSACTION" filtered by submittedResourceId of "repayment-txn-id" returns 1 entries
