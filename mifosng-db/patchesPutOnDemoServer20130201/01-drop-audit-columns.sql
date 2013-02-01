ALTER TABLE `m_role` 
DROP COLUMN `lastmodifiedby_id` , 
DROP COLUMN `lastmodified_date` , 
DROP COLUMN `created_date` , 
DROP COLUMN `createdby_id`;

ALTER TABLE `m_appuser` 
DROP COLUMN `lastmodifiedby_id` ,
DROP COLUMN `lastmodified_date` ,
DROP COLUMN `created_date` ,
DROP COLUMN `createdby_id`;