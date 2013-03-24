
/*
Datatables defined as one to many need a subresource_id for maker-checker approval
*/

ALTER TABLE m_portfolio_command_source
 ADD COLUMN `subresource_id` BIGINT(20) NULL  AFTER `resource_id` ;
