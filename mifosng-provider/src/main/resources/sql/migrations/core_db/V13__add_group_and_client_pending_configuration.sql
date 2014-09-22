

/*
default client and group status is active 300.  If you want to be able to have pending as the initial client stat
*/
INSERT INTO c_configuration (`name`, `enabled`) VALUES ('allow-pending-client-status', '0');
INSERT INTO c_configuration (`name`, `enabled`) VALUES ('allow-pending-group-status', '0');


INSERT INTO .`r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`)
 VALUES ('status_id', '0', 'Invalid', 'Invalid');
INSERT INTO .`r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`)
 VALUES ('status_id', '100', 'Pending', 'Pending');
INSERT INTO .`r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`)
 VALUES ('status_id', '300', 'Active', 'Active');
INSERT INTO .`r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`)
 VALUES ('status_id', '600', 'Closed', 'Closed');
INSERT INTO .`r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`)
 VALUES ('loan_status_id', '0', 'Invalid', 'Invalid');