ALTER TABLE `m_office` 
DROP COLUMN `lastmodifiedby_id` , 
DROP COLUMN `lastmodified_date` , 
DROP COLUMN `created_date`, 
DROP COLUMN `createdby_id`;

ALTER TABLE `m_office_transaction` 
DROP COLUMN `lastmodifiedby_id` , 
DROP COLUMN `lastmodified_date` , 
DROP COLUMN `created_date`, 
DROP COLUMN `createdby_id`;