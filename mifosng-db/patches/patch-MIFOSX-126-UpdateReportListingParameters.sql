UPDATE stretchy_parameter
SET `parameter_sql` = 'select  r.report_id, r.report_name, r.report_type, r.report_subtype, r.report_category,\r  rp.parameter_id, rp.report_parameter_name, p.parameter_name\r  from stretchy_report r\r  left join stretchy_report_parameter rp on rp.report_id = r.report_id\r  left join stretchy_parameter p on p.parameter_id = rp.parameter_id\r  where r.use_report is true\r  and exists\r  (\r select \'f\'\r  from m_appuser_role ur \r  join m_role r on r.id = ur.role_id\r  join m_role_permission rp on rp.role_id = r.id\r  join m_permission p on p.id = rp.permission_id\r  where ur.appuser_id = ${currentUserId}\r  and (p.code in (\'ALL_FUNCTIONS_READ\', \'ALL_FUNCTIONS\') or p.code = concat(\"READ_\", r.report_name))\r )\r  order by r.report_name, rp.parameter_id' 
WHERE parameter_name = 'FullReportList';

UPDATE stretchy_parameter 
SET `parameter_sql` = 'select  r.report_id, r.report_name, r.report_type, r.report_subtype, r.report_category,\r  rp.parameter_id, rp.report_parameter_name, p.parameter_name\r  from stretchy_report r\r  left join stretchy_report_parameter rp on rp.report_id = r.report_id\r  left join stretchy_parameter p on p.parameter_id = rp.parameter_id\r  where r.report_category = \'${reportCategory}\'\r  and r.use_report is true\r  and exists\r  (\r select \'f\'\r  from m_appuser_role ur \r  join m_role r on r.id = ur.role_id\r  join m_role_permission rp on rp.role_id = r.id\r  join m_permission p on p.id = rp.permission_id\r  where ur.appuser_id = ${currentUserId}\r  and (p.code in (\'ALL_FUNCTIONS_READ\', \'ALL_FUNCTIONS\') or p.code = concat(\"READ_\", r.report_name))\r )\r  order by r.report_name, rp.parameter_id' 
WHERE parameter_name = 'reportCategoryList';



