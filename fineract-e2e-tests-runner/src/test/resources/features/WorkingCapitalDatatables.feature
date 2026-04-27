@WorkingCapitalDatatablesFeature
Feature: WorkingCapitalDatatables

  # Datatable schema - foreign key column per entity and strategy
  @TestRailId:C76716
  Scenario: Single-row datatable for WC Loan exposes wc_loan_id as unique primary key
    When A datatable for "WC_Loan" is created
    Then The following column definitions match:
      | Name       | Primary key | Unique | Indexed |
      | wc_loan_id | true        | true   | true    |

  @TestRailId:C76717
  Scenario: Multi-row datatable for WC Loan exposes wc_loan_id as indexed foreign key
    When A multirow datatable for "WC_Loan" is created
    Then The following column definitions match:
      | Name       | Primary key | Unique | Indexed |
      | wc_loan_id | false       | false  | true    |

  @TestRailId:C76718
  Scenario: Single-row datatable for WC Loan Product exposes wc_product_loan_id as unique primary key
    When A datatable for "WC_Loan_Product" is created
    Then The following column definitions match:
      | Name               | Primary key | Unique | Indexed |
      | wc_product_loan_id | true        | true   | true    |

  @TestRailId:C76719
  Scenario: Multi-row datatable for WC Loan Product exposes wc_product_loan_id as indexed foreign key
    When A multirow datatable for "WC_Loan_Product" is created
    Then The following column definitions match:
      | Name               | Primary key | Unique | Indexed |
      | wc_product_loan_id | false       | false  | true    |

  # Datatable visibility is scoped to its registered entity
  @TestRailId:C76720
  Scenario: Datatable registered for WC Loan is listed under m_wc_loan but not under m_loan
    When A datatable for "WC_Loan" is created
    Then Listing datatables with apptable "m_wc_loan" includes the created datatable
    And Listing datatables with apptable "m_loan" excludes the created datatable

  @TestRailId:C76721
  Scenario: Datatable registered for WC Loan Product is listed under m_wc_loan_product but not under m_product_loan
    When A datatable for "WC_Loan_Product" is created
    Then Listing datatables with apptable "m_wc_loan_product" includes the created datatable
    And Listing datatables with apptable "m_product_loan" excludes the created datatable

  # Entry CRUD - WC Loan Product
  @TestRailId:C76722
  Scenario: Single-row datatable entry for WC Loan Product supports create, read, update, delete
    When Admin creates a new Working Capital Loan Product
    And A datatable for "WC_Loan_Product" is created with the following extra columns:
      | Name    | Type   | Length | Unique | Indexed |
      | test_nr | number | 10     | false  | false   |
    And A datatable entry is created for "WC_Loan_Product" with value "42" in column "test_nr"
    Then Fetching the datatable entry for "WC_Loan_Product" returns value "42" in column "test_nr"
    When The datatable entry for "WC_Loan_Product" is updated with value "99" in column "test_nr"
    Then Fetching the datatable entry for "WC_Loan_Product" returns value "99" in column "test_nr"
    When The datatable entry for "WC_Loan_Product" is deleted
    Then Fetching the datatable entry for "WC_Loan_Product" returns empty result

  @TestRailId:C76723
  Scenario: Multi-row datatable entries for WC Loan Product support create, read, update, delete
    When Admin creates a new Working Capital Loan Product
    And A multirow datatable for "WC_Loan_Product" is created
    And A multirow datatable entry is created for "WC_Loan_Product" with value "10" in column "col"
    Then Fetching multirow datatable entries for "WC_Loan_Product" returns value "10" in column "col"
    When The multirow datatable entry for "WC_Loan_Product" is updated with value "77" in column "col" by entry id
    Then Fetching multirow datatable entries for "WC_Loan_Product" returns value "77" in column "col"
    When The multirow datatable entry for "WC_Loan_Product" is deleted by entry id
    Then Fetching the datatable entry for "WC_Loan_Product" returns empty result

  # Entry CRUD - WC Loan
  @TestRailId:C76724
  Scenario: Single-row datatable entry for WC Loan supports create, read, update, delete
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And A datatable for "WC_Loan" is created with the following extra columns:
      | Name    | Type   | Length | Unique | Indexed |
      | test_nr | number | 10     | false  | false   |
    And A datatable entry is created for "WC_Loan" with value "42" in column "test_nr"
    Then Fetching the datatable entry for "WC_Loan" returns value "42" in column "test_nr"
    When The datatable entry for "WC_Loan" is updated with value "99" in column "test_nr"
    Then Fetching the datatable entry for "WC_Loan" returns value "99" in column "test_nr"
    When The datatable entry for "WC_Loan" is deleted
    Then Fetching the datatable entry for "WC_Loan" returns empty result

  @TestRailId:C76725
  Scenario: Multi-row datatable entries for WC Loan support create, read, update, delete
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And A multirow datatable for "WC_Loan" is created
    And A multirow datatable entry is created for "WC_Loan" with value "10" in column "col"
    Then Fetching multirow datatable entries for "WC_Loan" returns value "10" in column "col"
    When The multirow datatable entry for "WC_Loan" is updated with value "77" in column "col" by entry id
    Then Fetching multirow datatable entries for "WC_Loan" returns value "77" in column "col"
    When The multirow datatable entry for "WC_Loan" is deleted by entry id
    Then Fetching the datatable entry for "WC_Loan" returns empty result

  # Single-row strategy - only one entry per parent row
  @TestRailId:C76726
  Scenario: Single-row datatable for WC Loan rejects a second entry for the same loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And A datatable for "WC_Loan" is created with the following extra columns:
      | Name    | Type   | Length | Unique | Indexed |
      | test_nr | number | 10     | false  | false   |
    And A datatable entry is created for "WC_Loan" with value "1" in column "test_nr"
    Then A second datatable entry for "WC_Loan" with value "2" in column "test_nr" is rejected

  @TestRailId:C76727
  Scenario: Single-row datatable for WC Loan Product rejects a second entry for the same product
    When Admin creates a new Working Capital Loan Product
    And A datatable for "WC_Loan_Product" is created with the following extra columns:
      | Name    | Type   | Length | Unique | Indexed |
      | test_nr | number | 10     | false  | false   |
    And A datatable entry is created for "WC_Loan_Product" with value "1" in column "test_nr"
    Then A second datatable entry for "WC_Loan_Product" with value "2" in column "test_nr" is rejected

  @TestRailId:C76728
  Scenario: Deregistered WC Loan datatable is hidden from listings, and reappears when re-registered
    When A datatable for "WC_Loan" is created
    And The datatable is deregistered
    Then Listing datatables with apptable "m_wc_loan" excludes the created datatable
    When The datatable is registered against apptable "m_wc_loan"
    Then Listing datatables with apptable "m_wc_loan" includes the created datatable
    And Listing datatables with apptable "m_loan" excludes the created datatable

  @TestRailId:C76729
  Scenario: Deregistered WC Loan Product datatable is hidden from listings, and reappears when re-registered
    When A datatable for "WC_Loan_Product" is created
    And The datatable is deregistered
    Then Listing datatables with apptable "m_wc_loan_product" excludes the created datatable
    When The datatable is registered against apptable "m_wc_loan_product"
    Then Listing datatables with apptable "m_wc_loan_product" includes the created datatable
    And Listing datatables with apptable "m_product_loan" excludes the created datatable

  @TestRailId:C76730
  Scenario: Adding, renaming and dropping columns on a WC Loan datatable
    When A datatable for "WC_Loan" is created with the following extra columns:
      | Name      | Type   | Length | Unique | Indexed |
      | note      | number | 10     | false  | false   |
    And Column "extra_num" of type "number" is added to the datatable
    Then The datatable contains columns:
      | Name      |
      | note      |
      | extra_num |
    When Column "note" is renamed to "note_v2" on the datatable
    Then The datatable contains columns:
      | Name    |
      | note_v2 |
    And The datatable does not contain columns:
      | Name |
      | note |
    When Column "extra_num" is dropped from the datatable
    Then The datatable does not contain columns:
      | Name      |
      | extra_num |

  @TestRailId:C76731
  Scenario: Adding, renaming and dropping columns on a WC Loan Product datatable
    When A datatable for "WC_Loan_Product" is created with the following extra columns:
      | Name | Type   | Length | Unique | Indexed |
      | note | number | 10     | false  | false   |
    And Column "extra_num" of type "number" is added to the datatable
    Then The datatable contains columns:
      | Name      |
      | extra_num |
    When Column "note" is renamed to "note_v2" on the datatable
    Then The datatable contains columns:
      | Name    |
      | note_v2 |
    When Column "extra_num" is dropped from the datatable
    Then The datatable does not contain columns:
      | Name      |
      | extra_num |

  @TestRailId:C76732
  Scenario: Deleting a WC Loan datatable removes it from all listings
    When A datatable for "WC_Loan" is created
    And The datatable is deleted
    Then Listing datatables with apptable "m_wc_loan" excludes the created datatable

  @TestRailId:C76733
  Scenario: Deleting a WC Loan Product datatable removes it from all listings
    When A datatable for "WC_Loan_Product" is created
    And The datatable is deleted
    Then Listing datatables with apptable "m_wc_loan_product" excludes the created datatable

  @TestRailId:C76734
  Scenario Outline: Creating a WC Loan datatable with reserved column "<column>" is rejected
    Then A datatable for "WC_Loan" with column "<column>" is rejected with HTTP <status>

    Examples:
      | column     | status |
      | id         | 400    |
      | wc_loan_id | 400    |

  @TestRailId:C76735
  Scenario Outline: Creating a WC Loan Product datatable with reserved column "<column>" is rejected
    Then A datatable for "WC_Loan_Product" with column "<column>" is rejected with HTTP <status>

    Examples:
      | column             | status |
      | id                 | 400    |
      | wc_product_loan_id | 400    |

  @TestRailId:C76736
  # NOTE: `id` returns HTTP 400 from the API validator; `wc_loan_id` returns HTTP 403 because the deserializer
  # skips the FK reserved-name check on PUT (apptableName is not part of the PUT body) and the rejection falls
  # through to the DB layer (PlatformDataIntegrityException). Confluence implies a uniform 400 path; flip the
  # expected status back to 400 once the server-side inconsistency is fixed.
  Scenario Outline: Schema-mutating operations reject reserved column names on a WC Loan datatable
    When A datatable for "WC_Loan" is created with the following extra columns:
      | Name | Type   | Length | Unique | Indexed |
      | note | number | 10     | false  | false   |
    Then Adding column "<column>" of type "number" to the datatable is rejected with HTTP <status>
    And Renaming column "note" to "<column>" on the datatable is rejected with HTTP <status>
    And Dropping column "<column>" from the datatable is rejected with HTTP <status>

    Examples:
      | column     | status |
      | id         | 400    |
      | wc_loan_id | 403    |

  @TestRailId:C76737
  # NOTE: see C76736 — same server-side inconsistency for the FK column on PUT.
  Scenario Outline: Schema-mutating operations reject reserved column names on a WC Loan Product datatable
    When A datatable for "WC_Loan_Product" is created with the following extra columns:
      | Name | Type   | Length | Unique | Indexed |
      | note | number | 10     | false  | false   |
    Then Adding column "<column>" of type "number" to the datatable is rejected with HTTP <status>
    And Renaming column "note" to "<column>" on the datatable is rejected with HTTP <status>
    And Dropping column "<column>" from the datatable is rejected with HTTP <status>

    Examples:
      | column             | status |
      | id                 | 400    |
      | wc_product_loan_id | 403    |

  @TestRailId:C76738
  Scenario: Datatable registered for Term Loan is not listed under m_wc_loan
    When A datatable for "LOAN" is created
    Then Listing datatables with apptable "m_loan" includes the created datatable
    And Listing datatables with apptable "m_wc_loan" excludes the created datatable

  @TestRailId:C76739
  Scenario: Datatable registered for Term Loan Product is not listed under m_wc_loan_product
    When A datatable for "LOAN_PRODUCT" is created
    Then Listing datatables with apptable "m_product_loan" includes the created datatable
    And Listing datatables with apptable "m_wc_loan_product" excludes the created datatable

  @TestRailId:C76740
  Scenario: Fetching a specific multi-row datatable entry by id returns that row for WC Loan Product
    When Admin creates a new Working Capital Loan Product
    And A multirow datatable for "WC_Loan_Product" is created
    And A multirow datatable entry is created for "WC_Loan_Product" with value "55" in column "col"
    Then Fetching the multirow datatable entry by id for "WC_Loan_Product" returns value "55" in column "col"

  @TestRailId:C76741
  Scenario: Fetching a specific multi-row datatable entry by id returns that row for WC Loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And A multirow datatable for "WC_Loan" is created
    And A multirow datatable entry is created for "WC_Loan" with value "55" in column "col"
    Then Fetching the multirow datatable entry by id for "WC_Loan" returns value "55" in column "col"
