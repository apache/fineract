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
    When Loan Pay-off is made on "15 June 2023" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

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
    When Loan Pay-off is made on "17 June 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

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
    When Loan Pay-off is made on "15 June 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

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
    When Loan Pay-off is made on "26 May 2023" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

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
    When Loan Pay-off is made on "25 May 2023" with previous transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

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
    When Loan Pay-off is made on "29 May 2023" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

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
    When Loan Pay-off is made on "25 May 2023" with previous transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

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
    When Loan Pay-off is made on "25 May 2023" with previous transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C85346
  Scenario: Verify owner-to-owner transfer completes via COB, Recognition of Fully Deferred Capitalized Income has correct journal entries
    When Admin sets the business date to "1 January 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2026   | 3000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2026" with "3000" amount and expected disbursement date on "1 January 2026"
    And Admin successfully disburse the loan on "1 January 2026" with "1500" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |           | 1500.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2026 |           | 1002.91         | 497.09        | 8.75     | 0.0  | 0.0       | 505.84 | 0.0  | 0.0        | 0.0  | 505.84      |
      | 2  | 28   | 01 March 2026    |           | 502.92          | 499.99        | 5.85     | 0.0  | 0.0       | 505.84 | 0.0  | 0.0        | 0.0  | 505.84      |
      | 3  | 31   | 01 April 2026    |           | 0.0             | 502.92        | 2.93     | 0.0  | 0.0       | 505.85 | 0.0  | 0.0        | 0.0  | 505.85      |

    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "1 January 2026" with "900" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |           | 1500.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 01 January 2026  |           | 900.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2026 |           | 1604.65         | 795.35        | 14.0     | 0.0  | 0.0       | 809.35 | 0.0  | 0.0        | 0.0  | 809.35      |
      | 2  | 28   | 01 March 2026    |           | 804.66          | 799.99        | 9.36     | 0.0  | 0.0       | 809.35 | 0.0  | 0.0        | 0.0  | 809.35      |
      | 3  | 31   | 01 April 2026    |           | 0.0             | 804.66        | 4.69     | 0.0  | 0.0       | 809.35 | 0.0  | 0.0        | 0.0  | 809.35      |

    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement       | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    |
      | 01 January 2026  | Capitalized Income | 900.0  | 900.0     | 0.0      | 0.0  | 0.0       | 2400.0       | false    |
    When Admin sets the business date to "2 January 2026"
    When Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                    | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    |
      | 01 January 2026  | Capitalized Income              | 900.0  | 900.0     | 0.0      | 0.0  | 0.0       | 2400.0       | false    |
      | 01 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-01-15     | 1                  |

    When Admin sets the business date to "15 January 2026"
    When Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                    | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    |
      | 01 January 2026  | Capitalized Income              | 900.0  | 900.0     | 0.0      | 0.0  | 0.0       | 2400.0       | false    |
      | 01 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Accrual                         | 0.46   | 0.0       | 0.46     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2026  | Accrual                         | 0.46   | 0.0       | 0.46     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |

    When Admin sets the business date to "16 January 2026"
    When Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                    | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    |
      | 01 January 2026  | Capitalized Income              | 900.0  | 900.0     | 0.0      | 0.0  | 0.0       | 2400.0       | false    |
      | 01 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Accrual                         | 0.46   | 0.0       | 0.46     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2026  | Accrual                         | 0.46   | 0.0       | 0.46     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Capitalized Income Amortization | 750.0  | 0.0       | 750.0    | 0.0  | 0.0       | 0.0          | false    |

    And Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 404000       | Interest Income             |       | 750.0  |
      | LIABILITY | 145024       | Deferred Capitalized Income | 750.0 |        |
      | INCOME    | 404000       | Interest Income             |       | 10.0   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 10.0  |        |

    When Admin sets the business date to "17 January 2026"
    When Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                    | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    |
      | 01 January 2026  | Capitalized Income              | 900.0  | 900.0     | 0.0      | 0.0  | 0.0       | 2400.0       | false    |
      | 01 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Accrual                         | 0.46   | 0.0       | 0.46     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2026  | Accrual                         | 0.46   | 0.0       | 0.46     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Capitalized Income Amortization | 750.0  | 0.0       | 750.0    | 0.0  | 0.0       | 0.0          | false    |
      | 16 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2026-01-20     |                    |

    When Admin sets the business date to "21 January 2026"
    When Admin runs inline COB job for Loan
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-02-01     | 1                  |
    When Admin sets the business date to "2 February 2026"
    When Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                    | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    |
      | 01 January 2026  | Capitalized Income              | 900.0  | 900.0     | 0.0      | 0.0  | 0.0       | 2400.0       | false    |
      | 01 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Accrual                         | 0.46   | 0.0       | 0.46     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2026  | Accrual                         | 0.46   | 0.0       | 0.46     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Capitalized Income Amortization | 750.0  | 0.0       | 750.0    | 0.0  | 0.0       | 0.0          | false    |
      | 16 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 17 January 2026  | Accrual                         | 0.46   | 0.0       | 0.46     | 0.0  | 0.0       | 0.0          | false    |
      | 18 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 19 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 20 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 21 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 22 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 23 January 2026  | Accrual                         | 0.46   | 0.0       | 0.46     | 0.0  | 0.0       | 0.0          | false    |
      | 24 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 25 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 26 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 27 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 28 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 29 January 2026  | Accrual                         | 0.46   | 0.0       | 0.46     | 0.0  | 0.0       | 0.0          | false    |
      | 30 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 31 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 01 February 2026 | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
    When Admin sets the business date to "22 February 2026"
    When Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement                    | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    |
      | 01 January 2026  | Capitalized Income              | 900.0  | 900.0     | 0.0      | 0.0  | 0.0       | 2400.0       | false    |
      | 01 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Accrual                         | 0.46   | 0.0       | 0.46     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2026  | Accrual                         | 0.46   | 0.0       | 0.46     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Capitalized Income Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Capitalized Income Amortization | 750.0  | 0.0       | 750.0    | 0.0  | 0.0       | 0.0          | false    |
      | 16 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 17 January 2026  | Accrual                         | 0.46   | 0.0       | 0.46     | 0.0  | 0.0       | 0.0          | false    |
      | 18 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 19 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 20 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 21 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 22 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 23 January 2026  | Accrual                         | 0.46   | 0.0       | 0.46     | 0.0  | 0.0       | 0.0          | false    |
      | 24 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 25 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 26 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 27 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 28 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 29 January 2026  | Accrual                         | 0.46   | 0.0       | 0.46     | 0.0  | 0.0       | 0.0          | false    |
      | 30 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 31 January 2026  | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 01 February 2026 | Accrual                         | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    |
      | 02 February 2026 | Accrual                         | 0.5    | 0.0       | 0.5      | 0.0  | 0.0       | 0.0          | false    |
      | 03 February 2026 | Accrual                         | 0.5    | 0.0       | 0.5      | 0.0  | 0.0       | 0.0          | false    |
      | 04 February 2026 | Accrual                         | 0.5    | 0.0       | 0.5      | 0.0  | 0.0       | 0.0          | false    |
      | 05 February 2026 | Accrual                         | 0.5    | 0.0       | 0.5      | 0.0  | 0.0       | 0.0          | false    |
      | 06 February 2026 | Accrual                         | 0.5    | 0.0       | 0.5      | 0.0  | 0.0       | 0.0          | false    |
      | 07 February 2026 | Accrual                         | 0.5    | 0.0       | 0.5      | 0.0  | 0.0       | 0.0          | false    |
      | 08 February 2026 | Accrual                         | 0.5    | 0.0       | 0.5      | 0.0  | 0.0       | 0.0          | false    |
      | 09 February 2026 | Accrual                         | 0.5    | 0.0       | 0.5      | 0.0  | 0.0       | 0.0          | false    |
      | 10 February 2026 | Accrual                         | 0.5    | 0.0       | 0.5      | 0.0  | 0.0       | 0.0          | false    |
      | 11 February 2026 | Accrual                         | 0.5    | 0.0       | 0.5      | 0.0  | 0.0       | 0.0          | false    |
      | 12 February 2026 | Accrual                         | 0.5    | 0.0       | 0.5      | 0.0  | 0.0       | 0.0          | false    |
      | 13 February 2026 | Accrual                         | 0.5    | 0.0       | 0.5      | 0.0  | 0.0       | 0.0          | false    |
      | 14 February 2026 | Accrual                         | 0.5    | 0.0       | 0.5      | 0.0  | 0.0       | 0.0          | false    |
      | 15 February 2026 | Accrual                         | 0.5    | 0.0       | 0.5      | 0.0  | 0.0       | 0.0          | false    |
      | 16 February 2026 | Accrual                         | 0.5    | 0.0       | 0.5      | 0.0  | 0.0       | 0.0          | false    |
      | 17 February 2026 | Accrual                         | 0.5    | 0.0       | 0.5      | 0.0  | 0.0       | 0.0          | false    |
      | 18 February 2026 | Accrual                         | 0.5    | 0.0       | 0.5      | 0.0  | 0.0       | 0.0          | false    |
      | 19 February 2026 | Accrual                         | 0.5    | 0.0       | 0.5      | 0.0  | 0.0       | 0.0          | false    |
      | 20 February 2026 | Accrual                         | 0.5    | 0.0       | 0.5      | 0.0  | 0.0       | 0.0          | false    |
      | 21 February 2026 | Accrual                         | 0.5    | 0.0       | 0.5      | 0.0  | 0.0       | 0.0          | false    |

    When Loan Pay-off is made on "22 February 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C85347
  Scenario: Verify owner-to-owner transfer completes via COB, Recognition of Fully Deferred Buydown Fee has correct journal entries
    When Admin sets the business date to "1 January 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 January 2024   | 3000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2026" with "3000" amount and expected disbursement date on "1 January 2026"
    And Admin successfully disburse the loan on "1 January 2026" with "1500" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |           | 1500.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2026 |           | 1002.91         | 497.09        | 8.75     | 0.0  | 0.0       | 505.84 | 0.0  | 0.0        | 0.0  | 505.84      |
      | 2  | 28   | 01 March 2026    |           | 502.92          | 499.99        | 5.85     | 0.0  | 0.0       | 505.84 | 0.0  | 0.0        | 0.0  | 505.84      |
      | 3  | 31   | 01 April 2026    |           | 0.0             | 502.92        | 2.93     | 0.0  | 0.0       | 505.85 | 0.0  | 0.0        | 0.0  | 505.85      |

    When Admin adds buy down fee with "AUTOPAY" payment type to the loan on "01 January 2026" with "900" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |           | 1500.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2026 |           | 1002.91         | 497.09        | 8.75     | 0.0  | 0.0       | 505.84 | 0.0  | 0.0        | 0.0  | 505.84      |
      | 2  | 28   | 01 March 2026    |           | 502.92          | 499.99        | 5.85     | 0.0  | 0.0       | 505.84 | 0.0  | 0.0        | 0.0  | 505.84      |
      | 3  | 31   | 01 April 2026    |           | 0.0             | 502.92        | 2.93     | 0.0  | 0.0       | 505.85 | 0.0  | 0.0        | 0.0  | 505.85      |

    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement     | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    |
      | 01 January 2026  | Buy Down Fee     | 900.0  | 0.0       | 900.0    | 0.0  | 0.0       | 0.0          | false    |
    When Admin sets the business date to "2 January 2026"
    When Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    |
      | 01 January 2026  | Buy Down Fee              | 900.0  | 0.0       | 900.0    | 0.0  | 0.0       | 0.0          | false    |
      | 01 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-01-15     | 1                  |

    When Admin sets the business date to "15 January 2026"
    When Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    |
      | 01 January 2026  | Buy Down Fee              | 900.0  | 0.0       | 900.0    | 0.0  | 0.0       | 0.0          | false    |
      | 01 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Accrual                   | 0.29   | 0.0       | 0.29     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2026  | Accrual                   | 0.29   | 0.0       | 0.29     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2026  | Accrual                   | 0.29   | 0.0       | 0.29     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |

    When Admin sets the business date to "16 January 2026"
    When Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    |
      | 01 January 2026  | Buy Down Fee              | 900.0  | 0.0       | 900.0    | 0.0  | 0.0       | 0.0          | false    |
      | 01 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Accrual                   | 0.29   | 0.0       | 0.29     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2026  | Accrual                   | 0.29   | 0.0       | 0.29     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2026  | Accrual                   | 0.29   | 0.0       | 0.29     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Buy Down Fee Amortization | 750.0  | 0.0       | 750.0    | 0.0  | 0.0       | 0.0          | false    |

    And Loan Transactions tab has a "BUY_DOWN_FEE_AMORTIZATION" transaction with date "15 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 450281       | Income From Buy Down        |       | 750.0  |
      | LIABILITY | 145024       | Deferred Capitalized Income | 750.0 |        |
      | INCOME    | 450281       | Income From Buy Down        |       | 10.0   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 10.0  |        |

    When Admin sets the business date to "17 January 2026"
    When Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    |
      | 01 January 2026  | Buy Down Fee              | 900.0  | 0.0       | 900.0    | 0.0  | 0.0       | 0.0          | false    |
      | 01 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Accrual                   | 0.29   | 0.0       | 0.29     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2026  | Accrual                   | 0.29   | 0.0       | 0.29     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2026  | Accrual                   | 0.29   | 0.0       | 0.29     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Buy Down Fee Amortization | 750.0  | 0.0       | 750.0    | 0.0  | 0.0       | 0.0          | false    |
      | 16 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2026-01-20     |                    |

    When Admin sets the business date to "21 January 2026"
    When Admin runs inline COB job for Loan
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-01-25     | 1                  |
    When Admin sets the business date to "26 January 2026"
    When Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2026  | Disbursement              | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    |
      | 01 January 2026  | Buy Down Fee              | 900.0  | 0.0       | 900.0    | 0.0  | 0.0       | 0.0          | false    |
      | 01 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 03 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Accrual                   | 0.29   | 0.0       | 0.29     | 0.0  | 0.0       | 0.0          | false    |
      | 04 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 05 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 06 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 07 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2026  | Accrual                   | 0.29   | 0.0       | 0.29     | 0.0  | 0.0       | 0.0          | false    |
      | 08 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 09 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 10 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 11 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 12 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2026  | Accrual                   | 0.29   | 0.0       | 0.29     | 0.0  | 0.0       | 0.0          | false    |
      | 13 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 14 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Buy Down Fee Amortization | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          | false    |
      | 15 January 2026  | Buy Down Fee Amortization | 750.0  | 0.0       | 750.0    | 0.0  | 0.0       | 0.0          | false    |
      | 16 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 17 January 2026  | Accrual                   | 0.29   | 0.0       | 0.29     | 0.0  | 0.0       | 0.0          | false    |
      | 18 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 19 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 20 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 21 January 2026  | Accrual                   | 0.29   | 0.0       | 0.29     | 0.0  | 0.0       | 0.0          | false    |
      | 22 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 23 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 24 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |
      | 25 January 2026  | Accrual                   | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    |

    When Loan Pay-off is made on "26 January 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C85406
  Scenario: Verify buy down fee partial adjustment after full amortization via investor sale - UC1: results in correct amortization adjustment amount
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 July 2026      | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "1000" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "1000" EUR transaction amount
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "01 July 2026" with "50" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-01     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    And Buy down fee contains the following data:
      | Date         | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 01 July 2026 | 50.0       | 0.0              | 50.0                     | 0.0             | 0.0                |
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2026-07-01     | 1                  | PENDING | 2026-07-01    | 2026-07-01  | SALE             |
      | 2026-07-01     | 1                  | ACTIVE  | 2026-07-02    | 9999-12-31  | SALE             |
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement              | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Buy Down Fee Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Buy Down Fee Amortization | 49.46  | 0.0       | 49.46    | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date         | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 01 July 2026 | 50.0       | 50.0             | 0.0                      | 0.0             | 0.0                |
    And LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent is created on "01 July 2026"
    And Admin adds buy down fee adjustment with "AUTOPAY" payment type to the loan on "02 July 2026" with "20" EUR transaction amount
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement              | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Buy Down Fee Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Buy Down Fee Amortization | 49.46  | 0.0       | 49.46    | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Buy Down Fee Adjustment   | 20.0   | 0.0       | 20.0     | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date         | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 01 July 2026 | 50.0       | 30.0             | 0.0                      | 20.0            | 0.0                |
    And LoanBuyDownFeeAdjustmentTransactionCreatedBusinessEvent is created on "02 July 2026"
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                     | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                         | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Buy Down Fee                         | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Buy Down Fee Amortization            | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Buy Down Fee Amortization            | 49.46  | 0.0       | 49.46    | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Buy Down Fee Adjustment              | 20.0   | 0.0       | 20.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Buy Down Fee Amortization Adjustment | 20.0   | 0.0       | 20.0     | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date         | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 01 July 2026 | 50.0       | 30.0             | 0.0                      | 20.0            | 0.0                |
    And LoanBuyDownFeeAmortizationAdjustmentTransactionCreatedBusinessEvent is created on "02 July 2026"
