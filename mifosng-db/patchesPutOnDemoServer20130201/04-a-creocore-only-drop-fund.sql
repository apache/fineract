ALTER TABLE `mifostenant-creocore`.`m_fund` 
DROP FOREIGN KEY `FK4E56DB408F889C3F`, 
DROP FOREIGN KEY `FK4E56DB40541F0A56`;

ALTER TABLE `mifostenant-creocore`.`m_fund` 
DROP COLUMN `lastmodified_date`, 
DROP COLUMN `lastmodifiedby_id`, 
DROP COLUMN `created_date`,
DROP COLUMN `createdby_id`,
DROP INDEX `FK4E56DB408F889C3F`, 
DROP INDEX `FK4E56DB40541F0A56`;