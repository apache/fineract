INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_subtype`, `report_category`, `report_sql`, `description`, `core_report`, `use_report`) VALUES ('Active Loan Summary per Branch', 'Pentaho', NULL, 'Loans', NULL, NULL, 0, 1);

INSERT INTO `stretchy_parameter` (`parameter_name`, `parameter_variable`, `parameter_label`, `parameter_displayType`, `parameter_FormatType`, `parameter_default`, `special`, `selectOne`, `selectAll`, `parameter_sql`, `parent_id`) VALUES ('asOnDate', 'asOn', 'As On', 'date', 'date', 'today', NULL, NULL, NULL, NULL, NULL);

INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_subtype`, `report_category`, `report_sql`, `description`, `core_report`, `use_report`) VALUES ('Balance Outstanding', 'Pentaho', NULL, 'Loans', NULL, NULL, 0, 1);

INSERT INTO `stretchy_report_parameter` (report_id,parameter_id,report_parameter_name) VALUES ((select id from stretchy_report where report_name = 'Balance Outstanding'),(select id from stretchy_parameter where parameter_name='OfficeIdSelectOne'),'branch');
INSERT INTO `stretchy_report_parameter` (report_id,parameter_id,report_parameter_name) VALUES ((select id from stretchy_report where report_name = 'Balance Outstanding'),(select id from stretchy_parameter where parameter_name='asOnDate'),'ondate');

INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_subtype`, `report_category`, `report_sql`, `description`, `core_report`, `use_report`) VALUES ('Collection Report', 'Pentaho', NULL, 'Loans', NULL, NULL, 0, 1);
INSERT INTO `stretchy_report_parameter` (report_id,parameter_id,report_parameter_name) VALUES ((select id from stretchy_report where report_name = 'Collection Report'),(select id from stretchy_parameter where parameter_name='OfficeIdSelectOne'),'branch');
INSERT INTO `stretchy_report_parameter` (report_id,parameter_id,report_parameter_name) VALUES ((select id from stretchy_report where report_name = 'Collection Report'),(select id from stretchy_parameter where parameter_name='startDateSelect'),'fromDate');
INSERT INTO `stretchy_report_parameter` (report_id,parameter_id,report_parameter_name) VALUES ((select id from stretchy_report where report_name = 'Collection Report'),(select id from stretchy_parameter where parameter_name='endDateSelect'),'toDate');

INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_subtype`, `report_category`, `report_sql`, `description`, `core_report`, `use_report`) VALUES ('Disbursal Report', 'Pentaho', NULL, 'Loans', NULL, NULL, 0, 1);
INSERT INTO `stretchy_report_parameter` (report_id,parameter_id,report_parameter_name) VALUES ((select id from stretchy_report where report_name = 'Disbursal Report'),(select id from stretchy_parameter where parameter_name='OfficeIdSelectOne'),'branch');
INSERT INTO `stretchy_report_parameter` (report_id,parameter_id,report_parameter_name) VALUES ((select id from stretchy_report where report_name = 'Disbursal Report'),(select id from stretchy_parameter where parameter_name='startDateSelect'),'fromDate');
INSERT INTO `stretchy_report_parameter` (report_id,parameter_id,report_parameter_name) VALUES ((select id from stretchy_report where report_name = 'Disbursal Report'),(select id from stretchy_parameter where parameter_name='endDateSelect'),'toDate');

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('report', 'READ_Active Loan Summary per Branch', 'Active Loan Summary per Branch', 'READ', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('report', 'READ_Disbursal Report', 'Disbursal Report', 'READ', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('report', 'READ_Balance Outstanding', 'Balance Outstanding', 'READ', 0);
INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('report', 'READ_Collection Report', 'Collection Report', 'READ', 0);



