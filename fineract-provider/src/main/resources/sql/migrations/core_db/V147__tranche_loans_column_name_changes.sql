ALTER TABLE `m_product_loan`
	CHANGE COLUMN `multi_disburse_loan` `allow_multiple_disbursals` TINYINT(1) NOT NULL DEFAULT '0' AFTER `close_date`,
	CHANGE COLUMN `max_tranche_count` `max_disbursals` INT(2) NULL DEFAULT NULL AFTER `allow_multiple_disbursals`,
	CHANGE COLUMN `outstanding_loan_balance` `max_outstanding_loan_balance` DECIMAL(19,6) NULL DEFAULT NULL AFTER `max_disbursals`;