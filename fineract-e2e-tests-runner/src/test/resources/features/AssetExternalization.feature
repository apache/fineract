@AssetExternalizationFeature
Feature: Asset Externalization


  Scenario: Verify that all fields and values are correct in case of a SALES request by loan id and user-generated transferExternalId
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, user-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    Then Fetching Asset externalization details by loan external id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    Then Fetching Asset externalization details by transfer external id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    Then Asset externalization details has the generated transferExternalId
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that all fields and values are correct in case of a SALES request by loan id system-generated transferExternalId
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    Then Fetching Asset externalization details by loan external id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    Then Fetching Asset externalization details by transfer external id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    Then Asset externalization details has the generated transferExternalId
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that all fields and values are correct in case of a SALES request by loan external id user-generated transferExternalId
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan external ID with unique ownerExternalId, user-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    Then Fetching Asset externalization details by loan external id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    Then Fetching Asset externalization details by transfer external id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    Then Asset externalization details has the generated transferExternalId
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that all fields and values are correct in case of a SALES request by loan external id system-generated transferExternalId
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan external ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    Then Fetching Asset externalization details by loan external id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    Then Fetching Asset externalization details by transfer external id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    Then Asset externalization details has the generated transferExternalId
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that Asset externalization details are correct after CoB in case of a SALES request by loan id
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that Asset externalization details has the correct data in case of a BUYBACK request placed before the settlementDate with a same settlementDate as the sales one
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "10 May 2023"
    When Admin runs inline COB job for Loan
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-21     |                    |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
      | 2023-05-21     | 1                  | BUYBACK | 2023-05-10    | 9999-12-31  | BUYBACK          |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 4 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status    | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING   | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | BUYBACK   | 2023-05-10    | 2023-05-21  | BUYBACK          |
      | 2023-05-21     | 1                  | CANCELLED | 2023-05-21    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | CANCELLED | 2023-05-21    | 2023-05-21  | BUYBACK          |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that Asset externalization details has the correct data in case of a BUYBACK request placed on a business date before the settlementDate of sales request and with a settlementDate for buyback after the sales got active
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "10 May 2023"
    When Admin runs inline COB job for Loan
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-30     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-10    | 9999-12-31  | BUYBACK          |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-10    | 9999-12-31  | BUYBACK          |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin sets the business date to "31 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-10    | 2023-05-30  | BUYBACK          |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 2023-05-30  | SALE             |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that Asset externalization details has the correct data in case of a BUYBACK request placed after the settlementDate
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin sets the business date to "25 May 2023"
    When Admin runs inline COB job for Loan
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-30     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-25    | 9999-12-31  | BUYBACK          |
    When Admin sets the business date to "31 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 2023-05-30  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-25    | 2023-05-30  | BUYBACK          |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that BUYBACK request on a loan with PENDING ownership where BUYBACK settlement date is earlier than SALE settlement date results an error
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "10 May 2023"
    When Admin runs inline COB job for Loan
    Then BUYBACK transaction results a 403 error and proper error message when its settlementDate is earlier than the original settlementDate
      | Transaction type | settlementDate |
      | buyback          | 2023-05-15     |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that SALES request on a fully paid loan results an error
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 May 2023"
    And Customer makes "AUTOPAY" repayment on "10 May 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Asset externalization transaction with the following data results a 403 error and "LOAN_NOT_ACTIVE" error message
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     | 1                  |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that SALES request on an overpaid loan results an error
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 May 2023"
    And Customer makes "AUTOPAY" repayment on "10 May 2023" with 1200 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Asset externalization transaction with the following data results a 403 error and "LOAN_NOT_ACTIVE" error message
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     | 1                  |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that SALES request on a loan with ACTIVE ownership results an error
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin sets the business date to "25 May 2023"
    Then Asset externalization transaction with the following data results a 403 error and "ASSET_OWNED_CANNOT_BE_SOLD" error message
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-30     | 1                  |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that BUYBACK request on a fully paid loan can be done successfully
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin sets the business date to "25 May 2023"
    And Customer makes "AUTOPAY" repayment on "25 May 2023" with 1000 EUR transaction amount and check external owner
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-30     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-25    | 9999-12-31  | BUYBACK          |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that BUYBACK request on an overpaid loan can be done successfully
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin sets the business date to "25 May 2023"
    And Customer makes "AUTOPAY" repayment on "25 May 2023" with 1200 EUR transaction amount and check external owner
    Then Loan status will be "OVERPAID"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-30     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-25    | 9999-12-31  | BUYBACK          |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that BUYBACK request on a loan with INACTIVE ownership results an error
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Asset externalization transaction with the following data results a 403 error and "ASSET_NOT_OWNED_CANNOT_BE_BOUGHT" error message
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-21     |                    |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that SALES request can NOT be placed on a loan which is not APPROVED yet
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    Then Loan status will be "SUBMITTED_AND_PENDING_APPROVAL"
    Then Asset externalization transaction with the following data results a 403 error and "LOAN_NOT_ACTIVE" error message
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-30     | 1                  |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that SALES request can NOT be placed on a loan which is not DISBURSED yet
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    Then Loan status will be "APPROVED"
    Then Asset externalization transaction with the following data results a 403 error and "LOAN_NOT_ACTIVE" error message
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-30     | 1                  |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that SALES request on a loan with PENDING ownership results an error
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "10 May 2023"
    When Admin runs inline COB job for Loan
    Then Asset externalization transaction with the following data results a 403 error and "ALREADY_PENDING" error message
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-30     | 1                  |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that SALES with settlement date earlier than actual business date results an error
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Asset externalization transaction with the following data results a 403 error and "SETTLEMENT_DATE_IN_THE_PAST" error message
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-04-21     | 1                  |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that SALES with null owner external id results an error
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Asset externalization SALES transaction with ownerExternalId = null and the following data results a 400 error and "INVALID_REQUEST" error message
      | settlementDate | purchasePriceRatio |
      | 2023-05-21     | 1                  |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that SALES with null purchase price ratio results an error
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Asset externalization transaction with the following data results a 400 error and "INVALID_REQUEST" error message
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     |                    |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that SALES with null settlement date results an error
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Asset externalization transaction with the following data results a 400 error and "INVALID_REQUEST" error message
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             |                | 1                  |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that BUYBACK request on a loan with PENDING BUYBACK ownership result an error
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin sets the business date to "25 May 2023"
    When Admin runs inline COB job for Loan
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-30     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-25    | 9999-12-31  | BUYBACK          |
    When Admin sets the business date to "28 May 2023"
    When Admin runs inline COB job for Loan
    Then Asset externalization transaction with the following data results a 403 error and "BUYBACK_ALREADY_IN_PROGRESS_CANNOT_BE_BOUGHT" error message
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-30     |                    |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that BUYBACK with settlement date earlier than actual business date results an error
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin sets the business date to "25 May 2023"
    When Admin runs inline COB job for Loan
    Then Asset externalization transaction with the following data results a 403 error and "SETTLEMENT_DATE_IN_THE_PAST" error message
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-21     |                    |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that BUYBACK with ownerExternalId=NULL can be placed, and results a 200OK response
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin sets the business date to "25 May 2023"
    When Admin runs inline COB job for Loan
    When Admin makes asset externalization BUYBACK request with ownerExternalId = null and settlement date "2023-05-30" by Loan ID with system-generated transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-25    | 9999-12-31  | BUYBACK          |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that BUYBACK with purchasePriceRatio=NULL can be placed, and results a 200OK response
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin sets the business date to "25 May 2023"
    When Admin runs inline COB job for Loan
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-30     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-25    | 9999-12-31  | BUYBACK          |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that SALES with null settlement date results an error
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin sets the business date to "25 May 2023"
    When Admin runs inline COB job for Loan
    Then Asset externalization transaction with the following data results a 400 error and "INVALID_REQUEST" error message
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          |                |                    |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow

   @AssetExternalizationJournalEntry
  Scenario: Verify that Asset externalization SALES and BUYBACK has the correct Journal entries: no other transactions
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    Then The latest asset externalization transaction with "ACTIVE" status has the following TRANSFER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | CREDIT    | 1000.00 |
      | ASSET         | 146000        | Asset transfer   | DEBIT     | 1000.00 |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
      | ASSET         | 146000        | Asset transfer   | CREDIT    | 1000.00 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-30     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-22    | 9999-12-31  | BUYBACK          |
    When Admin sets the business date to "31 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 2023-05-30  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-22    | 2023-05-30  | BUYBACK          |
    Then The latest asset externalization transaction with "BUYBACK" status has the following TRANSFER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
      | ASSET         | 146000        | Asset transfer   | CREDIT    | 1000.00 |
      | ASSET         | 112601        | Loans Receivable | CREDIT    | 1000.00 |
      | ASSET         | 146000        | Asset transfer   | DEBIT     | 1000.00 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
      | ASSET         | 112601        | Loans Receivable | CREDIT    | 1000.00 |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow

   @AssetExternalizationJournalEntry
  Scenario: Verify that Asset externalization SALES and BUYBACK has the correct Journal entries: fee applied before sale, and penalty applied before buyback
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 May 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "10 May 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    Then The latest asset externalization transaction with "ACTIVE" status has the following TRANSFER Journal entries:
      | glAccountType | glAccountCode | glAccountName           | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable        | CREDIT    | 1000.00 |
      | ASSET         | 112603        | Interest/Fee Receivable | CREDIT    | 10.00   |
      | ASSET         | 146000        | Asset transfer          | DEBIT     | 1010.00 |
      | ASSET         | 112601        | Loans Receivable        | DEBIT     | 1000.00 |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 10.00   |
      | ASSET         | 146000        | Asset transfer          | CREDIT    | 1010.00 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName           | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable        | DEBIT     | 1000.00 |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 10.00   |
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-30     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-22    | 9999-12-31  | BUYBACK          |
    When Admin adds "LOAN_NSF_FEE" due date charge with "25 May 2023" due date and 20 EUR transaction amount
    When Admin sets the business date to "26 May 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "31 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 2023-05-30  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-22    | 2023-05-30  | BUYBACK          |
    Then The latest asset externalization transaction with "BUYBACK" status has the following TRANSFER Journal entries:
      | glAccountType | glAccountCode | glAccountName           | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable        | DEBIT     | 1000.00 |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 30.00   |
      | ASSET         | 146000        | Asset transfer          | CREDIT    | 1030.00 |
      | ASSET         | 112601        | Loans Receivable        | CREDIT    | 1000.00 |
      | ASSET         | 112603        | Interest/Fee Receivable | CREDIT    | 30.00   |
      | ASSET         | 146000        | Asset transfer          | DEBIT     | 1030.00 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName           | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable        | DEBIT     | 1000.00 |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 10.00   |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 20.00   |
      | INCOME        | 404007        | Fee Income              | CREDIT    | 20.00   |
      | ASSET         | 112601        | Loans Receivable        | CREDIT    | 1000.00 |
      | ASSET         | 112603        | Interest/Fee Receivable | CREDIT    | 30.00   |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow

   @AssetExternalizationJournalEntry
  Scenario: Verify that Asset externalization SALES and BUYBACK has the correct Journal entries: Repyment while status is ACTIVE
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    Then The latest asset externalization transaction with "ACTIVE" status has the following TRANSFER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | CREDIT    | 1000.00 |
      | ASSET         | 146000        | Asset transfer   | DEBIT     | 1000.00 |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
      | ASSET         | 146000        | Asset transfer   | CREDIT    | 1000.00 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "22 May 2023" with 200 EUR transaction amount and system-generated Idempotency key and check external owner
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-30     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-22    | 9999-12-31  | BUYBACK          |
    When Admin sets the business date to "31 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 2023-05-30  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-22    | 2023-05-30  | BUYBACK          |
    Then The latest asset externalization transaction with "BUYBACK" status has the following TRANSFER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 800.00 |
      | ASSET         | 146000        | Asset transfer   | CREDIT    | 800.00 |
      | ASSET         | 112601        | Loans Receivable | CREDIT    | 800.00 |
      | ASSET         | 146000        | Asset transfer   | DEBIT     | 800.00 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName             | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable          | DEBIT     | 1000.00 |
      | ASSET         | 112601        | Loans Receivable          | CREDIT    | 200.00  |
      | LIABILITY     | 145023        | Suspense/Clearing account | DEBIT     | 200.00  |
      | ASSET         | 112601        | Loans Receivable          | CREDIT    | 800.00  |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow

   @AssetExternalizationJournalEntry
  Scenario: Verify that Asset externalization SALES and BUYBACK has the correct Journal entries: GOODWILL_CREDIT transaction while status is ACTIVE
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    Then The latest asset externalization transaction with "ACTIVE" status has the following TRANSFER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | CREDIT    | 1000.00 |
      | ASSET         | 146000        | Asset transfer   | DEBIT     | 1000.00 |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
      | ASSET         | 146000        | Asset transfer   | CREDIT    | 1000.00 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "22 May 2023" with 200 EUR transaction amount and system-generated Idempotency key and check external owner
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-30     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-22    | 9999-12-31  | BUYBACK          |
    When Admin sets the business date to "31 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 2023-05-30  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-22    | 2023-05-30  | BUYBACK          |
    Then The latest asset externalization transaction with "BUYBACK" status has the following TRANSFER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 800.00 |
      | ASSET         | 146000        | Asset transfer   | CREDIT    | 800.00 |
      | ASSET         | 112601        | Loans Receivable | CREDIT    | 800.00 |
      | ASSET         | 146000        | Asset transfer   | DEBIT     | 800.00 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName            | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable         | DEBIT     | 1000.00 |
      | ASSET         | 112601        | Loans Receivable         | CREDIT    | 200.00  |
      | EXPENSE       | 744003        | Goodwill Expense Account | DEBIT     | 200.00  |
      | ASSET         | 112601        | Loans Receivable         | CREDIT    | 800.00  |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow

   @AssetExternalizationJournalEntry
  Scenario: Verify that Asset externalization SALES and BUYBACK has the correct Journal entries: MERCHANT_ISSUED_REFUND transaction while status is ACTIVE
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    Then The latest asset externalization transaction with "ACTIVE" status has the following TRANSFER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | CREDIT    | 1000.00 |
      | ASSET         | 146000        | Asset transfer   | DEBIT     | 1000.00 |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
      | ASSET         | 146000        | Asset transfer   | CREDIT    | 1000.00 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "22 May 2023" with 200 EUR transaction amount and system-generated Idempotency key and check external owner
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-30     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-22    | 9999-12-31  | BUYBACK          |
    When Admin sets the business date to "31 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 2023-05-30  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-22    | 2023-05-30  | BUYBACK          |
    Then The latest asset externalization transaction with "BUYBACK" status has the following TRANSFER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 800.00 |
      | ASSET         | 146000        | Asset transfer   | CREDIT    | 800.00 |
      | ASSET         | 112601        | Loans Receivable | CREDIT    | 800.00 |
      | ASSET         | 146000        | Asset transfer   | DEBIT     | 800.00 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName             | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable          | DEBIT     | 1000.00 |
      | ASSET         | 112601        | Loans Receivable          | CREDIT    | 200.00  |
      | LIABILITY     | 145023        | Suspense/Clearing account | DEBIT     | 200.00  |
      | ASSET         | 112601        | Loans Receivable          | CREDIT    | 800.00  |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow

   @AssetExternalizationJournalEntry
  Scenario: Verify that Asset externalization SALES and BUYBACK has the correct Journal entries: PAYOUT_REFUND transaction while status is ACTIVE
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    Then The latest asset externalization transaction with "ACTIVE" status has the following TRANSFER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | CREDIT    | 1000.00 |
      | ASSET         | 146000        | Asset transfer   | DEBIT     | 1000.00 |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
      | ASSET         | 146000        | Asset transfer   | CREDIT    | 1000.00 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
    When Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "22 May 2023" with 200 EUR transaction amount and system-generated Idempotency key and check external owner
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-30     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-22    | 9999-12-31  | BUYBACK          |
    When Admin sets the business date to "31 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 2023-05-30  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-22    | 2023-05-30  | BUYBACK          |
    Then The latest asset externalization transaction with "BUYBACK" status has the following TRANSFER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 800.00 |
      | ASSET         | 146000        | Asset transfer   | CREDIT    | 800.00 |
      | ASSET         | 112601        | Loans Receivable | CREDIT    | 800.00 |
      | ASSET         | 146000        | Asset transfer   | DEBIT     | 800.00 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName             | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable          | DEBIT     | 1000.00 |
      | ASSET         | 112601        | Loans Receivable          | CREDIT    | 200.00  |
      | LIABILITY     | 145023        | Suspense/Clearing account | DEBIT     | 200.00  |
      | ASSET         | 112601        | Loans Receivable          | CREDIT    | 800.00  |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow

   @AssetExternalizationJournalEntry
  Scenario: Verify that Asset externalization SALES and BUYBACK has the correct Journal entries: REPAYMENT_ADJUSTMENT_REFUND chargeback transaction while status is ACTIVE
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    Then The latest asset externalization transaction with "ACTIVE" status has the following TRANSFER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | CREDIT    | 1000.00 |
      | ASSET         | 146000        | Asset transfer   | DEBIT     | 1000.00 |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
      | ASSET         | 146000        | Asset transfer   | CREDIT    | 1000.00 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "22 May 2023" with 1000 EUR transaction amount and system-generated Idempotency key and check external owner
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-30     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-22    | 9999-12-31  | BUYBACK          |
    When Admin sets the business date to "25 May 2023"
    When Admin makes "REPAYMENT_ADJUSTMENT_REFUND" chargeback with 800 EUR transaction amount
    When Admin sets the business date to "31 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 2023-05-30  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-22    | 2023-05-30  | BUYBACK          |
    Then The latest asset externalization transaction with "BUYBACK" status has the following TRANSFER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 800.00 |
      | ASSET         | 146000        | Asset transfer   | CREDIT    | 800.00 |
      | ASSET         | 112601        | Loans Receivable | CREDIT    | 800.00 |
      | ASSET         | 146000        | Asset transfer   | DEBIT     | 800.00 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName             | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable          | DEBIT     | 1000.00 |
      | ASSET         | 112601        | Loans Receivable          | CREDIT    | 1000.00 |
      | LIABILITY     | 145023        | Suspense/Clearing account | DEBIT     | 1000.00 |
      | ASSET         | 112601        | Loans Receivable          | DEBIT     | 800.00  |
      | LIABILITY     | 145023        | Suspense/Clearing account | CREDIT    | 800.00  |
      | ASSET         | 112601        | Loans Receivable          | CREDIT    | 800.00  |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow

   @AssetExternalizationJournalEntry
  Scenario: Verify that Asset externalization SALES and BUYBACK has the correct Journal entries: CHARGE ADJUSTMENT transaction while status is ACTIVE
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    Then The latest asset externalization transaction with "ACTIVE" status has the following TRANSFER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | CREDIT    | 1000.00 |
      | ASSET         | 146000        | Asset transfer   | DEBIT     | 1000.00 |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
      | ASSET         | 146000        | Asset transfer   | CREDIT    | 1000.00 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName    | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable | DEBIT     | 1000.00 |
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "24 May 2023" due date and 300 EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-30     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-22    | 9999-12-31  | BUYBACK          |
    When Admin sets the business date to "25 May 2023"
    When Admin runs inline COB job for Loan
    When Admin makes a charge adjustment for the last "LOAN_SNOOZE_FEE" type charge which is due on "24 May 2023" with 100 EUR transaction amount and externalId ""
    When Admin sets the business date to "31 May 2023"
    When Admin runs inline COB job for Loan
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 2023-05-30  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-22    | 2023-05-30  | BUYBACK          |
    Then The latest asset externalization transaction with "BUYBACK" status has the following TRANSFER Journal entries:
      | glAccountType | glAccountCode | glAccountName           | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable        | DEBIT     | 1000.00 |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 200.00  |
      | ASSET         | 146000        | Asset transfer          | CREDIT    | 1200.00 |
      | ASSET         | 112601        | Loans Receivable        | CREDIT    | 1000.00 |
      | ASSET         | 112603        | Interest/Fee Receivable | CREDIT    | 200.00  |
      | ASSET         | 146000        | Asset transfer          | DEBIT     | 1200.00 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName           | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable        | DEBIT     | 1000.00 |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 300.00  |
      | INCOME        | 404007        | Fee Income              | CREDIT    | 300.00  |
      | ASSET         | 112603        | Interest/Fee Receivable | CREDIT    | 100.00  |
      | INCOME        | 404007        | Fee Income              | DEBIT     | 100.00  |
      | ASSET         | 112601        | Loans Receivable        | CREDIT    | 1000.00 |
      | ASSET         | 112603        | Interest/Fee Receivable | CREDIT    | 200.00  |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that LoanOwnershipTransferBusinessEvent and LoanAccountSnapshotBusinessEvent is created with correct data
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    Then Asset externalization details has the generated transferExternalId
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    When Admin sets the business date to "25 May 2023"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-30     |                    |
    When Admin sets the business date to "31 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 2023-05-30  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-25    | 2023-05-30  | BUYBACK          |
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that LoanOwnershipTransferBusinessEvent and LoanAccountSnapshotBusinessEvent is created with correct data for partial repayment, fee, penalty
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "1 May 2023" with 500 EUR transaction amount and system-generated Idempotency key
    When Admin adds "LOAN_NSF_FEE" due date charge with "1 May 2023" due date and 15 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "1 May 2023" due date and 20 EUR transaction amount
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    Then Asset externalization details has the generated transferExternalId
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    When Admin sets the business date to "25 May 2023"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-30     |                    |
    When Admin sets the business date to "31 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 2023-05-30  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-25    | 2023-05-30  | BUYBACK          |
    Then LoanOwnershipTransferBusinessEvent is created
    Then LoanAccountSnapshotBusinessEvent is created
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow

    #  TODO right now COB does not pick up closed loans
  @Skip
  Scenario: Verify that when loan got fully paid while SALES on PENDING ..........
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "10 May 2023"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "10 May 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
#    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with the following data:
#      | settlementDate | ownerExternalId | purchasePriceRatio | status  | effectiveFrom | effectiveTo |Transaction type |
#      | 2023-05-21     | TestOwner       | 1                  | PENDING | 2023-05-01    | 9999-12-31  |SALE             |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow

