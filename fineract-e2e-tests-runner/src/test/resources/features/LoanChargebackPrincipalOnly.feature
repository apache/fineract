@ChargebackPrincipalOnlyFeature
Feature: Loan Chargeback - Principal only credit allocation

  @TestRailId:C119667
  Scenario: Verify Principal only Chargeback allocation - UC1: loan product template contains creditAllocationStrategyOptions
    Then Loan Product template has creditAllocationStrategyOptions:
      | Code           | Value          |
      | RULE_BASED     | Rule based     |
      | PRINCIPAL_ONLY | Principal only |

  @TestRailId:C119668
  Scenario: Verify Principal only Chargeback allocation - UC2: loan product with PRINCIPAL_ONLY returns strategy and empty allocation order
    When Admin creates a new Loan Product "LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_PRINCIPAL_ONLY" with chargeback credit allocation strategy "PRINCIPAL_ONLY", credit allocation order "NONE" and interest recalculation "false"
    Then Loan Product "LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_PRINCIPAL_ONLY" has the following credit allocation:
      | Transaction type | Credit allocation strategy | Credit allocation order |
      | CHARGEBACK       | PRINCIPAL_ONLY             |                         |

  @TestRailId:C119669
  Scenario: Verify Principal only Chargeback allocation - UC3: loan product created without strategy defaults to RULE_BASED
    Then Loan Product "LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_INTEREST_FIRST" has the following credit allocation:
      | Transaction type | Credit allocation strategy | Credit allocation order        |
      | CHARGEBACK       | RULE_BASED                 | INTEREST,PENALTY,FEE,PRINCIPAL |

  @TestRailId:C119670
  Scenario Outline: Verify Principal only Chargeback allocation - UC4: loan product create with invalid credit allocation is rejected
    Then Admin failed to create a new Loan Product with "<payment_strategy>" payment strategy, credit allocation strategy "<strategy>", credit allocation order "<allocation_order>" and got an error "<error_message>"

    Examples:
      | payment_strategy                        | strategy       | allocation_order               | error_message                                                                          |
      | ADVANCED_PAYMENT_ALLOCATION             | RULE_BASED     | NONE                           | Credit allocation with 'RULE_BASED' strategy must contain creditAllocationOrder        |
      | ADVANCED_PAYMENT_ALLOCATION             | NONE           | NONE                           | Credit allocation with 'RULE_BASED' strategy must contain creditAllocationOrder        |
      | ADVANCED_PAYMENT_ALLOCATION             | RULE_BASED     | EMPTY                          | Credit allocation with 'RULE_BASED' strategy must contain creditAllocationOrder        |
      | ADVANCED_PAYMENT_ALLOCATION             | PRINCIPAL_ONLY | PENALTY,FEE,INTEREST,PRINCIPAL | Credit allocation with 'PRINCIPAL_ONLY' strategy must not contain creditAllocationOrder |
      | ADVANCED_PAYMENT_ALLOCATION             | INVALID        | PENALTY,FEE,INTEREST,PRINCIPAL | Credit allocation was provided with a not valid credit allocation strategy             |
      | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER | PRINCIPAL_ONLY | NONE                           | In case 'mifos-standard-strategy' payment strategy, creditAllocation must not be provided |

  @TestRailId:C119671
  Scenario: Verify Principal only Chargeback allocation - UC5: loan product credit allocation strategy can be updated
    Given Loan product "LP2_CHARGEBACK_CREDIT_ALLOCATION_STRATEGY_UPDATE" dedicated to this scenario exists with chargeback credit allocation strategy "RULE_BASED" and credit allocation order "PENALTY,FEE,INTEREST,PRINCIPAL"
    Then Loan Product "LP2_CHARGEBACK_CREDIT_ALLOCATION_STRATEGY_UPDATE" has the following credit allocation:
      | Transaction type | Credit allocation strategy | Credit allocation order        |
      | CHARGEBACK       | RULE_BASED                 | PENALTY,FEE,INTEREST,PRINCIPAL |
    # --- Rule based -> Principal only ---
    When Admin updates "LP2_CHARGEBACK_CREDIT_ALLOCATION_STRATEGY_UPDATE" loan product chargeback credit allocation to strategy "PRINCIPAL_ONLY" and credit allocation order "NONE"
    Then Loan Product "LP2_CHARGEBACK_CREDIT_ALLOCATION_STRATEGY_UPDATE" has the following credit allocation:
      | Transaction type | Credit allocation strategy | Credit allocation order |
      | CHARGEBACK       | PRINCIPAL_ONLY             |                         |
    # --- Principal only -> Rule based ---
    When Admin updates "LP2_CHARGEBACK_CREDIT_ALLOCATION_STRATEGY_UPDATE" loan product chargeback credit allocation to strategy "RULE_BASED" and credit allocation order "INTEREST,PRINCIPAL,FEE,PENALTY"
    Then Loan Product "LP2_CHARGEBACK_CREDIT_ALLOCATION_STRATEGY_UPDATE" has the following credit allocation:
      | Transaction type | Credit allocation strategy | Credit allocation order        |
      | CHARGEBACK       | RULE_BASED                 | INTEREST,PRINCIPAL,FEE,PENALTY |

  @TestRailId:C119672
  Scenario: Verify Principal only Chargeback allocation - UC6: full chargeback, no interest recalculation
    When Admin sets the business date to "01 January 2024"
    When Admin creates a new Loan Product "LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_PRINCIPAL_ONLY" with chargeback credit allocation strategy "PRINCIPAL_ONLY", credit allocation order "NONE" and interest recalculation "false"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_PRINCIPAL_ONLY | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    # --- Repayment ---
    When Admin sets the business date to "01 February 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Transactions tab has a transaction with date: "01 March 2024", and with the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    # --- Chargeback: full repayment amount (principal + interest) is allocated to principal ---
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 2
    Then Loan Transactions tab has a transaction with date: "01 March 2024", and with the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 84.06        | false    | false    |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 33.63         | 0.39     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 117.01        | 2.05     | 0    | 0         | 119.06 | 34.02 | 0          | 0    | 85.04       |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 17.01  |
      | ASSET     | 112601       | Loans Receivable          | 17.01 |        |
    # Closing the loan
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119673
  Scenario: Verify Principal only Chargeback allocation - UC7: partial chargeback, no interest recalculation
    When Admin sets the business date to "01 January 2024"
    When Admin creates a new Loan Product "LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_PRINCIPAL_ONLY" with chargeback credit allocation strategy "PRINCIPAL_ONLY", credit allocation order "NONE" and interest recalculation "false"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_PRINCIPAL_ONLY | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    # --- Repayment ---
    When Admin sets the business date to "01 February 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    # --- Chargeback: partial amount is allocated to principal ---
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 10 EUR transaction amount for Payment nr. 2
    Then Loan Transactions tab has a transaction with date: "01 March 2024", and with the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 77.05        | false    | false    |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date          | Principal due | Interest | Fees | Penalties | Due   |
      | 3  | 31   | 01 April 2024 | 26.62         | 0.39     | 0.0  | 0.0       | 27.01 |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 110.0         | 2.05     | 0    | 0         | 112.05 | 34.02 | 0          | 0    | 78.03       |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 10.0   |
      | ASSET     | 112601       | Loans Receivable          | 10.0  |        |
    # Closing the loan
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119674
  Scenario: Verify Principal only Chargeback allocation - UC8: partial chargeback with interest recalculation enabled
    When Admin sets the business date to "01 January 2024"
    When Admin creates a new Loan Product "LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_PRINCIPAL_ONLY" with chargeback credit allocation strategy "PRINCIPAL_ONLY", credit allocation order "NONE" and interest recalculation "true"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_PRINCIPAL_ONLY | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    # --- Repayment ---
    When Admin sets the business date to "01 February 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    # --- Chargeback: partial amount is allocated to principal, interest is recalculated on the new balance ---
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 15 EUR transaction amount for Payment nr. 2
    Then Loan Transactions tab has a transaction with date: "01 March 2024", and with the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 March 2024    | Repayment        | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 01 March 2024    | Chargeback       | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 82.05        | false    | false    |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.52           | 31.53         | 0.48     | 0.0  | 0.0       | 32.01 | 0.0   | 0.0        | 0.0  | 32.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.8            | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.99           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.99         | 0.1      | 0.0  | 0.0       | 17.09 | 0.0   | 0.0        | 0.0  | 17.09       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 115.0         | 2.14     | 0    | 0         | 117.14 | 34.02 | 0          | 0    | 83.12       |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 15.0   |
      | ASSET     | 112601       | Loans Receivable          | 15.0  |        |
    # Closing the loan
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119675
  Scenario: Verify Principal only Chargeback allocation - UC9: chargeback of repayment covering fee and penalty
    When Admin sets the business date to "01 January 2024"
    When Admin creates a new Loan Product "LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_PRINCIPAL_ONLY" with chargeback credit allocation strategy "PRINCIPAL_ONLY", credit allocation order "NONE" and interest recalculation "false"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_PRINCIPAL_ONLY | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Admin runs inline COB job for Loan
    And Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 February 2024" due date and 10 EUR transaction amount
    And Admin adds "LOAN_NSF_FEE" due date charge with "01 February 2024" due date and 5 EUR transaction amount
    # --- Repayment: principal, interest, fee and penalty of period 1 ---
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 32.01 EUR transaction amount
    Then Loan Transactions tab has a transaction with date: "01 February 2024", and with the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 February 2024 | Repayment        | 32.01  | 16.43     | 0.58     | 10.0 | 5.0       | 83.57        | false    | false    |
    # --- Chargeback: fee, penalty and interest portions are not reinstated, full amount is principal ---
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 32.01 EUR transaction amount for Payment nr. 1
    Then Loan Transactions tab has a transaction with date: "01 February 2024", and with the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 February 2024 | Repayment        | 32.01  | 16.43     | 0.58     | 10.0 | 5.0       | 83.57        | false    | false    |
      | 01 February 2024 | Chargeback       | 32.01  | 32.01     | 0.0      | 0.0  | 0.0       | 115.58       | false    | false    |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date          | Principal due | Interest | Fees | Penalties | Due   |
      | 2  | 29   | 01 March 2024 | 48.53         | 0.49     | 0.0  | 0.0       | 49.02 |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 32.01  |
      | ASSET     | 112601       | Loans Receivable          | 32.01 |        |
    # Closing the loan
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119676
  Scenario: Verify Principal only Chargeback allocation - UC10: chargeback after maturity adds N+1 installment with principal only
    When Admin sets the business date to "01 January 2024"
    When Admin creates a new Loan Product "LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_PRINCIPAL_ONLY" with chargeback credit allocation strategy "PRINCIPAL_ONLY", credit allocation order "NONE" and interest recalculation "false"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_PRINCIPAL_ONLY | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    # --- Repayment ---
    When Admin sets the business date to "01 February 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    # --- Chargeback after maturity date ---
    When Admin sets the business date to "15 July 2024"
    And Admin runs inline COB job for Loan
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 1
    Then Loan status will be "ACTIVE"
    Then Loan has 17.01 outstanding amount
    Then Loan Transactions tab has a transaction with date: "15 July 2024", and with the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 15 July 2024     | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 17.01        | false    | false    |
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date         | Principal due | Interest | Fees | Penalties | Due   | Outstanding |
      | 7  | 14   | 15 July 2024 | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 17.01       |
    # Closing the loan
    When Loan Pay-off is made on "15 July 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119677
  Scenario: Verify Principal only Chargeback allocation - UC11: chargeback on overpaid loan
    When Admin sets the business date to "01 January 2024"
    When Admin creates a new Loan Product "LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_PRINCIPAL_ONLY" with chargeback credit allocation strategy "PRINCIPAL_ONLY", credit allocation order "NONE" and interest recalculation "false"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_PRINCIPAL_ONLY | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    # --- Repayment: overpay the loan by 20 ---
    When Admin sets the business date to "01 February 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 122.05 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 20 overpaid amount
    # --- Chargeback covered by the overpayment ---
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 15 EUR transaction amount for Payment nr. 1
    Then Loan status will be "OVERPAID"
    Then Loan has 5 overpaid amount
    Then Loan Transactions tab has a transaction with date: "01 February 2024", and with the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 February 2024 | Chargeback       | 15.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    # --- Chargeback exceeding the remaining overpayment, the rest is principal ---
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 10 EUR transaction amount for Payment nr. 1
    Then Loan status will be "ACTIVE"
    Then Loan has 5 outstanding amount
    Then Loan Transactions tab has a transaction with date: "01 February 2024", and with the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 February 2024 | Chargeback       | 10.0   | 5.0       | 0.0      | 0.0  | 0.0       | 5.0          | false    | false    |
    # Closing the loan
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119678
  Scenario: Verify Principal only Chargeback allocation - UC12: loan keeps the strategy copied at loan creation after product update
    Given Loan product "LP2_CHARGEBACK_CREDIT_ALLOCATION_STRATEGY_LOAN_COPY" dedicated to this scenario exists with chargeback credit allocation strategy "RULE_BASED" and credit allocation order "INTEREST,PENALTY,FEE,PRINCIPAL"
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_CHARGEBACK_CREDIT_ALLOCATION_STRATEGY_LOAN_COPY | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    # --- Repayment ---
    When Admin sets the business date to "01 February 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    # --- Product switched to Principal only, existing loan keeps Rule based ---
    When Admin updates "LP2_CHARGEBACK_CREDIT_ALLOCATION_STRATEGY_LOAN_COPY" loan product chargeback credit allocation to strategy "PRINCIPAL_ONLY" and credit allocation order "NONE"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 1
    Then Loan Transactions tab has a transaction with date: "01 February 2024", and with the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 February 2024 | Chargeback       | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 100.0        | false    | false    |
    When Loan Pay-off is made on "01 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    # --- New loan created after the product update uses Principal only ---
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_CHARGEBACK_CREDIT_ALLOCATION_STRATEGY_LOAN_COPY | 01 February 2024  | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 February 2024" with "100" amount and expected disbursement date on "01 February 2024"
    And Admin successfully disburse the loan on "01 February 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount for Payment nr. 1
    Then Loan Transactions tab has a transaction with date: "01 March 2024", and with the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 March 2024    | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 100.58       | false    | false    |
    # Closing the loan
    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
