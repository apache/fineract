Feature: Datatables

  Scenario: Datatable's primary key is unique and indexed
    When A datatable for "Loan" is created
    Then The following column definitions match:
      | Name    | Primary key | Unique | Indexed |
      | loan_id | true        | true   | true    |

  Scenario: Multirow datatable's primary key is unique and indexed
    When A multirow datatable for "Loan" is created
    Then The following column definitions match:
      | Name | Primary key | Unique | Indexed |
      | id   | true        | true   | true    |

  Scenario: Multirow datatable has the foreign key indexed
    When A multirow datatable for "Loan" is created
    Then The following column definitions match:
      | Name    | Primary key | Unique | Indexed |
      | loan_id | false       | false  | true    |

  Scenario: Datatable with unique constrained column is indexed
    When A datatable for "Loan" is created with the following extra columns:
      | Name | Type   | Length | Unique | Indexed |
      | col1 | string | 10     | true   | false   |

    Then The following column definitions match:
      | Name | Primary key | Unique | Indexed |
      | col1 | false       | true   | true    |

  Scenario: Datatable with indexed column
    When A datatable for "Loan" is created with the following extra columns:
      | Name | Type   | Length | Unique | Indexed |
      | col1 | string | 10     | false  | true    |

    Then The following column definitions match:
      | Name | Primary key | Unique | Indexed |
      | col1 | false       | false  | true    |

  Scenario Outline: Query data from datatable with invalid filter
    When A datatable for "Loan" is created
    And The client calls the query endpoint for the created datatable with "<column_filter>" column filter, and "<value_filter>" value filter
    Then The status of the HTTP response should be 400
    And The response body should contain the following message: "<error_message>"
    Examples:
      | column_filter | value_filter | error_message                            |
      | loan_id       | InvalidInput | validation.msg.invalid.integer.format    |
      | created_at    | InvalidInput | validation.msg.invalid.dateFormat.format |
      | invalidColumn | InvalidInput | validation.msg.validation.errors.exist   |
