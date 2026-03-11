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