#    --- Close loan ---
    When Loan Pay-off is made on "02 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C85407
  Scenario: Verify buy down fee partial adjustment after full amortization via investor sale - UC2: with two buydown fees results in correct amortization adjustment amount
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 July 2026      | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "1000" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "1000" EUR transaction amount
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "01 July 2026" with "50" EUR transaction amount
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "01 July 2026" with "30" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-01     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    And Buy down fee contains the following data:
      | Date         | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 01 July 2026 | 50.0       | 0.0              | 50.0                     | 0.0             | 0.0                |
      | 01 July 2026 | 30.0       | 0.0              | 30.0                     | 0.0             | 0.0                |
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2026-07-01     | 1                  | PENDING | 2026-07-01    | 2026-07-01  | SALE             |
      | 2026-07-01     | 1                  | ACTIVE  | 2026-07-02    | 9999-12-31  | SALE             |
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement              | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Buy Down Fee              | 30.0   | 0.0       | 30.0     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Buy Down Fee Amortization | 0.87   | 0.0       | 0.87     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Buy Down Fee Amortization | 79.13  | 0.0       | 79.13    | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date         | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 01 July 2026 | 50.0       | 50.0             | 0.0                      | 0.0             | 0.0                |
      | 01 July 2026 | 30.0       | 30.0             | 0.0                      | 0.0             | 0.0                |
    And LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent is created on "01 July 2026"
    And Admin adds buy down fee adjustment with "AUTOPAY" payment type to the loan on "02 July 2026" with "20" EUR transaction amount
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement              | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Buy Down Fee              | 30.0   | 0.0       | 30.0     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Buy Down Fee Amortization | 0.87   | 0.0       | 0.87     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Buy Down Fee Amortization | 79.13  | 0.0       | 79.13    | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Buy Down Fee Adjustment   | 20.0   | 0.0       | 20.0     | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date         | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 01 July 2026 | 50.0       | 30.0             | 0.0                      | 20.0            | 0.0                |
      | 01 July 2026 | 30.0       | 30.0             | 0.0                      | 0.0             | 0.0                |
    And LoanBuyDownFeeAdjustmentTransactionCreatedBusinessEvent is created on "02 July 2026"
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                     | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                         | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Buy Down Fee                         | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Buy Down Fee                         | 30.0   | 0.0       | 30.0     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Buy Down Fee Amortization            | 0.87   | 0.0       | 0.87     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Buy Down Fee Amortization            | 79.13  | 0.0       | 79.13    | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Buy Down Fee Adjustment              | 20.0   | 0.0       | 20.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Buy Down Fee Amortization Adjustment | 20.0   | 0.0       | 20.0     | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date         | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 01 July 2026 | 50.0       | 30.0             | 0.0                      | 20.0            | 0.0                |
      | 01 July 2026 | 30.0       | 30.0             | 0.0                      | 0.0             | 0.0                |
    And LoanBuyDownFeeAmortizationAdjustmentTransactionCreatedBusinessEvent is created on "02 July 2026"
    #    --- Close loan ---
    When Loan Pay-off is made on "02 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C85408
  Scenario: Verify buy down fee partial adjustment after full amortization via investor sale - UC3: second daily COB after buy down fee adjustment post investor sale does not duplicate amortization adjustment
    When Admin sets the business date to "01 August 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 August 2026    | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 August 2026" with "1000" amount and expected disbursement date on "01 August 2026"
    And Admin successfully disburse the loan on "01 August 2026" with "1000" EUR transaction amount
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "01 August 2026" with "50" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-08-01     | 1                  |
    When Admin sets the business date to "02 August 2026"
    And Admin runs inline COB job for Loan
    And Admin adds buy down fee adjustment with "AUTOPAY" payment type to the loan on "02 August 2026" with "20" EUR transaction amount
    When Admin sets the business date to "03 August 2026"
    And Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                     | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 August 2026   | Disbursement                         | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 August 2026   | Buy Down Fee                         | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 01 August 2026   | Buy Down Fee Amortization            | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 01 August 2026   | Buy Down Fee Amortization            | 49.46  | 0.0       | 49.46    | 0.0  | 0.0       | 0.0          | false    |
      | 02 August 2026   | Buy Down Fee Adjustment              | 20.0   | 0.0       | 20.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 August 2026   | Buy Down Fee Amortization Adjustment | 20.0   | 0.0       | 20.0     | 0.0  | 0.0       | 0.0          | false    |
    When Admin sets the business date to "04 August 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                     | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 August 2026   | Disbursement                         | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 August 2026   | Buy Down Fee                         | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 01 August 2026   | Buy Down Fee Amortization            | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 01 August 2026   | Buy Down Fee Amortization            | 49.46  | 0.0       | 49.46    | 0.0  | 0.0       | 0.0          | false    |
      | 02 August 2026   | Buy Down Fee Adjustment              | 20.0   | 0.0       | 20.0     | 0.0  | 0.0       | 0.0          | false    |
      | 02 August 2026   | Buy Down Fee Amortization Adjustment | 20.0   | 0.0       | 20.0     | 0.0  | 0.0       | 0.0          | false    |
    When Loan Pay-off is made on "04 August 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C85409
  Scenario: Verify capitalized income partial adjustment after full amortization via investor sale - UC1: results in correct amortization adjustment amount
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 July 2026      | 1100           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "1100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "1000" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "01 July 2026" with "50" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-01     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 0.0              | 50.0                | 0.0             | 0.0                |
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2026-07-01     | 1                  | PENDING | 2026-07-01    | 2026-07-01  | SALE             |
      | 2026-07-01     | 1                  | ACTIVE  | 2026-07-02    | 9999-12-31  | SALE             |
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                    | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1050.0       | false    |
      | 01 July 2026     | Capitalized Income Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Capitalized Income Amortization | 49.46  | 0.0       | 49.46    | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 50.0             | 0.0                 | 0.0             | 0.0                |
    And LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent is raised on "01 July 2026"
    And Admin adds capitalized income adjustment with "AUTOPAY" payment type to the loan on "02 July 2026" with "20" EUR transaction amount
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                    | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1050.0       | false    |
      | 01 July 2026     | Capitalized Income Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Capitalized Income Amortization | 49.46  | 0.0       | 49.46    | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Capitalized Income Adjustment   | 20.0   | 20.0      | 0.0      | 0.0  | 0.0       | 1030.0       | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 30.0             | 0.0                 | 20.0            | 0.0                |
    And LoanCapitalizedIncomeAdjustmentTransactionCreatedBusinessEvent is raised on "02 July 2026"
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                            | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                                | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Capitalized Income                          | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1050.0       | false    |
      | 01 July 2026     | Capitalized Income Amortization             | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Capitalized Income Amortization             | 49.46  | 0.0       | 49.46    | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Capitalized Income Adjustment               | 20.0   | 20.0      | 0.0      | 0.0  | 0.0       | 1030.0       | false    |
      | 02 July 2026     | Capitalized Income Amortization Adjustment  | 20.0   | 0.0       | 20.0     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 30.0             | 0.0                 | 20.0            | 0.0                |
    And LoanCapitalizedIncomeAmortizationAdjustmentTransactionCreatedBusinessEvent is raised on "02 July 2026"
