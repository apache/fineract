
update m_permission p
join stretchy_report sr on p.code = concat('CAN_RUN_', sr.report_name)
set p.`code` = concat('READ_', sr.report_name), 
p.default_description = concat('READ_', sr.report_name), 
p.default_name = concat('READ_', sr.report_name);

update m_permission p
join x_registered_table r on p.code = concat('CAN_READ_', r.registered_table_name)
set p.`code` = concat('READ_', r.registered_table_name), 
p.default_description = concat('READ_', r.registered_table_name), 
p.default_name = concat('READ_', r.registered_table_name);

update m_permission p
join x_registered_table r on p.code = concat('CAN_UPDATE_', r.registered_table_name)
set p.`code` = concat('UPDATE_', r.registered_table_name), 
p.default_description = concat('UPDATE_', r.registered_table_name), 
p.default_name = concat('UPDATE_', r.registered_table_name);

update m_permission p
join x_registered_table r on p.code = concat('CAN_CREATE_', r.registered_table_name)
set p.`code` = concat('CREATE_', r.registered_table_name), 
p.default_description = concat('CREATE_', r.registered_table_name), 
p.default_name = concat('CREATE_', r.registered_table_name);

update m_permission p
join x_registered_table r on p.code = concat('CAN_DELETE_', r.registered_table_name)
set p.`code` = concat('DELETE_', r.registered_table_name), 
p.default_description = concat('DELETE_', r.registered_table_name), 
p.default_name = concat('DELETE_', r.registered_table_name);

