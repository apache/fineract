@LoanRescheduleFeature
Feature: LoanReschedule - Part2

  @TestRailId:C85269
  Scenario: Verify that reschedule after ReAge produces correct schedule and balance for non-interest bearing single-installment product - UC4: 2nd reAge DEFAULT
    When Admin sets the business date to "10 June 2026"
    When Admin creates a client with random data
    When Admin set "LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 31 May 2026       | 150            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 May 2026" with "150" amount and expected disbursement date on "31 May 2026"
    When Admin successfully disburse the loan on "31 May 2026" with "150" EUR transaction amount
    Then Loan has 150.0 outstanding amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026  |           | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 30 June 2026 |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    # --- Step 1: Reschedule #1 date  : 2026-06-30  (from: 2026-06-30 → adjusted: 2026-09-30) ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate   | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 30 June 2026       | 10 June 2026    | 30 September 2026 |                  |                 |            |                 |
    Then Loan has 150.0 outstanding amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026       |           | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 122  | 30 September 2026 |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    # --- Step 2: ReAge for 15 monthly installments starting 22 March 2026 ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate    | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 12 June 2026 | 15                   | DEFAULT               |
    Then Loan has 150.0 outstanding amount
    And Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026       |              | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 10   | 10 June 2026      | 10 June 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 2    | 12 June 2026      |              | 140.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 3  | 30   | 12 July 2026      |              | 130.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 4  | 31   | 12 August 2026    |              | 120.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 5  | 31   | 12 September 2026 |              | 110.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 6  | 30   | 12 October 2026   |              | 100.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 7  | 31   | 12 November 2026  |              | 90.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 8  | 30   | 12 December 2026  |              | 80.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 9  | 31   | 12 January 2027   |              | 70.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 10 | 31   | 12 February 2027  |              | 60.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 11 | 28   | 12 March 2027     |              | 50.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 12 | 31   | 12 April 2027     |              | 40.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 13 | 30   | 12 May 2027       |              | 30.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 14 | 31   | 12 June 2027      |              | 20.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 15 | 30   | 12 July 2027      |              | 10.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 16 | 31   | 12 August 2027    |              | 0.0             | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 10 June 2026     | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    # --- Step 3: Reschedule again (push schedule 2 more months: 12 June -> 23 June) ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 12 June 2026       | 10 June 2026    | 23 June 2026    |                  |                 |            |                 |
    Then Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026       |              | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 10   | 10 June 2026      | 10 June 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 13   | 23 June 2026      |              | 140.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 3  | 30   | 23 July 2026      |              | 130.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 4  | 31   | 23 August 2026    |              | 120.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 5  | 31   | 23 September 2026 |              | 110.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 6  | 30   | 23 October 2026   |              | 100.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 7  | 31   | 23 November 2026  |              | 90.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 8  | 30   | 23 December 2026  |              | 80.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 9  | 31   | 23 January 2027   |              | 70.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 10 | 31   | 23 February 2027  |              | 60.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 11 | 28   | 23 March 2027     |              | 50.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 12 | 31   | 23 April 2027     |              | 40.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 13 | 30   | 23 May 2027       |              | 30.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 14 | 31   | 23 June 2027      |              | 20.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 15 | 30   | 23 July 2027      |              | 10.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 16 | 31   | 23 August 2027    |              | 0.0             | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 10 June 2026     | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C85270
  Scenario: Verify that reschedule after ReAge produces correct schedule and balance for non-interest bearing single-installment product - UC5: 2nd reAge EQUAL_AMORTIZATION_PAYABLE_INTEREST
    When Admin sets the business date to "10 June 2026"
    When Admin creates a client with random data
    When Admin set "LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 31 May 2026       | 150            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 May 2026" with "150" amount and expected disbursement date on "31 May 2026"
    When Admin successfully disburse the loan on "31 May 2026" with "150" EUR transaction amount
    Then Loan has 150.0 outstanding amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026  |           | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 30 June 2026 |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    # --- Step 1: Reschedule #1 date  : 2026-06-30  (from: 2026-06-30 → adjusted: 2026-09-30) ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate   | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 30 June 2026       | 10 June 2026    | 30 September 2026 |                  |                 |            |                 |
    Then Loan has 150.0 outstanding amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026       |           | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 122  | 30 September 2026 |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    # --- Step 2: ReAge for 15 monthly installments starting 12 June 2026 ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate    | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 12 June 2026 | 15                   | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
    Then Loan has 150.0 outstanding amount
    And Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026       |              | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 10   | 10 June 2026      | 10 June 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 2    | 12 June 2026      |              | 140.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 3  | 30   | 12 July 2026      |              | 130.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 4  | 31   | 12 August 2026    |              | 120.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 5  | 31   | 12 September 2026 |              | 110.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 6  | 30   | 12 October 2026   |              | 100.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 7  | 31   | 12 November 2026  |              | 90.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 8  | 30   | 12 December 2026  |              | 80.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 9  | 31   | 12 January 2027   |              | 70.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 10 | 31   | 12 February 2027  |              | 60.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 11 | 28   | 12 March 2027     |              | 50.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 12 | 31   | 12 April 2027     |              | 40.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 13 | 30   | 12 May 2027       |              | 30.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 14 | 31   | 12 June 2027      |              | 20.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 15 | 30   | 12 July 2027      |              | 10.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 16 | 31   | 12 August 2027    |              | 0.0             | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 10 June 2026     | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