#    --- Close loan ---
    When Loan Pay-off is made on "02 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C85410
  Scenario: Verify capitalized income partial adjustment after full amortization via investor sale - UC2: with two capitalized income transactions results in correct amortization adjustment amount
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 July 2026      | 1100           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "1100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "1000" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "01 July 2026" with "50" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "01 July 2026" with "30" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-01     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 0.0              | 50.0                | 0.0             | 0.0                |
      | 30.0   | 0.0              | 30.0                | 0.0             | 0.0                |
    When Admin sets the business date to "02 July 2026"
    And Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2026-07-01     | 1                  | PENDING | 2026-07-01    | 2026-07-01  | SALE             |
      | 2026-07-01     | 1                  | ACTIVE  | 2026-07-02    | 9999-12-31  | SALE             |
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                    | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1050.0       | false    |
      | 01 July 2026     | Capitalized Income              | 30.0   | 30.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 01 July 2026     | Capitalized Income Amortization | 0.87   | 0.0       | 0.87     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Capitalized Income Amortization | 79.13  | 0.0       | 79.13    | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 50.0             | 0.0                 | 0.0             | 0.0                |
      | 30.0   | 30.0             | 0.0                 | 0.0             | 0.0                |
    And LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent is raised on "01 July 2026"
    And Admin adds capitalized income adjustment with "AUTOPAY" payment type to the loan on "02 July 2026" with "20" EUR transaction amount
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                    | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1050.0       | false    |
      | 01 July 2026     | Capitalized Income              | 30.0   | 30.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 01 July 2026     | Capitalized Income Amortization | 0.87   | 0.0       | 0.87     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Capitalized Income Amortization | 79.13  | 0.0       | 79.13    | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Capitalized Income Adjustment   | 20.0   | 20.0      | 0.0      | 0.0  | 0.0       | 1060.0       | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 30.0             | 0.0                 | 20.0            | 0.0                |
      | 30.0   | 30.0             | 0.0                 | 0.0             | 0.0                |
    And LoanCapitalizedIncomeAdjustmentTransactionCreatedBusinessEvent is raised on "02 July 2026"
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                            | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                                | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 July 2026     | Capitalized Income                          | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1050.0       | false    |
      | 01 July 2026     | Capitalized Income                          | 30.0   | 30.0      | 0.0      | 0.0  | 0.0       | 1080.0       | false    |
      | 01 July 2026     | Capitalized Income Amortization             | 0.87   | 0.0       | 0.87     | 0.0  | 0.0       | 0.0          | false    |
      | 01 July 2026     | Capitalized Income Amortization             | 79.13  | 0.0       | 79.13    | 0.0  | 0.0       | 0.0          | false    |
      | 02 July 2026     | Capitalized Income Adjustment               | 20.0   | 20.0      | 0.0      | 0.0  | 0.0       | 1060.0       | false    |
      | 02 July 2026     | Capitalized Income Amortization Adjustment  | 20.0   | 0.0       | 20.0     | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 30.0             | 0.0                 | 20.0            | 0.0                |
      | 30.0   | 30.0             | 0.0                 | 0.0             | 0.0                |
    And LoanCapitalizedIncomeAmortizationAdjustmentTransactionCreatedBusinessEvent is raised on "02 July 2026"