#  TODO right now COB does not pick up closed loans
  @Skip
  Scenario: Verify that when loan got overpaid while SALES on PENDING ..........
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "10 May 2023"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "10 May 2023" with 1200 EUR transaction amount
    Then Loan status will be "OVERPAID"
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
#    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with the following data:
#      | settlementDate | ownerExternalId | purchasePriceRatio | status  | effectiveFrom | effectiveTo |Transaction type |
#      | 2023-05-21     | TestOwner       | 1                  | PENDING | 2023-05-01    | 9999-12-31  |SALE             |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


# it is for making easier the event check manually on Postman / when event check automation scenarios implemented, it should be deleted
  @Skip @events
  Scenario: Verify that Asset externalization details are correct after CoB in case of a SALES request by loan id
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin sets the business date to "25 May 2023"
    When Admin runs inline COB job for Loan
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "25 May 2023" due date and 10 EUR transaction amount
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "25 May 2023" with 200 EUR transaction amount and self-generated Idempotency key and check external owner
    When Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "25 May 2023" with 200 EUR transaction amount and self-generated Idempotency key and check external owner
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "25 May 2023" with 200 EUR transaction amount and self-generated Idempotency key and check external owner
    And Customer makes "AUTOPAY" repayment on "25 May 2023" with 200 EUR transaction amount and check external owner
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that SALE and BUYBACK can be cancelled in right order
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request for type "SALE" by Loan ID with unique ownerExternalId, user-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId, ignore transactionExternalId and contain the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  |
    When Admin sets the business date to "10 May 2023"
    When Admin makes asset externalization request for type "BUYBACK" by Loan ID with unique ownerExternalId, user-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-21     |                    |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId, ignore transactionExternalId and contain the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  |
      | 2023-05-21     | 1                  | BUYBACK | 2023-05-10    | 9999-12-31  |
    When Admin send "cancel" command to the transaction type "SALE" will throw error
    When Admin send "cancel" command to the transaction type "BUYBACK"
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId, ignore transactionExternalId and contain the following data:
      | settlementDate | purchasePriceRatio | status    | effectiveFrom | effectiveTo |
      | 2023-05-21     | 1                  | PENDING   | 2023-05-01    | 9999-12-31  |
      | 2023-05-21     | 1                  | BUYBACK   | 2023-05-10    | 2023-05-10  |
      | 2023-05-21     | 1                  | CANCELLED | 2023-05-10    | 2023-05-10  |
    When Admin send "cancel" command to the transaction type "SALE"
    Then Fetching Asset externalization details by loan id gives numberOfElements: 4 with correct ownerExternalId, ignore transactionExternalId and contain the following data:
      | settlementDate | purchasePriceRatio | status    | effectiveFrom | effectiveTo |
      | 2023-05-21     | 1                  | PENDING   | 2023-05-01    | 2023-05-10  |
      | 2023-05-21     | 1                  | BUYBACK   | 2023-05-10    | 2023-05-10  |
      | 2023-05-21     | 1                  | CANCELLED | 2023-05-10    | 2023-05-10  |
      | 2023-05-21     | 1                  | CANCELLED | 2023-05-01    | 2023-05-10  |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that SALE can be cancelled
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, user-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId, ignore transactionExternalId and contain the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  |
    When Admin sets the business date to "10 May 2023"
    When Admin send "cancel" command on "SALE" transaction
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId, ignore transactionExternalId and contain the following data:
      | settlementDate | purchasePriceRatio | status    | effectiveFrom | effectiveTo |
      | 2023-05-21     | 1                  | PENDING   | 2023-05-01    | 2023-05-10  |
      | 2023-05-21     | 1                  | CANCELLED | 2023-05-01    | 2023-05-10  |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that active SALE can not be cancelled
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, user-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin send "cancel" command on "SALE" transaction it will throw an error
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that Asset cannot be cancelled after SALE and BUYBACK is completed
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "10 May 2023"
    When Admin runs inline COB job for Loan
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-30     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-10    | 9999-12-31  | BUYBACK          |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-10    | 9999-12-31  | BUYBACK          |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin sets the business date to "31 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-10    | 2023-05-30  | BUYBACK          |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 2023-05-30  | SALE             |
    When Admin send "cancel" command on "SALE" transaction it will throw an error
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that SALE and BUYBACK can be cancelled in right order with double cancel test
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request for type "SALE" by Loan ID with unique ownerExternalId, user-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId, ignore transactionExternalId and contain the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  |
    When Admin send "cancel" command to the transaction type "SALE"
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId, ignore transactionExternalId and contain the following data:
      | settlementDate | purchasePriceRatio | status    | effectiveFrom | effectiveTo |
      | 2023-05-21     | 1                  | PENDING   | 2023-05-01    | 2023-05-01  |
      | 2023-05-21     | 1                  | CANCELLED | 2023-05-01    | 2023-05-01  |
    When Admin makes asset externalization request for type "SALE" by Loan ID with unique ownerExternalId, force generated transferExternalId and without change test owner with following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId, ignore transactionExternalId and contain the following data:
      | settlementDate | purchasePriceRatio | status    | effectiveFrom | effectiveTo |
      | 2023-05-21     | 1                  | PENDING   | 2023-05-01    | 2023-05-01  |
      | 2023-05-21     | 1                  | CANCELLED | 2023-05-01    | 2023-05-01  |
      | 2023-05-21     | 1                  | PENDING   | 2023-05-01    | 9999-12-31  |
    When Admin sets the business date to "10 May 2023"
    When Admin makes asset externalization request for type "BUYBACK" by Loan ID with unique ownerExternalId, user-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-21     |                    |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 4 with correct ownerExternalId, ignore transactionExternalId and contain the following data:
      | settlementDate | purchasePriceRatio | status    | effectiveFrom | effectiveTo |
      | 2023-05-21     | 1                  | PENDING   | 2023-05-01    | 2023-05-01  |
      | 2023-05-21     | 1                  | CANCELLED | 2023-05-01    | 2023-05-01  |
      | 2023-05-21     | 1                  | PENDING   | 2023-05-01    | 9999-12-31  |
      | 2023-05-21     | 1                  | BUYBACK   | 2023-05-10    | 9999-12-31  |
    When Admin send "cancel" command to the transaction type "SALE" will throw error
    When Admin send "cancel" command to the transaction type "BUYBACK"
    Then Fetching Asset externalization details by loan id gives numberOfElements: 5 with correct ownerExternalId, ignore transactionExternalId and contain the following data:
      | settlementDate | purchasePriceRatio | status    | effectiveFrom | effectiveTo |
      | 2023-05-21     | 1                  | PENDING   | 2023-05-01    | 2023-05-01  |
      | 2023-05-21     | 1                  | CANCELLED | 2023-05-01    | 2023-05-01  |
      | 2023-05-21     | 1                  | PENDING   | 2023-05-01    | 9999-12-31  |
      | 2023-05-21     | 1                  | BUYBACK   | 2023-05-10    | 2023-05-10  |
      | 2023-05-21     | 1                  | CANCELLED | 2023-05-10    | 2023-05-10  |
    When Admin send "cancel" command to the transaction type "SALE"
    Then Fetching Asset externalization details by loan id gives numberOfElements: 6 with correct ownerExternalId, ignore transactionExternalId and contain the following data:
      | settlementDate | purchasePriceRatio | status    | effectiveFrom | effectiveTo |
      | 2023-05-21     | 1                  | PENDING   | 2023-05-01    | 2023-05-01  |
      | 2023-05-21     | 1                  | CANCELLED | 2023-05-01    | 2023-05-01  |
      | 2023-05-21     | 1                  | PENDING   | 2023-05-01    | 2023-05-10  |
      | 2023-05-21     | 1                  | BUYBACK   | 2023-05-10    | 2023-05-10  |
      | 2023-05-21     | 1                  | CANCELLED | 2023-05-10    | 2023-05-10  |
      | 2023-05-21     | 1                  | CANCELLED | 2023-05-01    | 2023-05-10  |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that when a loan with PENDING SALES is fully paid asset transfer status will be DECLINED
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "15 May 2023"
    And Customer makes "AUTOPAY" repayment on "15 May 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status   | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING  | 2023-05-01    | 2023-05-15  | SALE             |
      | 2023-05-21     | 1                  | DECLINED | 2023-05-15    | 2023-05-15  | SALE             |
    Then LoanOwnershipTransferBusinessEvent with transfer status: "DECLINED" and transfer status reason "BALANCE_ZERO" is created
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that when a loan with PENDING SALES is overpaid asset transfer status will be DECLINED
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "15 May 2023"
    And Customer makes "AUTOPAY" repayment on "15 May 2023" with 1200 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status   | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING  | 2023-05-01    | 2023-05-15  | SALE             |
      | 2023-05-21     | 1                  | DECLINED | 2023-05-15    | 2023-05-15  | SALE             |
    Then LoanOwnershipTransferBusinessEvent with transfer status: "DECLINED" and transfer status reason "BALANCE_NEGATIVE" is created
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that when a loan with PENDING BUYBACK is fully paid BUYBACK transaction can be done successfully
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-30     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-22    | 9999-12-31  | BUYBACK          |
    When Admin sets the business date to "25 May 2023"
    And Customer makes "AUTOPAY" repayment on "25 May 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "31 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 2023-05-25  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-22    | 2023-05-25  | BUYBACK          |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that when a loan with PENDING BUYBACK is overpaid BUYBACK transaction can be done successfully
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
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
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, system-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-30     |                    |
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-22    | 9999-12-31  | BUYBACK          |
    When Admin sets the business date to "25 May 2023"
    And Customer makes "AUTOPAY" repayment on "25 May 2023" with 1200 EUR transaction amount
    Then Loan status will be "OVERPAID"
    When Admin sets the business date to "31 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 3 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 2023-05-25  | SALE             |
      | 2023-05-30     | 1                  | BUYBACK | 2023-05-22    | 2023-05-25  | BUYBACK          |
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow


  Scenario: Verify that transaction and transaction adjustment events has the proper external owner
    Given Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin makes asset externalization request by Loan ID with unique ownerExternalId, user-generated transferExternalId and the following data:
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     | 1                  |
    Then Asset externalization response has the correct Loan ID, transferExternalId
    Then Fetching Asset externalization details by loan id gives numberOfElements: 1 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 9999-12-31  | SALE             |
    And Customer makes "AUTOPAY" repayment on "1 May 2023" with 10 EUR transaction amount
    When Admin sets the business date to "22 May 2023"
    When Admin runs inline COB job for Loan
    Then Fetching Asset externalization details by loan id gives numberOfElements: 2 with correct ownerExternalId and the following data:
      | settlementDate | purchasePriceRatio | status  | effectiveFrom | effectiveTo | Transaction type |
      | 2023-05-21     | 1                  | PENDING | 2023-05-01    | 2023-05-21  | SALE             |
      | 2023-05-21     | 1                  | ACTIVE  | 2023-05-22    | 9999-12-31  | SALE             |
    And Customer makes "AUTOPAY" repayment on "22 May 2023" with 10 EUR transaction amount and check external owner
    When Customer adjust "1"th repayment on "22 May 2023" with amount "9" and check external owner
    Then Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow
