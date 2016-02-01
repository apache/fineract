ALTER TABLE `m_deposit_product_term_and_preclosure`
	ADD COLUMN `min_deposit_amount` DECIMAL(19,6) NULL DEFAULT NULL,
	ADD COLUMN `max_deposit_amount` DECIMAL(19,6) NULL DEFAULT NULL,
	ADD COLUMN `deposit_amount` DECIMAL(19,6) NULL DEFAULT NULL;

ALTER TABLE  m_deposit_account_term_and_preclosure
	ADD COLUMN  `expected_firstdepositon_date` DATE NULL DEFAULT NULL;