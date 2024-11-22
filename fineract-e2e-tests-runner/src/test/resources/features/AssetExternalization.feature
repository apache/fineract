@AssetExternalizationFeature
Feature: Asset Externalization

  @TestRailId:C2722
  Scenario: Verify that all fields and values are correct in case of a SALES request by loan id and user-generated transferExternalId
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

  @TestRailId:C2723
  Scenario: Verify that all fields and values are correct in case of a SALES request by loan id system-generated transferExternalId
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

  @TestRailId:C2724
  Scenario: Verify that all fields and values are correct in case of a SALES request by loan external id user-generated transferExternalId
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

  @TestRailId:C2725
  Scenario: Verify that all fields and values are correct in case of a SALES request by loan external id system-generated transferExternalId
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

  @TestRailId:C2727
  Scenario: Verify that Asset externalization details are correct after CoB in case of a SALES request by loan id
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

  @TestRailId:C2729
  Scenario: Verify that Asset externalization details has the correct data in case of a BUYBACK request placed before the settlementDate with a same settlementDate as the sales one
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

  @TestRailId:C2730
  Scenario: Verify that Asset externalization details has the correct data in case of a BUYBACK request placed on a business date before the settlementDate of sales request and with a settlementDate for buyback after the sales got active
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

  @TestRailId:C2731
  Scenario: Verify that Asset externalization details has the correct data in case of a BUYBACK request placed after the settlementDate
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

  @TestRailId:C2732
  Scenario: Verify that BUYBACK request on a loan with PENDING ownership where BUYBACK settlement date is earlier than SALE settlement date results an error
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

  @TestRailId:C2733
  Scenario: Verify that SALES request on a fully paid loan results an error
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

  @TestRailId:C2734
  Scenario: Verify that SALES request on an overpaid loan results an error
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

  @TestRailId:C2735
  Scenario: Verify that SALES request on a loan with ACTIVE ownership results an error
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

  @TestRailId:C2736
  Scenario: Verify that BUYBACK request on a fully paid loan can be done successfully
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

  @TestRailId:C2737
  Scenario: Verify that BUYBACK request on an overpaid loan can be done successfully
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

  @TestRailId:C2738
  Scenario: Verify that BUYBACK request on a loan with INACTIVE ownership results an error
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Asset externalization transaction with the following data results a 403 error and "ASSET_NOT_OWNED_CANNOT_BE_BOUGHT" error message
      | Transaction type | settlementDate | purchasePriceRatio |
      | buyback          | 2023-05-21     |                    |

  @TestRailId:C2739
  Scenario: Verify that SALES request can NOT be placed on a loan which is not APPROVED yet
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    Then Loan status will be "SUBMITTED_AND_PENDING_APPROVAL"
    Then Asset externalization transaction with the following data results a 403 error and "LOAN_NOT_ACTIVE" error message
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-30     | 1                  |

  @TestRailId:C2740
  Scenario: Verify that SALES request can NOT be placed on a loan which is not DISBURSED yet
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    Then Loan status will be "APPROVED"
    Then Asset externalization transaction with the following data results a 403 error and "LOAN_NOT_ACTIVE" error message
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-30     | 1                  |

  @TestRailId:C2741
  Scenario: Verify that SALES request on a loan with PENDING ownership results an error
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

  @TestRailId:C2742
  Scenario: Verify that SALES with settlement date earlier than actual business date results an error
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Asset externalization transaction with the following data results a 403 error and "SETTLEMENT_DATE_IN_THE_PAST" error message
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-04-21     | 1                  |

  @TestRailId:C2743
  Scenario: Verify that SALES with null owner external id results an error
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Asset externalization SALES transaction with ownerExternalId = null and the following data results a 400 error and "INVALID_REQUEST" error message
      | settlementDate | purchasePriceRatio |
      | 2023-05-21     | 1                  |

  @TestRailId:C2744
  Scenario: Verify that SALES with null purchase price ratio results an error
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Asset externalization transaction with the following data results a 400 error and "INVALID_REQUEST" error message
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             | 2023-05-21     |                    |

  @TestRailId:C2745
  Scenario: Verify that SALES with null settlement date results an error
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 May 2023"
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Asset externalization transaction with the following data results a 400 error and "INVALID_REQUEST" error message
      | Transaction type | settlementDate | purchasePriceRatio |
      | sale             |                | 1                  |

  @TestRailId:C2746
  Scenario: Verify that BUYBACK request on a loan with PENDING BUYBACK ownership result an error
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

  @TestRailId:C2747
  Scenario: Verify that BUYBACK with settlement date earlier than actual business date results an error
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

  @TestRailId:C2748
  Scenario: Verify that BUYBACK with ownerExternalId=NULL can be placed, and results a 200OK response
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

  @TestRailId:C2749
  Scenario: Verify that BUYBACK with purchasePriceRatio=NULL can be placed, and results a 200OK response
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

  @TestRailId:C2750
  Scenario: Verify that SALES with null settlement date results an error
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

  @TestRailId:C2751 @AssetExternalizationJournalEntry
  Scenario: Verify that Asset externalization SALES and BUYBACK has the correct Journal entries: no other transactions
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

  @TestRailId:C2752 @AssetExternalizationJournalEntry
  Scenario: Verify that Asset externalization SALES and BUYBACK has the correct Journal entries: fee applied before sale, and penalty applied before buyback
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

  @TestRailId:C2753 @AssetExternalizationJournalEntry
  Scenario: Verify that Asset externalization SALES and BUYBACK has the correct Journal entries: Repyment while status is ACTIVE
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

  @TestRailId:C2754 @AssetExternalizationJournalEntry
  Scenario: Verify that Asset externalization SALES and BUYBACK has the correct Journal entries: GOODWILL_CREDIT transaction while status is ACTIVE
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

  @TestRailId:C2755 @AssetExternalizationJournalEntry
  Scenario: Verify that Asset externalization SALES and BUYBACK has the correct Journal entries: MERCHANT_ISSUED_REFUND transaction while status is ACTIVE
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

  @TestRailId:C2756 @AssetExternalizationJournalEntry
  Scenario: Verify that Asset externalization SALES and BUYBACK has the correct Journal entries: PAYOUT_REFUND transaction while status is ACTIVE
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

  @TestRailId:C2757 @AssetExternalizationJournalEntry
  Scenario: Verify that Asset externalization SALES and BUYBACK has the correct Journal entries: REPAYMENT_ADJUSTMENT_REFUND chargeback transaction while status is ACTIVE
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

  @TestRailId:C2758 @AssetExternalizationJournalEntry
  Scenario: Verify that Asset externalization SALES and BUYBACK has the correct Journal entries: CHARGE ADJUSTMENT transaction while status is ACTIVE
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

  @TestRailId:C2759
  Scenario: Verify that LoanOwnershipTransferBusinessEvent and LoanAccountSnapshotBusinessEvent is created with correct data
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

  @TestRailId:C2760
  Scenario: Verify that LoanOwnershipTransferBusinessEvent and LoanAccountSnapshotBusinessEvent is created with correct data for partial repayment, fee, penalty
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

  @TestRailId:C2771
  Scenario: Verify that SALE and BUYBACK can be cancelled in right order
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

  @TestRailId:C2772
  Scenario: Verify that SALE can be cancelled
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

  @TestRailId:C2773
  Scenario: Verify that active SALE can not be cancelled
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

  @TestRailId:C2774
  Scenario: Verify that Asset cannot be cancelled after SALE and BUYBACK is completed
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

  @TestRailId:C2775
  Scenario: Verify that SALE and BUYBACK can be cancelled in right order with double cancel test
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

  @TestRailId:C2785
  Scenario: Verify that when a loan with PENDING SALES is fully paid asset transfer status will be DECLINED
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

  @TestRailId:C2786
  Scenario: Verify that when a loan with PENDING SALES is overpaid asset transfer status will be DECLINED
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

  @TestRailId:C2787
  Scenario: Verify that when a loan with PENDING BUYBACK is fully paid BUYBACK transaction can be done successfully
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

  @TestRailId:C2788
  Scenario: Verify that when a loan with PENDING BUYBACK is overpaid BUYBACK transaction can be done successfully
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

  @TestRailId:C2811
  Scenario: Verify that transaction and transaction adjustment events has the proper external owner
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


  @TestRailId:C3193
  Scenario: Verify that Asset externalization SALES and BUYBACK has the correct Journal entries: no other transactions - interest bearing loan
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL | 01 May 2023       | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
      | ASSET         | 112603        | Interest/Fee Receivable | CREDIT    | 45.57   |
      | ASSET         | 146000        | Asset transfer          | DEBIT     | 1045.57 |
      | ASSET         | 112601        | Loans Receivable        | DEBIT     | 1000.00 |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 45.57   |
      | ASSET         | 146000        | Asset transfer          | CREDIT    | 1045.57 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName           | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable        | DEBIT     | 1000.00 |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 45.57   |
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
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 65.57   |
      | ASSET         | 146000        | Asset transfer          | CREDIT    | 1065.57 |
      | ASSET         | 112601        | Loans Receivable        | CREDIT    | 1000.00 |
      | ASSET         | 112603        | Interest/Fee Receivable | CREDIT    | 65.57   |
      | ASSET         | 146000        | Asset transfer          | DEBIT     | 1065.57 |
    Then The asset external owner has the following OWNER Journal entries:
      | glAccountType | glAccountCode | glAccountName           | entryType | amount  |
      | ASSET         | 112601        | Loans Receivable        | DEBIT     | 1000.00 |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 45.57   |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 0.33    |
      | INCOME        | 404000        | Interest Income         | CREDIT    | 0.33    |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 0.33    |
      | INCOME        | 404000        | Interest Income         | CREDIT    | 0.33    |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 0.33    |
      | INCOME        | 404000        | Interest Income         | CREDIT    | 0.33    |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 0.33    |
      | INCOME        | 404000        | Interest Income         | CREDIT    | 0.33    |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 0.33    |
      | INCOME        | 404000        | Interest Income         | CREDIT    | 0.33    |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 0.33    |
      | INCOME        | 404000        | Interest Income         | CREDIT    | 0.33    |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 0.33    |
      | INCOME        | 404000        | Interest Income         | CREDIT    | 0.33    |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 0.33    |
      | INCOME        | 404000        | Interest Income         | CREDIT    | 0.33    |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 0.33    |
      | INCOME        | 404000        | Interest Income         | CREDIT    | 0.33    |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 0.33    |
      | INCOME        | 404000        | Interest Income         | CREDIT    | 0.33    |
      | ASSET         | 112603        | Interest/Fee Receivable | DEBIT     | 0.33    |
      | INCOME        | 404000        | Interest Income         | CREDIT    | 0.33    |
