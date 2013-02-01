ALTER TABLE `m_staff`  
DROP COLUMN `lastmodifiedby_id` , 
DROP COLUMN `lastmodified_date` , 
DROP COLUMN `created_date`, 
DROP COLUMN `createdby_id`;

ALTER TABLE `m_charge`  
DROP COLUMN `lastmodifiedby_id` , 
DROP COLUMN `lastmodified_date` , 
DROP COLUMN `created_date`, 
DROP COLUMN `createdby_id`;

ALTER TABLE `m_document`  
DROP COLUMN `lastmodifiedby_id` , 
DROP COLUMN `lastmodified_date` , 
DROP COLUMN `created_date`, 
DROP COLUMN `createdby_id`;