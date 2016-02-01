
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
    VALUES
        ('configuration', 'CREATE_DATATABLE', 'DATATABLE', 'CREATE', 0),
        ('configuration', 'CREATE_DATATABLE_CHECKER', 'DATATABLE', 'CREATE', 0),
        ('configuration', 'UPDATE_DATATABLE', 'DATATABLE', 'UPDATE', 0),
        ('configuration', 'UPDATE_DATATABLE_CHECKER', 'DATATABLE', 'UPDATE', 0),
        ('configuration', 'DELETE_DATATABLE', 'DATATABLE', 'DELETE', 0),
        ('configuration', 'DELETE_DATATABLE_CHECKER', 'DATATABLE', 'DELETE', 0);