# --- Step 3: Reschedule again (push schedule 2 more months: 12 June -> 23 June) ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 12 June 2026       | 10 June 2026    | 23 June 2026    |                  |                 |            |                 |
    Then Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026       |              | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 10   | 10 June 2026      | 10 June 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 13   | 23 June 2026      |              | 140.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 3  | 30   | 23 July 2026      |              | 130.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 4  | 31   | 23 August 2026    |              | 120.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 5  | 31   | 23 September 2026 |              | 110.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 6  | 30   | 23 October 2026   |              | 100.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 7  | 31   | 23 November 2026  |              | 90.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 8  | 30   | 23 December 2026  |              | 80.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 9  | 31   | 23 January 2027   |              | 70.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 10 | 31   | 23 February 2027  |              | 60.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 11 | 28   | 23 March 2027     |              | 50.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 12 | 31   | 23 April 2027     |              | 40.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 13 | 30   | 23 May 2027       |              | 30.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 14 | 31   | 23 June 2027      |              | 20.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 15 | 30   | 23 July 2027      |              | 10.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 16 | 31   | 23 August 2027    |              | 0.0             | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 10 June 2026     | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C85271
  Scenario: Verify that reschedule after ReAge produces correct schedule and balance for non-interest bearing single-installment product - UC5: 2nd reAge DEFAULT end of month
    When Admin sets the business date to "10 June 2026"
    When Admin creates a client with random data
    When Admin set "LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 31 May 2026       | 150            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 May 2026" with "150" amount and expected disbursement date on "31 May 2026"
    When Admin successfully disburse the loan on "31 May 2026" with "150" EUR transaction amount
    Then Loan has 150.0 outstanding amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026  |           | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 30 June 2026 |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    # --- Step 1: Reschedule #1 date  : 2026-06-30  (from: 2026-06-30 → adjusted: 2026-09-30) ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate   | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 30 June 2026       | 10 June 2026    | 30 September 2026 |                  |                 |            |                 |
    Then Loan has 150.0 outstanding amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026       |           | 150.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 122  | 30 September 2026 |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    # --- Step 2: ReAge for 15 monthly installments starting 12 July 2026 ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate    | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 12 July 2026 | 15                   | DEFAULT               |
    Then Loan has 150.0 outstanding amount
    And Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026       |              | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 10   | 10 June 2026      | 10 June 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 32   | 12 July 2026      |              | 140.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 3  | 31   | 12 August 2026    |              | 130.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 4  | 31   | 12 September 2026 |              | 120.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 5  | 30   | 12 October 2026   |              | 110.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 6  | 31   | 12 November 2026  |              | 100.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 7  | 30   | 12 December 2026  |              | 90.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 8  | 31   | 12 January 2027   |              | 80.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 9  | 31   | 12 February 2027  |              | 70.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 10 | 28   | 12 March 2027     |              | 60.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 11 | 31   | 12 April 2027     |              | 50.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 12 | 30   | 12 May 2027       |              | 40.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 13 | 31   | 12 June 2027      |              | 30.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 14 | 30   | 12 July 2027      |              | 20.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 15 | 31   | 12 August 2027    |              | 10.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 16 | 31   | 12 September 2027 |              | 0.0             | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 10 June 2026     | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    # --- Step 3: Reschedule again (push schedule 2 more months: 12 July -> 12 December) ---
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate  | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 12 July 2026       | 10 June 2026    | 12 December 2026 |                  |                 |            |                 |
    Then Loan Repayment schedule has 16 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 31 May 2026       |              | 150.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 10   | 10 June 2026      | 10 June 2026 | 150.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0  | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 185  | 12 December 2026  |              | 140.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 3  | 31   | 12 January 2027   |              | 130.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 4  | 31   | 12 February 2027  |              | 120.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 5  | 28   | 12 March 2027     |              | 110.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 6  | 31   | 12 April 2027     |              | 100.0           | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 7  | 30   | 12 May 2027       |              | 90.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 8  | 31   | 12 June 2027      |              | 80.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 9  | 30   | 12 July 2027      |              | 70.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 10 | 31   | 12 August 2027    |              | 60.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 11 | 31   | 12 September 2027 |              | 50.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 12 | 30   | 12 October 2027   |              | 40.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 13 | 31   | 12 November 2027  |              | 30.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 14 | 30   | 12 December 2027  |              | 20.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 15 | 31   | 12 January 2028   |              | 10.0            | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
      | 16 | 31   | 12 February 2028  |              | 0.0             | 10.0          | 0.0      | 0.0  | 0.0       | 10.0 | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 31 May 2026      | Disbursement     | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 10 June 2026     | Re-age           | 150.0  | 150.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C89778
  Scenario: Verify no duplicate installment periods after advance repayment and due date reschedule
    When Admin sets the business date to "20 January 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY | 20 January 2026   | 741.99         | 24.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 24                | MONTHS                | 1              | MONTHS                 | 24                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "20 January 2026" with "741.99" amount and expected disbursement date on "20 January 2026"
    And Admin successfully disburse the loan on "20 January 2026" with "741.99" EUR transaction amount
    Then Loan has 949.82 outstanding amount
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 20 January 2026   |           | 741.99          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 20 February 2026  |           | 718.16          | 23.83         | 15.75    | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 2  | 28   | 20 March 2026     |           | 692.35          | 25.81         | 13.77    | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 3  | 31   | 20 April 2026     |           | 667.46          | 24.89         | 14.69    | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 4  | 30   | 20 May 2026       |           | 641.59          | 25.87         | 13.71    | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 5  | 31   | 20 June 2026      |           | 615.63          | 25.96         | 13.62    | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 6  | 30   | 20 July 2026      |           | 588.69          | 26.94         | 12.64    | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 7  | 31   | 20 August 2026    |           | 561.6           | 27.09         | 12.49    | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 8  | 31   | 20 September 2026 |           | 533.94          | 27.66         | 11.92    | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 9  | 30   | 20 October 2026   |           | 505.33          | 28.61         | 10.97    | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 10 | 31   | 20 November 2026  |           | 476.48          | 28.85         | 10.73    | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 11 | 30   | 20 December 2026  |           | 446.69          | 29.79         | 9.79     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 12 | 31   | 20 January 2027   |           | 416.59          | 30.1          | 9.48     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 13 | 31   | 20 February 2027  |           | 385.85          | 30.74         | 8.84     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 14 | 28   | 20 March 2027     |           | 353.67          | 32.18         | 7.4      | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 15 | 31   | 20 April 2027     |           | 321.6           | 32.07         | 7.51     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 16 | 30   | 20 May 2027       |           | 288.63          | 32.97         | 6.61     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 17 | 31   | 20 June 2027      |           | 255.18          | 33.45         | 6.13     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 18 | 30   | 20 July 2027      |           | 220.84          | 34.34         | 5.24     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 19 | 31   | 20 August 2027    |           | 185.95          | 34.89         | 4.69     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 20 | 31   | 20 September 2027 |           | 150.32          | 35.63         | 3.95     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 21 | 30   | 20 October 2027   |           | 113.83          | 36.49         | 3.09     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 22 | 31   | 20 November 2027  |           | 76.67           | 37.16         | 2.42     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 23 | 30   | 20 December 2027  |           | 38.66           | 38.01         | 1.57     | 0.0  | 0.0       | 39.58 | 0.0  | 0.0        | 0.0  | 39.58       |
      | 24 | 31   | 20 January 2028   |           | 0.0             | 38.66         | 0.82     | 0.0  | 0.0       | 39.48 | 0.0  | 0.0        | 0.0  | 39.48       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 741.99        | 207.83   | 0.0  | 0.0       | 949.82 | 0.0  | 0.0        | 0.0  | 949.82      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 20 January 2026  | Disbursement     | 741.99 | 0.0       | 0.0      | 0.0  | 0.0       | 741.99       | false    |
    And Customer makes "REAL_TIME" repayment on "20 January 2026" with 300 EUR transaction amount
    Then Loan has 597.69 outstanding amount
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 20 January 2026   |                 | 741.99          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 20 February 2026  | 20 January 2026 | 702.41          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58 | 39.58 | 39.58      | 0.0  | 0.0         |
      | 2  | 28   | 20 March 2026     | 20 January 2026 | 662.83          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58 | 39.58 | 39.58      | 0.0  | 0.0         |
      | 3  | 31   | 20 April 2026     | 20 January 2026 | 623.25          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58 | 39.58 | 39.58      | 0.0  | 0.0         |
      | 4  | 30   | 20 May 2026       | 20 January 2026 | 583.67          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58 | 39.58 | 39.58      | 0.0  | 0.0         |
      | 5  | 31   | 20 June 2026      | 20 January 2026 | 544.09          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58 | 39.58 | 39.58      | 0.0  | 0.0         |
      | 6  | 30   | 20 July 2026      | 20 January 2026 | 504.51          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58 | 39.58 | 39.58      | 0.0  | 0.0         |
      | 7  | 31   | 20 August 2026    | 20 January 2026 | 464.93          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58 | 39.58 | 39.58      | 0.0  | 0.0         |
      | 8  | 31   | 20 September 2026 |                 | 425.35          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58 | 22.94 | 22.94      | 0.0  | 16.64       |
      | 9  | 30   | 20 October 2026   |                 | 425.35          | 0.0           | 39.58    | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 10 | 31   | 20 November 2026  |                 | 425.35          | 0.0           | 39.58    | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 11 | 30   | 20 December 2026  |                 | 406.65          | 18.7          | 20.88    | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 12 | 31   | 20 January 2027   |                 | 375.7           | 30.95         | 8.63     | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 13 | 31   | 20 February 2027  |                 | 344.09          | 31.61         | 7.97     | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 14 | 28   | 20 March 2027     |                 | 311.11          | 32.98         | 6.6      | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 15 | 31   | 20 April 2027     |                 | 278.13          | 32.98         | 6.6      | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 16 | 30   | 20 May 2027       |                 | 244.26          | 33.87         | 5.71     | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 17 | 31   | 20 June 2027      |                 | 209.86          | 34.4          | 5.18     | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 18 | 30   | 20 July 2027      |                 | 174.59          | 35.27         | 4.31     | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 19 | 31   | 20 August 2027    |                 | 138.72          | 35.87         | 3.71     | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 20 | 31   | 20 September 2027 |                 | 102.08          | 36.64         | 2.94     | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 21 | 30   | 20 October 2027   |                 | 64.6            | 37.48         | 2.1      | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 22 | 31   | 20 November 2027  |                 | 26.39           | 38.21         | 1.37     | 0.0  | 0.0       | 39.58 | 0.0   | 0.0        | 0.0  | 39.58       |
      | 23 | 30   | 20 December 2027  |                 | 0.0             | 26.39         | 0.54     | 0.0  | 0.0       | 26.93 | 0.0   | 0.0        | 0.0  | 26.93       |
      | 24 | 31   | 20 January 2028   | 20 January 2026 | 0.0             | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 741.99        | 155.7    | 0.0  | 0.0       | 897.69 | 300.0 | 300.0      | 0.0  | 597.69      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 20 January 2026  | Disbursement     | 741.99 | 0.0       | 0.0      | 0.0  | 0.0       | 741.99       | false    |
      | 20 January 2026  | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 441.99       | false    |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 20 February 2026   | 20 January 2026 | 20 October 2026 |                  |                 |            |                 |
    Then Loan has 690.03 outstanding amount
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 20 January 2026   |                 | 741.99          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 273  | 20 October 2026   | 20 January 2026 | 702.41          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58  | 39.58 | 39.58      | 0.0  | 0.0         |
      | 2  | 31   | 20 November 2026  | 20 January 2026 | 662.83          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58  | 39.58 | 39.58      | 0.0  | 0.0         |
      | 3  | 30   | 20 December 2026  | 20 January 2026 | 623.25          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58  | 39.58 | 39.58      | 0.0  | 0.0         |
      | 4  | 31   | 20 January 2027   | 20 January 2026 | 583.67          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58  | 39.58 | 39.58      | 0.0  | 0.0         |
      | 5  | 31   | 20 February 2027  | 20 January 2026 | 544.09          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58  | 39.58 | 39.58      | 0.0  | 0.0         |
      | 6  | 28   | 20 March 2027     | 20 January 2026 | 504.51          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58  | 39.58 | 39.58      | 0.0  | 0.0         |
      | 7  | 31   | 20 April 2027     | 20 January 2026 | 464.93          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58  | 39.58 | 39.58      | 0.0  | 0.0         |
      | 8  | 30   | 20 May 2027       |                 | 425.35          | 39.58         | 0.0      | 0.0  | 0.0       | 39.58  | 22.94 | 22.94      | 0.0  | 16.64       |
      | 9  | 31   | 20 June 2027      |                 | 425.35          | 0.0           | 39.58    | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 10 | 30   | 20 July 2027      |                 | 425.35          | 0.0           | 39.58    | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 11 | 31   | 20 August 2027    |                 | 425.35          | 0.0           | 39.58    | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 12 | 31   | 20 September 2027 |                 | 425.35          | 0.0           | 39.58    | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 13 | 30   | 20 October 2027   |                 | 418.78          | 6.57          | 33.01    | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 14 | 31   | 20 November 2027  |                 | 388.09          | 30.69         | 8.89     | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 15 | 30   | 20 December 2027  |                 | 356.48          | 31.61         | 7.97     | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 16 | 31   | 20 January 2028   |                 | 324.45          | 32.03         | 7.55     | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 17 | 31   | 20 February 2028  |                 | 291.74          | 32.71         | 6.87     | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 18 | 29   | 20 March 2028     |                 | 257.94          | 33.8          | 5.78     | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 19 | 31   | 20 April 2028     |                 | 223.82          | 34.12         | 5.46     | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 20 | 30   | 20 May 2028       |                 | 188.82          | 35.0          | 4.58     | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 21 | 31   | 20 June 2028      |                 | 153.24          | 35.58         | 4.0      | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 22 | 30   | 20 July 2028      |                 | 116.8           | 36.44         | 3.14     | 0.0  | 0.0       | 39.58  | 0.0   | 0.0        | 0.0  | 39.58       |
      | 23 | 31   | 20 August 2028    |                 | 0.0             | 116.8         | 2.47     | 0.0  | 0.0       | 119.27 | 0.0   | 0.0        | 0.0  | 119.27      |
      | 24 | 31   | 20 September 2028 | 20 January 2026 | 0.0             | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0   | 0.0        | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 741.99        | 248.04   | 0.0  | 0.0       | 990.03 | 300.0 | 300.0      | 0.0  | 690.03      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 20 January 2026  | Disbursement     | 741.99 | 0.0       | 0.0      | 0.0  | 0.0       | 741.99       | false    |
      | 20 January 2026  | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 441.99       | false    |
    When Admin sets the business date to "20 September 2028"
    When Loan Pay-off is made on "20 September 2028"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C89779
  Scenario: Verify reschedule after MIR, CBR and down payment reversal keeps unique due dates, correct balance and delinquency
    When Admin sets the business date to "18 March 2026"
    And Admin creates a client with random data
    And Admin set "LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_NO_MULTIPLES_OF" loan product "DEFAULT" transaction type to "REAMORTIZATION" future installment allocation rule
    And Admin set "LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_NO_MULTIPLES_OF" loan product "MERCHANT_ISSUED_REFUND" transaction type to "REAMORTIZATION" future installment allocation rule
    And Admin creates a fully customized loan with auto downpayment 33.33% and with the following data:
      | LoanProduct                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_NO_MULTIPLES_OF | 18 March 2026     | 131.7          | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 2                 | MONTHS                | 1              | MONTHS                 | 2                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "18 March 2026" with "131.7" amount and expected disbursement date on "18 March 2026"
    And Admin successfully disburse the loan on "18 March 2026" with "131.7" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 18 March 2026 | 18 March 2026 | 87.8            | 43.9          | 0.0      | 0.0  | 0.0       | 43.9  | 43.9  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 18 April 2026 |               | 43.9            | 43.9          | 0.0      | 0.0  | 0.0       | 43.9  | 0.0   | 0.0        | 0.0  | 43.9        |
      | 3  | 30   | 18 May 2026   |               | 0.0             | 43.9          | 0.0      | 0.0  | 0.0       | 43.9  | 0.0   | 0.0        | 0.0  | 43.9        |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement     | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Down Payment     | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 131.7         | 0.0      | 0.0  | 0.0       | 131.7 | 43.9 | 0.0        | 0.0  | 87.8        |
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "18 March 2026" with 131.7 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 18 March 2026 | 18 March 2026 | 87.8            | 43.9          | 0.0      | 0.0  | 0.0       | 43.9  | 43.9  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 18 April 2026 | 18 March 2026 | 43.9            | 43.9          | 0.0      | 0.0  | 0.0       | 43.9  | 43.9  | 43.9       | 0.0  | 0.0         |
      | 3  | 30   | 18 May 2026   | 18 March 2026 | 0.0             | 43.9          | 0.0      | 0.0  | 0.0       | 43.9  | 43.9  | 43.9       | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement           | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Down Payment           | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | false    |
      | 18 March 2026    | Merchant Issued Refund | 131.7  | 87.8      | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 131.7         | 0.0      | 0.0  | 0.0       | 131.7 | 131.7 | 87.8       | 0.0  | 0.0         |
    Then Loan has 43.9 overpaid amount
    When Admin sets the business date to "19 March 2026"
    And Admin makes Credit Balance Refund transaction on "19 March 2026" with 43.9 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 18 March 2026 | 18 March 2026 | 87.8            | 43.9          | 0.0      | 0.0  | 0.0       | 43.9  | 43.9  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 18 April 2026 | 18 March 2026 | 43.9            | 43.9          | 0.0      | 0.0  | 0.0       | 43.9  | 43.9  | 43.9       | 0.0  | 0.0         |
      | 3  | 30   | 18 May 2026   | 18 March 2026 | 0.0             | 43.9          | 0.0      | 0.0  | 0.0       | 43.9  | 43.9  | 43.9       | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement           | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Down Payment           | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | false    |
      | 18 March 2026    | Merchant Issued Refund | 131.7  | 87.8      | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 19 March 2026    | Credit Balance Refund  | 43.9   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 131.7         | 0.0      | 0.0  | 0.0       | 131.7 | 131.7 | 87.8       | 0.0  | 0.0         |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | null        | 0              | 0           |
    Then Loan has 0 overpaid amount
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "24 March 2026"
    And Customer undo "1"th "Down Payment" transaction made on "18 March 2026"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 18 March 2026 | 18 March 2026 | 87.8            | 43.9          | 0.0      | 0.0  | 0.0       | 43.9 | 43.9 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 18 April 2026 |               | 43.9            | 87.8          | 0.0      | 0.0  | 0.0       | 87.8 | 43.9 | 43.9       | 0.0  | 43.9        |
      | 3  | 30   | 18 May 2026   | 18 March 2026 | 0.0             | 43.9          | 0.0      | 0.0  | 0.0       | 43.9 | 43.9 | 43.9       | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement           | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Down Payment           | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | true     |
      | 18 March 2026    | Merchant Issued Refund | 131.7  | 131.7     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 19 March 2026    | Credit Balance Refund  | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 43.9         | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 175.6         | 0.0      | 0.0  | 0.0       | 175.6 | 131.7 | 87.8       | 0.0  | 43.9        |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | null        | 0              | 0           |
    When Admin sets the business date to "15 April 2026"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 18 April 2026      | 15 April 2026   | 18 May 2026     |                  |                 |            |                 |
    Then Loan has 43.9 outstanding amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 18 March 2026 | 18 March 2026 | 87.8            | 43.9          | 0.0      | 0.0  | 0.0       | 43.9 | 43.9 | 0.0        | 0.0  | 0.0         |
      | 2  | 61   | 18 May 2026   |               | 43.9            | 87.8          | 0.0      | 0.0  | 0.0       | 87.8 | 43.9 | 43.9       | 0.0  | 43.9        |
      | 3  | 31   | 18 June 2026  | 18 March 2026 | 0.0             | 43.9          | 0.0      | 0.0  | 0.0       | 43.9 | 43.9 | 43.9       | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement           | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Down Payment           | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | true     |
      | 18 March 2026    | Merchant Issued Refund | 131.7  | 131.7     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 19 March 2026    | Credit Balance Refund  | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 43.9         | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 175.6         | 0.0      | 0.0  | 0.0       | 175.6 | 131.7 | 87.8       | 0.0  | 43.9        |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | null        | 0              | 0           |
    When Admin sets the business date to "1 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan has 43.9 outstanding amount
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | RANGE_30       | 43.9             | 21 May 2026    | 18 May 2026 | 41             | 44          |
    When Admin sets the business date to "2 July 2026"
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | RANGE_30       | 43.9             | 21 May 2026    | 18 May 2026 | 42             | 45          |
    And Customer makes "AUTOPAY" repayment on "2 July 2026" with 43.9 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 18 March 2026 | 18 March 2026 | 87.8            | 43.9          | 0.0      | 0.0  | 0.0       | 43.9 | 43.9 | 0.0        | 0.0  | 0.0         |
      | 2  | 61   | 18 May 2026   | 02 July 2026  | 43.9            | 87.8          | 0.0      | 0.0  | 0.0       | 87.8 | 87.8 | 43.9       | 43.9 | 0.0         |
      | 3  | 31   | 18 June 2026  | 18 March 2026 | 0.0             | 43.9          | 0.0      | 0.0  | 0.0       | 43.9 | 43.9 | 43.9       | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement           | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Down Payment           | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | true     |
      | 18 March 2026    | Merchant Issued Refund | 131.7  | 131.7     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 19 March 2026    | Credit Balance Refund  | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 43.9         | false    |
      | 02 July 2026     | Repayment              | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 175.6         | 0.0      | 0.0  | 0.0       | 175.6 | 175.6 | 87.8       | 43.9 | 0.0         |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | null        | 0              | 0           |
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    And Admin set "LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_NO_MULTIPLES_OF" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin set "LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_NO_MULTIPLES_OF" loan product "MERCHANT_ISSUED_REFUND" transaction type to "NEXT_INSTALLMENT" future installment allocation rule

  @TestRailId:C89780
  Scenario: Verify reschedule after MIR, CBR and repayment reversal keeps unique due dates, correct balance and delinquency
    When Admin sets the business date to "18 March 2026"
    And Admin creates a client with random data
    And Admin set "LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_NO_MULTIPLES_OF" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin set "LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_NO_MULTIPLES_OF" loan product "MERCHANT_ISSUED_REFUND" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with forced disabled downpayment with the following data:
      | LoanProduct                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_NO_MULTIPLES_OF | 18 March 2026     | 131.7          | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 2                 | MONTHS                | 1              | MONTHS                 | 2                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "18 March 2026" with "131.7" amount and expected disbursement date on "18 March 2026"
    And Admin successfully disburse the loan on "18 March 2026" with "131.7" EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |           | 131.7           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 18 April 2026 |           | 65.85           | 65.85         | 0.0      | 0.0  | 0.0       | 65.85 | 0.0  | 0.0        | 0.0  | 65.85       |
      | 2  | 30   | 18 May 2026   |           | 0.0             | 65.85         | 0.0      | 0.0  | 0.0       | 65.85 | 0.0  | 0.0        | 0.0  | 65.85       |
    And Customer makes "AUTOPAY" repayment on "18 March 2026" with 43.9 EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |           | 131.7           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 18 April 2026 |           | 65.85           | 65.85         | 0.0      | 0.0  | 0.0       | 65.85 | 43.9  | 43.9       | 0.0  | 21.95       |
      | 2  | 30   | 18 May 2026   |           | 0.0             | 65.85         | 0.0      | 0.0  | 0.0       | 65.85 | 0.0   | 0.0        | 0.0  | 65.85       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement     | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Repayment        | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 131.7         | 0.0      | 0.0  | 0.0       | 131.7 | 43.9 | 43.9       | 0.0  | 87.8        |
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "18 March 2026" with 131.7 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 18 April 2026 | 18 March 2026 | 65.85           | 65.85         | 0.0      | 0.0  | 0.0       | 65.85 | 65.85 | 65.85      | 0.0  | 0.0         |
      | 2  | 30   | 18 May 2026   | 18 March 2026 | 0.0             | 65.85         | 0.0      | 0.0  | 0.0       | 65.85 | 65.85 | 65.85      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement           | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Repayment              | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | false    |
      | 18 March 2026    | Merchant Issued Refund | 131.7  | 87.8      | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 131.7         | 0.0      | 0.0  | 0.0       | 131.7 | 131.7 | 131.7      | 0.0  | 0.0         |
    Then Loan has 43.9 overpaid amount
    When Admin sets the business date to "19 March 2026"
    And Admin makes Credit Balance Refund transaction on "19 March 2026" with 43.9 EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 18 April 2026 | 18 March 2026 | 65.85           | 65.85         | 0.0      | 0.0  | 0.0       | 65.85 | 65.85 | 65.85      | 0.0  | 0.0         |
      | 2  | 30   | 18 May 2026   | 18 March 2026 | 0.0             | 65.85         | 0.0      | 0.0  | 0.0       | 65.85 | 65.85 | 65.85      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement           | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Repayment              | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | false    |
      | 18 March 2026    | Merchant Issued Refund | 131.7  | 87.8      | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 19 March 2026    | Credit Balance Refund  | 43.9   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 131.7         | 0.0      | 0.0  | 0.0       | 131.7 | 131.7 | 131.7      | 0.0  | 0.0         |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | null        | 0              | 0           |
    Then Loan has 0 overpaid amount
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "24 March 2026"
    And Customer undo "1"th "Repayment" transaction made on "18 March 2026"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 18 April 2026 |               | 65.85           | 109.75        | 0.0      | 0.0  | 0.0       | 109.75 | 65.85 | 65.85      | 0.0  | 43.9        |
      | 2  | 30   | 18 May 2026   | 18 March 2026 | 0.0             | 65.85         | 0.0      | 0.0  | 0.0       | 65.85  | 65.85 | 65.85      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement           | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Repayment              | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | true     |
      | 18 March 2026    | Merchant Issued Refund | 131.7  | 131.7     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 19 March 2026    | Credit Balance Refund  | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 43.9         | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 175.6         | 0.0      | 0.0  | 0.0       | 175.6 | 131.7 | 131.7      | 0.0  | 43.9        |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | null        | 0              | 0           |
    When Admin sets the business date to "15 April 2026"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 18 April 2026      | 15 April 2026   | 18 May 2026     |                  |                 |            |                 |
    Then Loan has 43.9 outstanding amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 61   | 18 May 2026   |               | 65.85           | 109.75        | 0.0      | 0.0  | 0.0       | 109.75 | 65.85 | 65.85      | 0.0  | 43.9        |
      | 2  | 31   | 18 June 2026  | 18 March 2026 | 0.0             | 65.85         | 0.0      | 0.0  | 0.0       | 65.85  | 65.85 | 65.85      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement           | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Repayment              | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | true     |
      | 18 March 2026    | Merchant Issued Refund | 131.7  | 131.7     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 19 March 2026    | Credit Balance Refund  | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 43.9         | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 175.6         | 0.0      | 0.0  | 0.0       | 175.6 | 131.7 | 131.7      | 0.0  | 43.9        |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | null        | 0              | 0           |
    When Admin sets the business date to "1 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan has 43.9 outstanding amount
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | RANGE_30       | 43.9             | 21 May 2026    | 18 May 2026 | 41             | 44          |
    When Admin sets the business date to "2 July 2026"
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | RANGE_30       | 43.9             | 21 May 2026    | 18 May 2026 | 42             | 45          |
    And Customer makes "AUTOPAY" repayment on "2 July 2026" with 43.9 EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 18 March 2026 |               | 131.7           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 61   | 18 May 2026   | 02 July 2026  | 65.85           | 109.75        | 0.0      | 0.0  | 0.0       | 109.75 | 109.75 | 65.85      | 43.9 | 0.0         |
      | 2  | 31   | 18 June 2026  | 18 March 2026 | 0.0             | 65.85         | 0.0      | 0.0  | 0.0       | 65.85  | 65.85  | 65.85      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 18 March 2026    | Disbursement           | 131.7  | 0.0       | 0.0      | 0.0  | 0.0       | 131.7        | false    |
      | 18 March 2026    | Repayment              | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 87.8         | true     |
      | 18 March 2026    | Merchant Issued Refund | 131.7  | 131.7     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 19 March 2026    | Credit Balance Refund  | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 43.9         | false    |
      | 02 July 2026     | Repayment              | 43.9   | 43.9      | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 175.6         | 0.0      | 0.0  | 0.0       | 175.6 | 175.6 | 131.7      | 43.9 | 0.0         |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | pastDueDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | null        | 0              | 0           |
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C89781
  Scenario: Verify reschedule after ReAge keeps unique due dates when a re-aged installment is fully paid in advance
    When Admin sets the business date to "21 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "LAST_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 21 January 2026   | 480            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 January 2026" with "480" amount and expected disbursement date on "21 January 2026"
    When Admin successfully disburse the loan on "21 January 2026" with "480" EUR transaction amount
    Then Loan has 360.0 outstanding amount
    # --- Step 1: payment holiday: push 21 Feb installment to 21 Mar ---
    When Admin sets the business date to "23 January 2026"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 21 February 2026   | 23 January 2026 | 21 March 2026   |                  |                 |            |                 |
    # --- Step 2: ReAge into 15 monthly installments starting 21 March 2026 ---
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling |
      | 1               | MONTHS        | 21 March 2026 | 15                   | DEFAULT               |
    Then Loan has 360.0 outstanding amount
    # --- Step 3: advance repayment fully pays the LAST re-aged installment (future allocation: LAST_INSTALLMENT) ---
    When Admin sets the business date to "24 January 2026"
    And Customer makes "AUTOPAY" repayment on "24 January 2026" with 24 EUR transaction amount
    Then Loan has 336.0 outstanding amount
    And Loan Repayment schedule has 17 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 21 January 2026   |                 | 480.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 21 January 2026   | 21 January 2026 | 360.0           | 120.0         | 0.0      | 0.0  | 0.0       | 120.0 | 120.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 2    | 23 January 2026   | 23 January 2026 | 360.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 57   | 21 March 2026     |                 | 336.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 4  | 31   | 21 April 2026     |                 | 312.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 5  | 30   | 21 May 2026       |                 | 288.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 6  | 31   | 21 June 2026      |                 | 264.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 7  | 30   | 21 July 2026      |                 | 240.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 8  | 31   | 21 August 2026    |                 | 216.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 9  | 31   | 21 September 2026 |                 | 192.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 10 | 30   | 21 October 2026   |                 | 168.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 11 | 31   | 21 November 2026  |                 | 144.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 12 | 30   | 21 December 2026  |                 | 120.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 13 | 31   | 21 January 2027   |                 | 96.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 14 | 31   | 21 February 2027  |                 | 72.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 15 | 28   | 21 March 2027     |                 | 48.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 16 | 31   | 21 April 2027     |                 | 24.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 17 | 30   | 21 May 2027       | 24 January 2026 | 0.0             | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 24.0  | 24.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 480.0         | 0.0      | 0.0  | 0.0       | 480.0 | 144.0 | 24.0       | 0.0  | 336.0       |
    # --- Step 4: reschedule first re-aged installment 21 Mar -> 21 May: ALL re-aged installments incl. the paid one must cascade by 2 months ---
    When Admin sets the business date to "28 January 2026"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 21 March 2026      | 28 January 2026 | 21 May 2026     |                  |                 |            |                 |
    Then Loan has 336.0 outstanding amount
    Then Loan Repayment schedule has 17 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 21 January 2026   |                 | 480.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 21 January 2026   | 21 January 2026 | 360.0           | 120.0         | 0.0      | 0.0  | 0.0       | 120.0 | 120.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 2    | 23 January 2026   | 23 January 2026 | 360.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 118  | 21 May 2026       |                 | 336.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 4  | 31   | 21 June 2026      |                 | 312.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 5  | 30   | 21 July 2026      |                 | 288.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 6  | 31   | 21 August 2026    |                 | 264.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 7  | 31   | 21 September 2026 |                 | 240.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 8  | 30   | 21 October 2026   |                 | 216.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 9  | 31   | 21 November 2026  |                 | 192.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 10 | 30   | 21 December 2026  |                 | 168.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 11 | 31   | 21 January 2027   |                 | 144.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 12 | 31   | 21 February 2027  |                 | 120.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 13 | 28   | 21 March 2027     |                 | 96.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 14 | 31   | 21 April 2027     |                 | 72.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 15 | 30   | 21 May 2027       |                 | 48.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 16 | 31   | 21 June 2027      |                 | 24.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 17 | 30   | 21 July 2027      | 24 January 2026 | 0.0             | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 24.0  | 24.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 480.0         | 0.0      | 0.0  | 0.0       | 480.0 | 144.0 | 24.0       | 0.0  | 336.0       |
    # --- Step 5: backdated repayment triggers full reprocessing replay; schedule must keep unique cascaded dates ---
    When Admin sets the business date to "5 February 2026"
    And Customer makes "AUTOPAY" repayment on "1 February 2026" with 24 EUR transaction amount
    Then Loan has 312.0 outstanding amount
    Then Loan Repayment schedule has 17 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 21 January 2026   |                  | 480.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 21 January 2026   | 21 January 2026  | 360.0           | 120.0         | 0.0      | 0.0  | 0.0       | 120.0 | 120.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 2    | 23 January 2026   | 23 January 2026  | 360.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 118  | 21 May 2026       |                  | 336.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 4  | 31   | 21 June 2026      |                  | 312.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 5  | 30   | 21 July 2026      |                  | 288.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 6  | 31   | 21 August 2026    |                  | 264.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 7  | 31   | 21 September 2026 |                  | 240.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 8  | 30   | 21 October 2026   |                  | 216.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 9  | 31   | 21 November 2026  |                  | 192.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 10 | 30   | 21 December 2026  |                  | 168.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 11 | 31   | 21 January 2027   |                  | 144.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 12 | 31   | 21 February 2027  |                  | 120.0           | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 13 | 28   | 21 March 2027     |                  | 96.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 14 | 31   | 21 April 2027     |                  | 72.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 15 | 30   | 21 May 2027       |                  | 48.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 0.0   | 0.0        | 0.0  | 24.0        |
      | 16 | 31   | 21 June 2027      | 01 February 2026 | 24.0            | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 24.0  | 24.0       | 0.0  | 0.0         |
      | 17 | 30   | 21 July 2027      | 24 January 2026  | 0.0             | 24.0          | 0.0      | 0.0  | 0.0       | 24.0  | 24.0  | 24.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 480.0         | 0.0      | 0.0  | 0.0       | 480.0 | 168.0 | 48.0       | 0.0  | 312.0       |
    And Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule

  @TestRailId:C111028 @AdvancedPaymentAllocation
  Scenario: Verify reschedule with extra terms after re-aging with a start date on the 31st keeps month-end due dates
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    When Admin sets the business date to "15 March 2024"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 31 May 2024   | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 77   | 31 May 2024       |                  | 69.64           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 5  | 30   | 30 June 2024      |                  | 55.71           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 6  | 31   | 31 July 2024      |                  | 41.78           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 7  | 31   | 31 August 2024    |                  | 27.85           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 8  | 30   | 30 September 2024 |                  | 13.92           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 9  | 31   | 31 October 2024   |                  | 0.0             | 13.92         | 0.11     | 0.0  | 0.0       | 14.03 | 0.0   | 0.0        | 0.0  | 14.03       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.29     | 0.0  | 0.0       | 101.29 | 17.01 | 0.0        | 0.0  | 84.28       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    |
    When Admin sets the business date to "02 April 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 31 August 2024     | 02 April 2024   |                 |                  |                 | 2          |                 |
    Then Loan Repayment schedule has 11 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 77   | 31 May 2024       |                  | 69.64           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 5  | 30   | 30 June 2024      |                  | 55.71           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 6  | 31   | 31 July 2024      |                  | 41.78           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 7  | 31   | 31 August 2024    |                  | 33.47           | 8.31          | 0.12     | 0.0  | 0.0       | 8.43  | 0.0   | 0.0        | 0.0  | 8.43        |
      | 8  | 30   | 30 September 2024 |                  | 25.16           | 8.31          | 0.12     | 0.0  | 0.0       | 8.43  | 0.0   | 0.0        | 0.0  | 8.43        |
      | 9  | 31   | 31 October 2024   |                  | 16.84           | 8.32          | 0.11     | 0.0  | 0.0       | 8.43  | 0.0   | 0.0        | 0.0  | 8.43        |
      | 10 | 30   | 30 November 2024  |                  | 8.41            | 8.43          | 0.0      | 0.0  | 0.0       | 8.43  | 0.0   | 0.0        | 0.0  | 8.43        |
      | 11 | 31   | 31 December 2024  |                  | 0.0             | 8.41          | 0.0      | 0.0  | 0.0       | 8.41  | 0.0   | 0.0        | 0.0  | 8.41        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.29     | 0.0  | 0.0       | 101.29 | 17.01 | 0.0        | 0.0  | 84.28       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    |
    When Loan Pay-off is made on "02 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
