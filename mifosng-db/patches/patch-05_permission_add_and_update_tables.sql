/* some addition permissions and alterations */
INSERT INTO m_permission (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) 
VALUES ('configuration', 'UPDATE_PERMISSION', 'PERMISSION', 'UPDATE', '0');

INSERT INTO m_permission (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) 
VALUES ('configuration', 'UPDATE_PERMISSION_CHECKER', 'PERMISSION', 'UPDATE', '0');

INSERT INTO m_permission (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) 
VALUES ('configuration', 'READ_DATATABLE', 'DATATABLE', 'READ', '0');

INSERT INTO m_permission (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) 
VALUES ('configuration', 'REGISTER_DATATABLE', 'DATATABLE', 'REGISTER', '0');

INSERT INTO m_permission (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) 
VALUES ('configuration', 'REGISTER_DATATABLE_CHECKER', 'DATATABLE', 'REGISTER', '0');

INSERT INTO m_permission (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) 
VALUES ('configuration', 'DEREGISTER_DATATABLE', 'DATATABLE', 'DEREGISTER', '0');

INSERT INTO m_permission (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) 
VALUES ('configuration', 'DEREGISTER_DATATABLE_CHECKER', 'DATATABLE', 'DEREGISTER', '0');

INSERT INTO m_permission (`grouping`, `code`, `can_maker_checker`) 
VALUES ('special', 'CHECKER_SUPER_USER', '0');


update m_permission 
set grouping = 'configuration'
where code like '%permission%';

