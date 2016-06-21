
ALTER TABLE `m_loan_transaction`
	CHANGE COLUMN `appuser_id` `createdby_id` BIGINT(20) NULL DEFAULT NULL AFTER `created_date`,
	ADD COLUMN `lastmodified_date` DATETIME NULL DEFAULT NULL AFTER `createdby_id`,
	ADD COLUMN `lastmodifiedby_id` BIGINT(20) NULL  DEFAULT NULL AFTER `lastmodified_date`,
	ADD COLUMN `transaction_sub_type_enum` SMALLINT NULL DEFAULT NULL AFTER `transaction_type_enum`;
	
ALTER TABLE `m_product_loan`
	ADD COLUMN `close_loan_on_overpayment` TINYINT(1) NOT NULL DEFAULT '0' AFTER `instalment_amount_in_multiples_of`;