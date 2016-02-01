INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_subtype`, `report_category`, `report_sql`, `description`, `core_report`, `use_report`) VALUES ('Savings Transactions', 'Pentaho', NULL, 'Savings', NULL, NULL, 0, 1);
INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_subtype`, `report_category`, `report_sql`, `description`, `core_report`, `use_report`) VALUES ('Client Savings Summary', 'Pentaho', NULL, 'Savings', NULL, NULL, 0, 1);

INSERT INTO `stretchy_parameter` (`parameter_name`, `parameter_variable`, `parameter_label`, `parameter_displayType`, `parameter_FormatType`, `parameter_default`, `special`, `selectOne`, `selectAll`, `parameter_sql`, `parent_id`) VALUES ( 'selectAccount', 'accountNo', 'Enter Account No', 'text', 'string', 'n/a', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `stretchy_parameter` (`parameter_name`, `parameter_variable`, `parameter_label`, `parameter_displayType`, `parameter_FormatType`, `parameter_default`, `special`, `selectOne`, `selectAll`, `parameter_sql`, `parent_id`) VALUES ('savingsProductIdSelectAll', 'savingsProductId', 'Product', 'select', 'number', '0', NULL, NULL, 'Y', 'select p.id, p.`name`\r\nfrom m_savings_product p\r\norder by 2', NULL);

INSERT INTO `stretchy_report_parameter` (`report_id`, `parameter_id`, `report_parameter_name`) VALUES ((select id from stretchy_report where report_name = 'Savings Transactions'), (select id from stretchy_parameter where parameter_name='startDateSelect'), 'fromDate');
INSERT INTO `stretchy_report_parameter` (`report_id`, `parameter_id`, `report_parameter_name`) VALUES ((select id from stretchy_report where report_name = 'Savings Transactions'), (select id from stretchy_parameter where parameter_name='endDateSelect'), 'toDate');
INSERT INTO `stretchy_report_parameter` (`report_id`, `parameter_id`, `report_parameter_name`) VALUES ((select id from stretchy_report where report_name = 'Savings Transactions'), (select id from stretchy_parameter where parameter_name='selectAccount'), 'accountNo');
INSERT INTO `stretchy_report_parameter` (`report_id`, `parameter_id`, `report_parameter_name`) VALUES ((select id from stretchy_report where report_name = 'Client Savings Summary'), (select id from stretchy_parameter where parameter_name='startDateSelect'), 'fromDate');
INSERT INTO `stretchy_report_parameter` (`report_id`, `parameter_id`, `report_parameter_name`) VALUES ((select id from stretchy_report where report_name = 'Client Savings Summary'), (select id from stretchy_parameter where parameter_name='endDateSelect'), 'toDate');
INSERT INTO `stretchy_report_parameter` (`report_id`, `parameter_id`, `report_parameter_name`) VALUES ((select id from stretchy_report where report_name = 'Client Savings Summary'), (select id from stretchy_parameter where parameter_name='OfficeIdSelectOne'), 'selectOffice');
INSERT INTO `stretchy_report_parameter` (`report_id`, `parameter_id`, `report_parameter_name`) VALUES ((select id from stretchy_report where report_name = 'Client Savings Summary'), (select id from stretchy_parameter where parameter_name='savingsProductIdSelectAll'), 'selectProduct');

ALTER TABLE `r_enum_value`
	ADD COLUMN `enum_type` TINYINT(1) NOT NULL AFTER `enum_value`;

INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('savings_transaction_type_enum', 1, 'deposit', 'deposit', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('savings_transaction_type_enum', 2, 'withdrawal', 'withdrawal', 1);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('savings_transaction_type_enum', 3, 'Interest Posting', 'Interest Posting', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('savings_transaction_type_enum', 4, 'Withdrawal Fee', 'Withdrawal Fee', 1);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('savings_transaction_type_enum', 5, 'Annual Fee', 'Annual Fee', 1);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('savings_transaction_type_enum', 6, 'Waive Charge', 'Waive Charge', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('savings_transaction_type_enum', 7, 'Pay Charge', 'Pay Charge', 1);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('savings_transaction_type_enum', 12, 'Initiate Transfer', 'Initiate Transfer', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('savings_transaction_type_enum', 13, 'Approve Transfer', 'Approve Transfer', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('savings_transaction_type_enum', 14, 'Withdraw Transfer', 'Withdraw Transfer', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('savings_transaction_type_enum', 15, 'Reject Transfer', 'Reject Transfer', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('savings_transaction_type_enum', 16, 'Written-Off', 'Written-Off', 0);
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('savings_transaction_type_enum', 17, 'Overdraft Interest', 'Overdraft Interest', 0);
