INSERT INTO stretchy_parameter ( parameter_name, parameter_variable, parameter_label, parameter_displayType, parameter_FormatType, parameter_default, special, selectOne, selectAll, parameter_sql, parent_id) VALUES ('transactionId', 'transactionId', 'transactionId', 'text', 'string', 'n/a', NULL, NULL, NULL, NULL, NULL);

INSERT INTO stretchy_report ( report_name, report_type, report_subtype, report_category, report_sql, description, core_report, use_report) VALUES ( 'Savings Transaction Receipt', 'Pentaho', NULL, NULL, NULL, NULL, 0, 1);

INSERT INTO stretchy_report_parameter ( report_id, parameter_id, report_parameter_name) 
VALUES ((select sr.id from stretchy_report sr where sr.report_name='Savings Transaction Receipt'),
 (select sp.id from stretchy_parameter sp where sp.parameter_name='transactionId'), 
 'transactionId');