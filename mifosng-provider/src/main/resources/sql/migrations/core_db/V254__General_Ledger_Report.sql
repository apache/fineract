INSERT INTO `stretchy_report` (`report_name`, `report_type`, `report_subtype`, `report_category`, `report_sql`, `description`, `core_report`, `use_report`) VALUES ('GeneralLedgerReport', 'Pentaho', NULL, 'Accounting', NULL, NULL, 0, 1);

INSERT INTO `stretchy_parameter` (`parameter_name`, `parameter_variable`, `parameter_label`, `parameter_displayType`, `parameter_FormatType`, `parameter_default`, `special`, `selectOne`, `selectAll`, `parameter_sql`, `parent_id`) VALUES ('SelectGLAccountNO', 'GLAccountNO', 'GLAccountNO', 'select', 'number', '0', NULL, NULL, NULL, '\r\n\r\n\r\n\r\n\r\n(select id aid,name aname\r\nfrom acc_gl_account)\r\nunion\r\n(select -1,\'ALL\')       \r\norder by 1                                                                                                                                ', NULL);

INSERT INTO `stretchy_report_parameter` (`report_id`, `parameter_id`, `report_parameter_name`) VALUES
 ((select sr.id from stretchy_report sr where sr.report_name='GeneralLedgerReport'), 
 (select sp.id from stretchy_parameter sp where sp.parameter_name='SelectGLAccountNO'), 
  'Account');
  
   INSERT INTO `stretchy_report_parameter` (`report_id`, `parameter_id`, `report_parameter_name`) VALUES
 ((select sr.id from stretchy_report sr where sr.report_name='GeneralLedgerReport'), 
 (select sp.id from stretchy_parameter sp where sp.parameter_name='startDateselect'),
  'ondate');
  
    INSERT INTO `stretchy_report_parameter` (`report_id`, `parameter_id`, `report_parameter_name`) VALUES
 ((select sr.id from stretchy_report sr where sr.report_name='GeneralLedgerReport'), 
 (select sp.id from stretchy_parameter sp where sp.parameter_name='endDateselect'), 
  'todate');
  
   INSERT INTO `stretchy_report_parameter` (`report_id`, `parameter_id`, `report_parameter_name`) VALUES
 ((select sr.id from stretchy_report sr where sr.report_name='GeneralLedgerReport'), 
 (select sp.id from stretchy_parameter sp where sp.parameter_name='OfficeIdSelectOne'), 
  'office');
  