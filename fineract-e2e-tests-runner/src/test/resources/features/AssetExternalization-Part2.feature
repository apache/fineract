@AssetExternalizationFeature
Feature: Asset Externalization - Part2

  @TestRailId:C3800 @AssetExternalizationJournalEntry
  Scenario: Verify manual journal entry with External Asset Owner empty value if asset-externalization is enabled - UC2
    Given Global configuration "asset-externalization-of-non-active-loans" is enabled
    When Admin sets the business date to "10 June 2025"
    Then Admin creates manual Journal entry with "88" amount and "10 June 2025" date and without External Asset Owner
    Then Verify manual Journal entry with External Asset Owner "true" and with the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit | Manual Entry |
      | ASSET     | 112601       | Loans Receivable          | 88.0  |        | true         |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 88.0   | true         |
    Given Global configuration "asset-externalization-of-non-active-loans" is enabled

  @TestRailId:C3801 @AssetExternalizationJournalEntry
  Scenario: Verify manual journal entry with External Asset Owner empty value if asset-externalization is enabled for existing loan - UC3
    Given Global configuration "asset-externalization-of-non-active-loans" is enabled

    When Admin sets the business date to "1 June 2025"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 June 2025"
    And Admin successfully approves the loan on "1 June 2025" with "1000" amount and expected disbursement date on "1 June 2025"
    When Admin successfully disburse the loan on "1 June 2025" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2025-06-01     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2025-06-01     | 1                  | PENDING | 2025-06-01    | 9999-12-31  | SALE             |

    When Admin sets the business date to "27 June 2025"
    Then Admin creates manual Journal entry with "99" amount and "27 June 2025" date and unique External Asset Owner
    Then Verify manual Journal entry with External Asset Owner "true" and with the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit | Manual Entry |
      | ASSET     | 112601       | Loans Receivable          | 99.0  |        | true         |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 99.0   | true         |
    Given Global configuration "asset-externalization-of-non-active-loans" is enabled

    When Loan Pay-off is made on "26 June 2025"
    Then Loan's all installments have obligations met

  @TestRailId:C3821 @AssetExternalizationJournalEntry
  Scenario: Verify manual journal entry with no External Asset Owner value if asset-externalization is disabled - UC4
    Given Global configuration "asset-externalization-of-non-active-loans" is disabled
    When Admin sets the business date to "25 June 2025"
    Then Admin creates manual Journal entry with "250.05" amount and "15 June 2025" date and without External Asset Owner
    Then Verify manual Journal entry with External Asset Owner "false" and with the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit | Manual Entry |
      | ASSET     | 112601       | Loans Receivable          | 250.05 |        | true         |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 250.05 | true         |
    Given Global configuration "asset-externalization-of-non-active-loans" is enabled

  @TestRailId:C3991
  Scenario: Verify asset externalization previous owner for intermediarySale transfer with following SALES request - UC1
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DELAYED_SETTLEMENT" for loan product "LP1_DUE_DATE"
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_DUE_DATE | 01 May 2023       | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | intermediarySale | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 9999-12-31  | INTERMEDIARYSALE |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 9999-12-31  | INTERMEDIARYSALE |
    Then LoanOwnershipTransferBusinessEvent with transfer type: "INTERMEDIARYSALE" and transfer asset owner is created
    When Admin sets the business date to "14 June 2023"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-06-14     | 1                  |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 9999-12-31  | INTERMEDIARYSALE |
      | 2023-06-14     | 1                  | PENDING              | 2023-06-14    | 9999-12-31  | SALE             |
    When Admin sets the business date to "15 June 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 4 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 2023-06-14  | INTERMEDIARYSALE |
      | 2023-06-14     | 1                  | PENDING              | 2023-06-14    | 2023-06-14  | SALE             |
      | 2023-06-14     | 1                  | ACTIVE               | 2023-06-15    | 9999-12-31  | SALE             |
    Then LoanOwnershipTransferBusinessEvent with transfer type: "SALE" and transfer asset owner based on intermediarySale is created
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DEFAULT_SETTLEMENT" for loan product "LP1_DUE_DATE"

  @TestRailId:C3992
  Scenario: Verify asset externalization previous owner for intermediarySale transfer with following SALES and BUYBACK requests - UC2
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DELAYED_SETTLEMENT" for loan product "LP1_DUE_DATE"
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_DUE_DATE | 01 May 2023       | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | intermediarySale | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 9999-12-31  | INTERMEDIARYSALE |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 9999-12-31  | INTERMEDIARYSALE |
    Then LoanOwnershipTransferBusinessEvent with transfer type: "INTERMEDIARYSALE" and transfer asset owner is created

    When Admin sets the business date to "14 June 2023"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-06-14     | 1                  |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 9999-12-31  | INTERMEDIARYSALE |
      | 2023-06-14     | 1                  | PENDING              | 2023-06-14    | 9999-12-31  | SALE             |
    When Admin sets the business date to "15 June 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 4 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 2023-06-14  | INTERMEDIARYSALE |
      | 2023-06-14     | 1                  | PENDING              | 2023-06-14    | 2023-06-14  | SALE             |
      | 2023-06-14     | 1                  | ACTIVE               | 2023-06-15    | 9999-12-31  | SALE             |
    Then LoanOwnershipTransferBusinessEvent with transfer type: "SALE" and transfer asset owner based on intermediarySale is created

    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-06-16     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 5 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 2023-06-14  | INTERMEDIARYSALE |
      | 2023-06-14     | 1                  | PENDING              | 2023-06-14    | 2023-06-14  | SALE             |
      | 2023-06-14     | 1                  | ACTIVE               | 2023-06-15    | 9999-12-31  | SALE             |
      | 2023-06-16     | 1                  | BUYBACK              | 2023-06-15    | 9999-12-31  | BUYBACK          |
    When Admin sets the business date to "17 June 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 5 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 2023-06-14  | INTERMEDIARYSALE |
      | 2023-06-14     | 1                  | PENDING              | 2023-06-14    | 2023-06-14  | SALE             |
      | 2023-06-14     | 1                  | ACTIVE               | 2023-06-15    | 2023-06-16  | SALE             |
      | 2023-06-16     | 1                  | BUYBACK              | 2023-06-15    | 2023-06-16  | BUYBACK          |
    Then LoanOwnershipTransferBusinessEvent with transfer type: "BUYBACK" and transfer asset owner is created
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DEFAULT_SETTLEMENT" for loan product "LP1_DUE_DATE"

  @TestRailId:C3993
  Scenario: Verify asset externalization previous owner for intermediarySale transfer with following BUYBACK requests - UC3
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DELAYED_SETTLEMENT" for loan product "LP1_DUE_DATE"
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_DUE_DATE | 01 May 2023       | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | intermediarySale | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 9999-12-31  | INTERMEDIARYSALE |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 9999-12-31  | INTERMEDIARYSALE |
    Then LoanOwnershipTransferBusinessEvent with transfer type: "INTERMEDIARYSALE" and transfer asset owner is created
    When Admin sets the business date to "14 June 2023"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-06-14     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 9999-12-31  | INTERMEDIARYSALE |
      | 2023-06-14     | 1                  | BUYBACK_INTERMEDIATE | 2023-06-14    | 9999-12-31  | BUYBACK          |
    When Admin sets the business date to "15 June 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING_INTERMEDIATE | 2023-05-01    | 2023-05-21  | INTERMEDIARYSALE |
      | 2023-05-21     | 1                  | ACTIVE_INTERMEDIATE  | 2023-05-22    | 2023-06-14  | INTERMEDIARYSALE |
      | 2023-06-14     | 1                  | BUYBACK_INTERMEDIATE | 2023-06-14    | 2023-06-14  | BUYBACK          |
    Then LoanOwnershipTransferBusinessEvent with transfer type: "BUYBACK" and transfer asset owner based on intermediarySale is created
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DEFAULT_SETTLEMENT" for loan product "LP1_DUE_DATE"

  @TestRailId:C4640
  Scenario: Verify creation of new external asset owner and it presence in the list
    When Admin creates a new external asset owner with a unique ownerExternalId
    Then External asset owner creation response has a non-null resourceId
    Then External asset owner list contains the created owner

  @TestRailId:C4641
  Scenario: Verify creation of an external asset owner fails for null, duplicate and empty ownerExternalId
    When Admin tries to create an external asset owner with null ownerExternalId then it should fail with 400 status code
    When Admin tries to create an external asset owner with empty JSON body then it should fail with 400 status code
    When Admin creates a new external asset owner with a unique ownerExternalId
    Then External asset owner creation response has a non-null resourceId
    When Admin tries to create an external asset owner with a duplicate ownerExternalId then it should fail with 403 status code

  @TestRailId:C4642
  Scenario: Verify creation of multiple external asset owners and presence of all items the list
    When Admin creates a new external asset owner with a unique ownerExternalId
    Then External asset owner creation response has a non-null resourceId
    Then External asset owner list contains the created owner
    When Admin creates a new external asset owner with a unique ownerExternalId
    Then External asset owner creation response has a non-null resourceId
    Then External asset owner list contains the created owner
    Then Admin retrieves all external asset owners successfully

  @TestRailId:C72360
  Scenario: Verify that when a loan with PENDING owner-to-owner SALES is fully paid asset transfer is DECLINED and original owner remains active
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin sets the business date to "25 May 2023"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-06-14     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "28 May 2023"
    And Customer makes "AUTOPAY" repayment on "28 May 2023" with 1000 EUR transaction amount and check previous external owner
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Fetching Asset externalization details by loan id gives numberOfElements: 4 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status   | effectiveFrom | effectiveTo | Transaction type |
      | 2023-06-14     | 1                  | DECLINED | 2023-05-28    | 2023-05-28  | SALE             |
    Then LoanOwnershipTransferBusinessEvent with transfer status: "DECLINED" and transfer status reason "BALANCE_ZERO" is created

  @TestRailId:C72361
  Scenario: Verify that when a loan with PENDING owner-to-owner SALES is overpaid asset transfer is DECLINED and original owner remains active
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin sets the business date to "25 May 2023"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-06-14     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "28 May 2023"
    And Customer makes "AUTOPAY" repayment on "28 May 2023" with 1200 EUR transaction amount and check previous external owner
    Then Loan status will be "OVERPAID"
    Then Fetching Asset externalization details by loan id gives numberOfElements: 4 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status   | effectiveFrom | effectiveTo | Transaction type |
      | 2023-06-14     | 1                  | DECLINED | 2023-05-28    | 2023-05-28  | SALE             |
    Then LoanOwnershipTransferBusinessEvent with transfer status: "DECLINED" and transfer status reason "BALANCE_NEGATIVE" is created

  @TestRailId:C72362
  Scenario: Verify owner-to-owner transfer completes via COB and next repayment accounting goes to new owner
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin sets the business date to "25 May 2023"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-25     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "26 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then The latest asset externalization transaction with "ACTIVE" status has the following TRANSFER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | CREDIT    | 1000.00 |
      | ASSET         | 146000        | Asset transfer   | DEBIT     | 1000.00 |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
      | ASSET         | 146000        | Asset transfer   | CREDIT    | 1000.00 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "26 May 2023" with 200 EUR transaction amount and system-generated Idempotency key and check external owner
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName             | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable          | DEBIT     | 1000.00 |
      | ASSET         | 112601        | Loans Receivable          | CREDIT    | 200.00  |
      | LIABILITY     | 145023        | Suspense/Clearing account | DEBIT     | 200.00  |

  @TestRailId:C72363
  Scenario: Verify owner-to-owner repayment accounting goes to old owner while PENDING transfer not yet settled
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
    When Admin sets the business date to "25 May 2023"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-06-14     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "25 May 2023" with 200 EUR transaction amount and system-generated Idempotency key and check previous external owner

  @TestRailId:C72364
  Scenario: Verify chained owner-to-owner transfers complete successfully
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin sets the business date to "25 May 2023"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-25     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "26 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
    When Admin sets the business date to "28 May 2023"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-28     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "29 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "29 May 2023" with 300 EUR transaction amount and system-generated Idempotency key and check external owner
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName             | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable          | DEBIT     | 1000.00 |
      | ASSET         | 112601        | Loans Receivable          | CREDIT    | 300.00  |
      | LIABILITY     | 145023        | Suspense/Clearing account | DEBIT     | 300.00  |

  @TestRailId:C72365
  Scenario: Verify cancel of PENDING owner-to-owner transfer before COB preserves original owner
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin sets the business date to "25 May 2023"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-30     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin send "cancel" command on "PENDING" transaction
    Then Fetching Asset externalization details by loan id gives numberOfElements: 4 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status    | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-30     | 1                  | CANCELLED | 2023-05-25    | 2023-05-25  | SALE             |
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "25 May 2023" with 200 EUR transaction amount and system-generated Idempotency key and check previous external owner

  @TestRailId:C72366
  Scenario: Verify buyback is blocked while PENDING owner-to-owner transfer exists
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin sets the business date to "25 May 2023"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-30     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Asset externalization transaction with the following data results a 403 error and "BUYBACK_ALREADY_IN_PROGRESS_CANNOT_BE_BOUGHT" error message
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-06-01     |                    |
