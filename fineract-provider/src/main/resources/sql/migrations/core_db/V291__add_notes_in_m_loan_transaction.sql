
ALTER TABLE `m_loan_transaction`
	CHANGE COLUMN `appuser_id` `createdby_id` BIGINT(20) NULL DEFAULT NULL AFTER `created_date`,
	ADD COLUMN `lastmodified_date` DATETIME NULL DEFAULT NULL AFTER `createdby_id`,
	ADD COLUMN `lastmodifiedby_id` BIGINT NULL  DEFAULT NULL AFTER `lastmodified_date`,
	ADD COLUMN `notes` LONGTEXT NULL DEFAULT NULL AFTER `lastmodifiedby_id`;