#    --- Close loan ---
    When Loan Pay-off is made on "02 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C85411
  Scenario: Verify capitalized income partial adjustment after full amortization via investor sale - UC3: second daily COB after capitalized income adjustment post investor sale does not duplicate amortization adjustment
    When Admin sets the business date to "01 August 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 August 2026    | 1100           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 August 2026" with "1100" amount and expected disbursement date on "01 August 2026"
    And Admin successfully disburse the loan on "01 August 2026" with "1000" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "01 August 2026" with "50" EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-08-01     | 1                  |
    When Admin sets the business date to "02 August 2026"
    And Admin runs inline COB job for Loan
    And Admin adds capitalized income adjustment with "AUTOPAY" payment type to the loan on "02 August 2026" with "20" EUR transaction amount
    When Admin sets the business date to "03 August 2026"
    And Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                            | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 August 2026   | Disbursement                                | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 August 2026   | Capitalized Income                          | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1050.0       | false    |
      | 01 August 2026   | Capitalized Income Amortization             | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 01 August 2026   | Capitalized Income Amortization             | 49.46  | 0.0       | 49.46    | 0.0  | 0.0       | 0.0          | false    |
      | 02 August 2026   | Capitalized Income Adjustment               | 20.0   | 20.0      | 0.0      | 0.0  | 0.0       | 1030.0       | false    |
      | 02 August 2026   | Capitalized Income Amortization Adjustment  | 20.0   | 0.0       | 20.0     | 0.0  | 0.0       | 0.0          | false    |
    When Admin sets the business date to "04 August 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                            | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 August 2026   | Disbursement                                | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 August 2026   | Capitalized Income                          | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1050.0       | false    |
      | 01 August 2026   | Capitalized Income Amortization             | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    |
      | 01 August 2026   | Capitalized Income Amortization             | 49.46  | 0.0       | 49.46    | 0.0  | 0.0       | 0.0          | false    |
      | 02 August 2026   | Capitalized Income Adjustment               | 20.0   | 20.0      | 0.0      | 0.0  | 0.0       | 1030.0       | false    |
      | 02 August 2026   | Capitalized Income Amortization Adjustment  | 20.0   | 0.0       | 20.0     | 0.0  | 0.0       | 0.0          | false    |
    When Loan Pay-off is made on "04 August 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C89806
  Scenario: Verify accelerated buy down fee amortization on intermediate-to-final sale is attributed to intermediate owner
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DELAYED_SETTLEMENT" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 July 2026     | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "100" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2026     |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2026   |           | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 2  | 31   | 01 September 2026 |           | 33.53           | 33.33         | 0.39     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 3  | 30   | 01 October 2026  |           | 0.0             | 33.53         | 0.2      | 0.0  | 0.0       | 33.73 | 0.0  | 0.0        | 0.0  | 33.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.17     | 0.0  | 0.0       | 101.17 | 0.0  | 0.0        | 0.0  | 101.17      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
    And Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | intermediarySale | 2026-07-02     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2026-07-02     | 1                  | PENDING_INTERMEDIATE | 2026-07-01    | 2026-07-02  | INTERMEDIARYSALE |
      | 2026-07-02     | 1                  | ACTIVE_INTERMEDIATE  | 2026-07-03    | 9999-12-31  | INTERMEDIARYSALE |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2026      |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2026    |           | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 2  | 31   | 01 September 2026 |           | 33.53           | 33.33         | 0.39     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 3  | 30   | 01 October 2026   |           | 0.0             | 33.53         | 0.2      | 0.0  | 0.0       | 33.73 | 0.0  | 0.0        | 0.0  | 33.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.17     | 0.0  | 0.0       | 101.17 | 0.0  | 0.0        | 0.0  | 101.17      |
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 July 2026" with "50" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2026      |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2026    |           | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 2  | 31   | 01 September 2026 |           | 33.53           | 33.33         | 0.39     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 3  | 30   | 01 October 2026   |           | 0.0             | 33.53         | 0.2      | 0.0  | 0.0       | 33.73 | 0.0  | 0.0        | 0.0  | 33.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.17     | 0.0  | 0.0       | 101.17 | 0.0  | 0.0        | 0.0  | 101.17      |
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 July 2026     | Buy Down Fee     | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "BUY_DOWN_FEE" transaction with date "03 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | EXPENSE   | 450280       | Buy Down Expense            | 50.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   |
    And Buy down fee contains the following data:
      | Date         | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 July 2026 | 50.0       | 0.0              | 50.0                     | 0.0             | 0.0                |
    When Admin sets the business date to "04 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2026      |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2026    |           | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 2  | 31   | 01 September 2026 |           | 33.53           | 33.33         | 0.39     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 3  | 30   | 01 October 2026   |           | 0.0             | 33.53         | 0.2      | 0.0  | 0.0       | 33.73 | 0.0  | 0.0        | 0.0  | 33.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.17     | 0.0  | 0.0       | 101.17 | 0.0  | 0.0        | 0.0  | 101.17      |
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 July 2026     | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 July 2026     | Buy Down Fee Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "BUY_DOWN_FEE_AMORTIZATION" transaction with date "03 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 450281       | Income From Buy Down        |       | 0.56   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 0.56  |        |
    And Buy down fee contains the following data:
      | Date         | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 July 2026 | 50.0       | 0.56             | 49.44                    | 0.0             | 0.0                |
    And The previous asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName               | entryType | amount |
      | EXPENSE       | 450280        | Buy Down Expense            | DEBIT     | 50.00  |
      | LIABILITY     | 145024        | Deferred Capitalized Income | CREDIT    | 50.00  |
      | INCOME        | 450281        | Income From Buy Down        | CREDIT    | 0.56   |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 0.56   |
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-04     | 1                  |
    When Admin sets the business date to "05 July 2026"
    And Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 4 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2026-07-02     | 1                  | PENDING_INTERMEDIATE | 2026-07-01    | 2026-07-02  | INTERMEDIARYSALE |
      | 2026-07-02     | 1                  | ACTIVE_INTERMEDIATE  | 2026-07-03    | 2026-07-04  | INTERMEDIARYSALE |
      | 2026-07-04     | 1                  | PENDING              | 2026-07-04    | 2026-07-04  | SALE             |
      | 2026-07-04     | 1                  | ACTIVE               | 2026-07-05    | 9999-12-31  | SALE             |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2026      |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2026    |           | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 2  | 31   | 01 September 2026 |           | 33.53           | 33.33         | 0.39     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 3  | 30   | 01 October 2026   |           | 0.0             | 33.53         | 0.2      | 0.0  | 0.0       | 33.73 | 0.0  | 0.0        | 0.0  | 33.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.17     | 0.0  | 0.0       | 101.17 | 0.0  | 0.0        | 0.0  | 101.17      |
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 July 2026     | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 July 2026     | Buy Down Fee Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    |
      | 04 July 2026     | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 04 July 2026     | Buy Down Fee Amortization | 48.89  | 0.0       | 48.89    | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date         | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 July 2026 | 50.0       | 50.0             | 0.0                      | 0.0             | 0.0                |
    And Loan Transactions tab has 2 a "BUY_DOWN_FEE_AMORTIZATION" transactions with date "04 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 450281       | Income From Buy Down        |       | 0.55   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 0.55  |        |
      | INCOME    | 450281       | Income From Buy Down        |       | 48.89  |
      | LIABILITY | 145024       | Deferred Capitalized Income | 48.89 |        |
    And The previous asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName               | entryType | amount |
      | EXPENSE       | 450280        | Buy Down Expense            | DEBIT     | 50.00  |
      | LIABILITY     | 145024        | Deferred Capitalized Income | CREDIT    | 50.00  |
      | INCOME        | 450281        | Income From Buy Down        | CREDIT    | 0.56   |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 0.56   |
      | INCOME        | 450281        | Income From Buy Down        | CREDIT    | 0.55   |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 0.55   |
      | INCOME        | 450281        | Income From Buy Down        | CREDIT    | 48.89  |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 48.89  |
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DEFAULT_SETTLEMENT" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    When Loan Pay-off is made on "05 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C89807
  Scenario: Verify accelerated capitalized income amortization on intermediate-to-final sale is attributed to intermediate owner
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DELAYED_SETTLEMENT" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 July 2026     | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "200" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "100" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2026      |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2026    |           | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 2  | 31   | 01 September 2026 |           | 33.53           | 33.33         | 0.39     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 3  | 30   | 01 October 2026   |           | 0.0             | 33.53         | 0.2      | 0.0  | 0.0       | 33.73 | 0.0  | 0.0        | 0.0  | 33.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.17     | 0.0  | 0.0       | 101.17 | 0.0  | 0.0        | 0.0  | 101.17      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
    And Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | intermediarySale | 2026-07-02     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2026-07-02     | 1                  | PENDING_INTERMEDIATE | 2026-07-01    | 2026-07-02  | INTERMEDIARYSALE |
      | 2026-07-02     | 1                  | ACTIVE_INTERMEDIATE  | 2026-07-03    | 9999-12-31  | INTERMEDIARYSALE |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2026      |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2026    |           | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 2  | 31   | 01 September 2026 |           | 33.53           | 33.33         | 0.39     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 3  | 30   | 01 October 2026   |           | 0.0             | 33.53         | 0.2      | 0.0  | 0.0       | 33.73 | 0.0  | 0.0        | 0.0  | 33.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.17     | 0.0  | 0.0       | 101.17 | 0.0  | 0.0        | 0.0  | 101.17      |
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "03 July 2026" with "50" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2026      |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      |    |      | 03 July 2026      |           | 50.0            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2026    |           | 100.28          | 49.72         | 0.86     | 0.0  | 0.0       | 50.58 | 0.0  | 0.0        | 0.0  | 50.58       |
      | 2  | 31   | 01 September 2026 |           | 50.28           | 50.0          | 0.58     | 0.0  | 0.0       | 50.58 | 0.0  | 0.0        | 0.0  | 50.58       |
      | 3  | 30   | 01 October 2026   |           | 0.0             | 50.28         | 0.29     | 0.0  | 0.0       | 50.57 | 0.0  | 0.0        | 0.0  | 50.57       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 150.0         | 1.73     | 0.0  | 0.0       | 151.73 | 0.0  | 0.0        | 0.0  | 151.73      |
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 July 2026     | Capitalized Income | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "03 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable            | 50.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 0.0              | 50.0                | 0.0             | 0.0                |
    When Admin sets the business date to "04 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2026      |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      |    |      | 03 July 2026      |           | 50.0            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2026    |           | 100.28          | 49.72         | 0.86     | 0.0  | 0.0       | 50.58 | 0.0  | 0.0        | 0.0  | 50.58       |
      | 2  | 31   | 01 September 2026 |           | 50.28           | 50.0          | 0.58     | 0.0  | 0.0       | 50.58 | 0.0  | 0.0        | 0.0  | 50.58       |
      | 3  | 30   | 01 October 2026   |           | 0.0             | 50.28         | 0.29     | 0.0  | 0.0       | 50.57 | 0.0  | 0.0        | 0.0  | 50.57       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 150.0         | 1.73     | 0.0  | 0.0       | 151.73 | 0.0  | 0.0        | 0.0  | 151.73      |
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 July 2026     | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 03 July 2026     | Capitalized Income Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "03 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 404000       | Interest Income             |       | 0.56   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 0.56  |        |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 0.56             | 49.44               | 0.0             | 0.0                |
    And The previous asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName               | entryType | amount |
      | ASSET         | 112601        | Loans Receivable            | DEBIT     | 50.00  |
      | LIABILITY     | 145024        | Deferred Capitalized Income | CREDIT    | 50.00  |
      | INCOME        | 404000        | Interest Income             | CREDIT    | 0.56   |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 0.56   |
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-04     | 1                  |
    When Admin sets the business date to "05 July 2026"
    And Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 4 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status               | effectiveFrom | effectiveTo | Transaction type |
      | 2026-07-02     | 1                  | PENDING_INTERMEDIATE | 2026-07-01    | 2026-07-02  | INTERMEDIARYSALE |
      | 2026-07-02     | 1                  | ACTIVE_INTERMEDIATE  | 2026-07-03    | 2026-07-04  | INTERMEDIARYSALE |
      | 2026-07-04     | 1                  | PENDING              | 2026-07-04    | 2026-07-04  | SALE             |
      | 2026-07-04     | 1                  | ACTIVE               | 2026-07-05    | 9999-12-31  | SALE             |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2026      |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      |    |      | 03 July 2026      |           | 50.0            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2026    |           | 100.28          | 49.72         | 0.86     | 0.0  | 0.0       | 50.58 | 0.0  | 0.0        | 0.0  | 50.58       |
      | 2  | 31   | 01 September 2026 |           | 50.28           | 50.0          | 0.58     | 0.0  | 0.0       | 50.58 | 0.0  | 0.0        | 0.0  | 50.58       |
      | 3  | 30   | 01 October 2026   |           | 0.0             | 50.28         | 0.29     | 0.0  | 0.0       | 50.57 | 0.0  | 0.0        | 0.0  | 50.57       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 150.0         | 1.73     | 0.0  | 0.0       | 151.73 | 0.0  | 0.0        | 0.0  | 151.73      |
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 July 2026     | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 03 July 2026     | Capitalized Income Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    |
      | 04 July 2026     | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 04 July 2026     | Capitalized Income Amortization | 48.89  | 0.0       | 48.89    | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 50.0             | 0.0                 | 0.0             | 0.0                |
    And Loan Transactions tab has 2 a "CAPITALIZED_INCOME_AMORTIZATION" transactions with date "04 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 404000       | Interest Income             |       | 0.55   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 0.55  |        |
      | INCOME    | 404000       | Interest Income             |       | 48.89  |
      | LIABILITY | 145024       | Deferred Capitalized Income | 48.89 |        |
    And The previous asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName               | entryType | amount |
      | ASSET         | 112601        | Loans Receivable            | DEBIT     | 50.00  |
      | LIABILITY     | 145024        | Deferred Capitalized Income | CREDIT    | 50.00  |
      | INCOME        | 404000        | Interest Income             | CREDIT    | 0.56   |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 0.56   |
      | INCOME        | 404000        | Interest Income             | CREDIT    | 0.55   |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 0.55   |
      | INCOME        | 404000        | Interest Income             | CREDIT    | 48.89  |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 48.89  |
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DEFAULT_SETTLEMENT" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    When Loan Pay-off is made on "05 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C89808
  Scenario: Verify accelerated buy down fee amortization on owner-to-owner sale is attributed to previous owner
    # DEFAULT_SETTLEMENT: Owner A buys loan → buy-down + daily amort under A → sale to Owner B
    # Accelerated amort on sale must stay on Owner A (current at recognition), then ownership moves to B
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DEFAULT_SETTLEMENT" for loan product "LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES"
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 July 2026     | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "100" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "100" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2026      |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2026    |           | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 2  | 31   | 01 September 2026 |           | 33.53           | 33.33         | 0.39     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 3  | 30   | 01 October 2026   |           | 0.0             | 33.53         | 0.2      | 0.0  | 0.0       | 33.73 | 0.0  | 0.0        | 0.0  | 33.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.17     | 0.0  | 0.0       | 101.17 | 0.0  | 0.0        | 0.0  | 101.17      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
    And Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    # --- Sale to Owner A ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-02     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2026-07-02     | 1                  | PENDING | 2026-07-01    | 2026-07-02  | SALE             |
      | 2026-07-02     | 1                  | ACTIVE  | 2026-07-03    | 9999-12-31  | SALE             |
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
    # --- Buy-down under Owner A + daily amort ---
    And Admin adds buy down fee with "AUTOPAY" payment type to the loan on "03 July 2026" with "50" EUR transaction amount
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 July 2026     | Buy Down Fee     | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "BUY_DOWN_FEE" transaction with date "03 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | EXPENSE   | 450280       | Buy Down Expense            | 50.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   |
    And Buy down fee contains the following data:
      | Date         | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 July 2026 | 50.0       | 0.0              | 50.0                     | 0.0             | 0.0                |
    When Admin sets the business date to "04 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 July 2026     | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 July 2026     | Buy Down Fee Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "BUY_DOWN_FEE_AMORTIZATION" transaction with date "03 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 450281       | Income From Buy Down        |       | 0.56   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 0.56  |        |
    And Buy down fee contains the following data:
      | Date         | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 July 2026 | 50.0       | 0.56             | 49.44                    | 0.0             | 0.0                |
    # Owner A (current) owns daily amort
    And The asset external owner has the following OWNER Journal entries containing:
      | glAccountType | glAccountCode | glAccountName               | entryType | amount |
      | EXPENSE       | 450280        | Buy Down Expense            | DEBIT     | 50.00  |
      | LIABILITY     | 145024        | Deferred Capitalized Income | CREDIT    | 50.00  |
      | INCOME        | 450281        | Income From Buy Down        | CREDIT    | 0.56   |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 0.56   |
    # --- Sale Owner A → Owner B: accelerated amort must stay on Owner A ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-04     | 1                  |
    When Admin sets the business date to "05 July 2026"
    And Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement              | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 July 2026     | Buy Down Fee              | 50.0   | 0.0       | 50.0     | 0.0  | 0.0       | 0.0          | false    |
      | 03 July 2026     | Buy Down Fee Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    |
      | 04 July 2026     | Buy Down Fee Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 04 July 2026     | Buy Down Fee Amortization | 48.89  | 0.0       | 48.89    | 0.0  | 0.0       | 0.0          | false    |
    And Buy down fee contains the following data:
      | Date         | Fee Amount | Amortized Amount | Not Yet Amortized Amount | Adjusted Amount | Charged Off Amount |
      | 03 July 2026 | 50.0       | 50.0             | 0.0                      | 0.0             | 0.0                |
    And Loan Transactions tab has 2 a "BUY_DOWN_FEE_AMORTIZATION" transactions with date "04 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 450281       | Income From Buy Down        |       | 0.55   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 0.55  |        |
      | INCOME    | 450281       | Income From Buy Down        |       | 48.89  |
      | LIABILITY | 145024       | Deferred Capitalized Income | 48.89 |        |
    # Owner A is now previous; accelerated 48.89 must be on A, not B / self
    And The previous asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName               | entryType | amount |
      | EXPENSE       | 450280        | Buy Down Expense            | DEBIT     | 50.00  |
      | LIABILITY     | 145024        | Deferred Capitalized Income | CREDIT    | 50.00  |
      | INCOME        | 450281        | Income From Buy Down        | CREDIT    | 0.56   |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 0.56   |
      | INCOME        | 450281        | Income From Buy Down        | CREDIT    | 0.55   |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 0.55   |
      | INCOME        | 450281        | Income From Buy Down        | CREDIT    | 48.89  |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 48.89  |
    When Loan Pay-off is made on "05 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C89809
  Scenario: Verify accelerated capitalized income amortization on owner-to-owner sale is attributed to previous owner
    # DEFAULT_SETTLEMENT: Owner A buys loan → capitalized income + daily amort under A → sale to Owner B
    # Accelerated amort on sale must stay on Owner A (current at recognition), then ownership moves to B
    When Admin set external asset owner loan product attribute "SETTLEMENT_MODEL" value "DEFAULT_SETTLEMENT" for loan product "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME"
    When Admin sets the business date to "01 July 2026"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 July 2026     | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 July 2026" with "200" amount and expected disbursement date on "01 July 2026"
    And Admin successfully disburse the loan on "01 July 2026" with "100" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2026      |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2026    |           | 66.86           | 33.14         | 0.58     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 2  | 31   | 01 September 2026 |           | 33.53           | 33.33         | 0.39     | 0.0  | 0.0       | 33.72 | 0.0  | 0.0        | 0.0  | 33.72       |
      | 3  | 30   | 01 October 2026   |           | 0.0             | 33.53         | 0.2      | 0.0  | 0.0       | 33.73 | 0.0  | 0.0        | 0.0  | 33.73       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.17     | 0.0  | 0.0       | 101.17 | 0.0  | 0.0        | 0.0  | 101.17      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
    And Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    # --- Sale to Owner A ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-02     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    When Admin sets the business date to "03 July 2026"
    And Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2026-07-02     | 1                  | PENDING | 2026-07-01    | 2026-07-02  | SALE             |
      | 2026-07-02     | 1                  | ACTIVE  | 2026-07-03    | 9999-12-31  | SALE             |
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
    # --- Capitalized income under Owner A + daily amort ---
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "03 July 2026" with "50" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 July 2026      |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      |    |      | 03 July 2026      |           | 50.0            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 August 2026    |           | 100.28          | 49.72         | 0.86     | 0.0  | 0.0       | 50.58 | 0.0  | 0.0        | 0.0  | 50.58       |
      | 2  | 31   | 01 September 2026 |           | 50.28           | 50.0          | 0.58     | 0.0  | 0.0       | 50.58 | 0.0  | 0.0        | 0.0  | 50.58       |
      | 3  | 30   | 01 October 2026   |           | 0.0             | 50.28         | 0.29     | 0.0  | 0.0       | 50.57 | 0.0  | 0.0        | 0.0  | 50.57       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 150.0         | 1.73     | 0.0  | 0.0       | 151.73 | 0.0  | 0.0        | 0.0  | 151.73      |
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 July 2026     | Capitalized Income | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "03 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable            | 50.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 0.0              | 50.0                | 0.0             | 0.0                |
    When Admin sets the business date to "04 July 2026"
    And Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 July 2026     | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 03 July 2026     | Capitalized Income Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "03 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 404000       | Interest Income             |       | 0.56   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 0.56  |        |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 0.56             | 49.44               | 0.0             | 0.0                |
    # Owner A (current) owns daily amort
    And The asset external owner has the following OWNER Journal entries containing:
      | glAccountType | glAccountCode | glAccountName               | entryType | amount |
      | ASSET         | 112601        | Loans Receivable            | DEBIT     | 50.00  |
      | LIABILITY     | 145024        | Deferred Capitalized Income | CREDIT    | 50.00  |
      | INCOME        | 404000        | Interest Income             | CREDIT    | 0.56   |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 0.56   |
    # --- Sale Owner A → Owner B: accelerated amort must stay on Owner A ---
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2026-07-04     | 1                  |
    When Admin sets the business date to "05 July 2026"
    And Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    And Loan Transactions tab has the following data without accruals:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 July 2026     | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 03 July 2026     | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    |
      | 03 July 2026     | Capitalized Income Amortization | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    |
      | 04 July 2026     | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    |
      | 04 July 2026     | Capitalized Income Amortization | 48.89  | 0.0       | 48.89    | 0.0  | 0.0       | 0.0          | false    |
    And Deferred Capitalized Income contains the following data:
      | Amount | Amortized Amount | Unrecognized Amount | Adjusted Amount | Charged Off Amount |
      | 50.0   | 50.0             | 0.0                 | 0.0             | 0.0                |
    And Loan Transactions tab has 2 a "CAPITALIZED_INCOME_AMORTIZATION" transactions with date "04 July 2026" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 404000       | Interest Income             |       | 0.55   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 0.55  |        |
      | INCOME    | 404000       | Interest Income             |       | 48.89  |
      | LIABILITY | 145024       | Deferred Capitalized Income | 48.89 |        |
    # Owner A is now previous; accelerated 48.89 must be on A, not B
    And The previous asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName               | entryType | amount |
      | ASSET         | 112601        | Loans Receivable            | DEBIT     | 50.00  |
      | LIABILITY     | 145024        | Deferred Capitalized Income | CREDIT    | 50.00  |
      | INCOME        | 404000        | Interest Income             | CREDIT    | 0.56   |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 0.56   |
      | INCOME        | 404000        | Interest Income             | CREDIT    | 0.55   |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 0.55   |
      | INCOME        | 404000        | Interest Income             | CREDIT    | 48.89  |
      | LIABILITY     | 145024        | Deferred Capitalized Income | DEBIT     | 48.89  |
    When Loan Pay-off is made on "05 July 2026" with transfer external owner
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
